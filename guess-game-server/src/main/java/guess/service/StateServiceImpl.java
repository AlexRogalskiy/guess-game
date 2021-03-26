package guess.service;

import guess.dao.*;
import guess.domain.*;
import guess.domain.answer.*;
import guess.domain.question.*;
import guess.domain.source.*;
import guess.util.LocalizationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * State service implementation.
 */
@Service
public class StateServiceImpl implements StateService {
    private static final String UNKNOWN_GUESS_MODE_TEXT = "Unknown guess mode: %s";

    private final StateDao stateDao;
    private final QuestionDao questionDao;
    private final AnswerDao answerDao;
    private final EventTypeDao eventTypeDao;
    private final EventDao eventDao;

    @Autowired
    public StateServiceImpl(StateDao stateDao, QuestionDao questionDao, AnswerDao answerDao, EventTypeDao eventTypeDao, EventDao eventDao) {
        this.stateDao = stateDao;
        this.questionDao = questionDao;
        this.answerDao = answerDao;
        this.eventTypeDao = eventTypeDao;
        this.eventDao = eventDao;
    }

    @Override
    public void setStartParameters(StartParameters startParameters, HttpSession httpSession) {
        stateDao.setStartParameters(startParameters, httpSession);

        QuestionAnswersSet questionAnswersSet = createQuestionAnswersSet(startParameters);
        stateDao.setQuestionAnswersSet(questionAnswersSet, httpSession);

        answerDao.clearAnswerSets(httpSession);
        stateDao.setGameState(
                questionAnswersSet.getQuestionAnswersList().isEmpty() ?
                        GameState.RESULT_STATE :
                        getStateByGuessMode(startParameters.getGuessMode()),
                httpSession);
    }

    @Override
    public void deleteStartParameters(HttpSession httpSession) {
        stateDao.clearStartParameters(httpSession);
        stateDao.clearQuestionAnswersSet(httpSession);
        answerDao.clearAnswerSets(httpSession);
        stateDao.setGameState(GameState.START_STATE, httpSession);
    }

    GameState getStateByGuessMode(GuessMode guessMode) {
        if (GuessMode.GUESS_NAME_BY_PHOTO_MODE.equals(guessMode)) {
            return GameState.GUESS_NAME_BY_PHOTO_STATE;
        } else if (GuessMode.GUESS_PHOTO_BY_NAME_MODE.equals(guessMode)) {
            return GameState.GUESS_PHOTO_BY_NAME_STATE;
        } else if (GuessMode.GUESS_TALK_BY_SPEAKER_MODE.equals(guessMode)) {
            return GameState.GUESS_TALK_BY_SPEAKER_STATE;
        } else if (GuessMode.GUESS_SPEAKER_BY_TALK_MODE.equals(guessMode)) {
            return GameState.GUESS_SPEAKER_BY_TALK_STATE;
        } else if (GuessMode.GUESS_COMPANY_BY_SPEAKER_MODE.equals(guessMode)) {
            return GameState.GUESS_COMPANY_BY_SPEAKER_STATE;
        } else if (GuessMode.GUESS_SPEAKER_BY_COMPANY_MODE.equals(guessMode)) {
            return GameState.GUESS_SPEAKER_BY_COMPANY_STATE;
        } else if (GuessMode.GUESS_ACCOUNT_BY_SPEAKER_MODE.equals(guessMode)) {
            return GameState.GUESS_ACCOUNT_BY_SPEAKER_STATE;
        } else if (GuessMode.GUESS_SPEAKER_BY_ACCOUNT_MODE.equals(guessMode)) {
            return GameState.GUESS_SPEAKER_BY_ACCOUNT_STATE;
        } else if (GuessMode.GUESS_TAG_CLOUD_BY_SPEAKER_MODE.equals(guessMode)) {
            return GameState.GUESS_TAG_CLOUD_BY_SPEAKER_STATE;
        } else if (GuessMode.GUESS_SPEAKER_BY_TAG_CLOUD_MODE.equals(guessMode)) {
            return GameState.GUESS_SPEAKER_BY_TAG_CLOUD_STATE;
        } else {
            throw new IllegalArgumentException(String.format(UNKNOWN_GUESS_MODE_TEXT, guessMode));
        }
    }

    @Override
    public GameState getState(HttpSession httpSession) {
        return stateDao.getGameState(httpSession);
    }

    @Override
    public void setState(GameState state, HttpSession httpSession) {
        stateDao.setGameState(state, httpSession);
    }

    @Override
    public QuestionAnswersSet getQuestionAnswersSet(HttpSession httpSession) {
        return stateDao.getQuestionAnswersSet(httpSession);
    }

    QuestionAnswersSet createQuestionAnswersSet(StartParameters startParameters) {
        // Find unique questions by ids
        List<Question> uniqueQuestions = questionDao.getQuestionByIds(startParameters.getEventTypeIds(), startParameters.getEventIds(), startParameters.getGuessMode());

        // Fill question and answers list
        List<QuestionAnswers> questionAnswersList = new ArrayList<>();
        if (uniqueQuestions.size() >= QuestionAnswersSet.QUESTION_ANSWERS_LIST_SIZE) {
            // Shuffle questions
            List<Question> shuffledQuestions = new ArrayList<>(uniqueQuestions);
            Collections.shuffle(shuffledQuestions);

            // Select first "quantity" elements
            List<Question> selectedShuffledQuestions = shuffledQuestions.subList(
                    0,
                    Math.min(startParameters.getQuantity(), shuffledQuestions.size()));

            Map<Long, Answer> answerCache = new HashMap<>();

            // Create question/answers list
            for (Question question : selectedShuffledQuestions) {
                List<Answer> correctAnswers = new ArrayList<>(getCorrectAnswers(question, startParameters.getGuessMode(), answerCache));
                Collections.shuffle(correctAnswers);

                // Correct answers size must be < QUESTION_ANSWERS_LIST_SIZE
                correctAnswers = correctAnswers.subList(
                        0,
                        Math.min(QuestionAnswersSet.QUESTION_ANSWERS_LIST_SIZE - 1, correctAnswers.size()));
                List<Answer> shuffledAllAvailableAnswersWithoutCorrectAnswers = new ArrayList<>(getAllAvailableAnswers(
                        shuffledQuestions, question, correctAnswers, startParameters.getGuessMode(), answerCache));

                shuffledAllAvailableAnswersWithoutCorrectAnswers.removeAll(correctAnswers);
                Collections.shuffle(shuffledAllAvailableAnswersWithoutCorrectAnswers);

                // Select (QUESTION_ANSWERS_LIST_SIZE - correctAnswers.size()) first elements, add correct answers, shuffle
                List<Answer> availableAnswers = shuffledAllAvailableAnswersWithoutCorrectAnswers.subList(
                        0,
                        Math.min(QuestionAnswersSet.QUESTION_ANSWERS_LIST_SIZE - correctAnswers.size(), shuffledAllAvailableAnswersWithoutCorrectAnswers.size()));
                availableAnswers.addAll(correctAnswers);
                Collections.shuffle(availableAnswers);

                questionAnswersList.add(new QuestionAnswers(
                        question,
                        correctAnswers,
                        new Quadruple<>(availableAnswers.get(0), availableAnswers.get(1), availableAnswers.get(2), availableAnswers.get(3))));
            }
        }

        List<LocaleItem> name;
        String logoFileName;

        // Set name and logo filename
        if (startParameters.getEventTypeIds().size() == 1) {
            EventType eventType = eventTypeDao.getEventTypeById(startParameters.getEventTypeIds().get(0));
            Event event;

            if (eventType.isEventTypeConference() &&
                    (startParameters.getEventIds().size() == 1) &&
                    ((event = eventDao.getEventById(startParameters.getEventIds().get(0))) != null)) {
                name = event.getName();
            } else {
                name = eventType.getName();
            }

            logoFileName = eventType.getLogoFileName();
        } else {
            final String SELECTED_EVENT_TYPES = "selectedEventTypes";

            name = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), String.format(
                            LocalizationUtils.getResourceString(SELECTED_EVENT_TYPES, Language.ENGLISH),
                            startParameters.getEventTypeIds().size())),
                    new LocaleItem(Language.RUSSIAN.getCode(), String.format(
                            LocalizationUtils.getResourceString(SELECTED_EVENT_TYPES, Language.RUSSIAN),
                            startParameters.getEventTypeIds().size())));
            logoFileName = null;
        }

        return new QuestionAnswersSet(name, logoFileName, questionAnswersList);
    }

    List<Answer> getCorrectAnswers(Question question, GuessMode guessMode, Map<Long, Answer> answerCache) {
        if (GuessMode.GUESS_NAME_BY_PHOTO_MODE.equals(guessMode) ||
                GuessMode.GUESS_PHOTO_BY_NAME_MODE.equals(guessMode) ||
                GuessMode.GUESS_ACCOUNT_BY_SPEAKER_MODE.equals(guessMode) ||
                GuessMode.GUESS_SPEAKER_BY_ACCOUNT_MODE.equals(guessMode)) {
            Speaker speaker = ((SpeakerQuestion) question).getSpeaker();
            SpeakerAnswer speakerAnswer = (SpeakerAnswer) answerCache.computeIfAbsent(
                    speaker.getId(),
                    k -> new SpeakerAnswer(speaker));

            return Collections.singletonList(speakerAnswer);
        } else if (GuessMode.GUESS_TALK_BY_SPEAKER_MODE.equals(guessMode)) {
            Talk talk = ((TalkQuestion) question).getTalk();
            TalkAnswer talkAnswer = (TalkAnswer) answerCache.computeIfAbsent(
                    talk.getId(),
                    k -> new TalkAnswer(talk));

            return Collections.singletonList(talkAnswer);
        } else if (GuessMode.GUESS_SPEAKER_BY_TALK_MODE.equals(guessMode)) {
            return ((TalkQuestion) question).getSpeakers().stream()
                    .map(s -> (SpeakerAnswer) answerCache.computeIfAbsent(
                            s.getId(),
                            k -> new SpeakerAnswer(s)))
                    .collect(Collectors.toList());
        } else if (GuessMode.GUESS_COMPANY_BY_SPEAKER_MODE.equals(guessMode)) {
            return ((CompanyBySpeakerQuestion) question).getCompanies().stream()
                    .map(c -> (CompanyAnswer) answerCache.computeIfAbsent(
                            c.getId(),
                            k -> new CompanyAnswer(c)))
                    .collect(Collectors.toList());
        } else if (GuessMode.GUESS_SPEAKER_BY_COMPANY_MODE.equals(guessMode)) {
            return ((SpeakerByCompanyQuestion) question).getSpeakers().stream()
                    .map(s -> (SpeakerAnswer) answerCache.computeIfAbsent(
                            s.getId(),
                            k -> new SpeakerAnswer(s)))
                    .collect(Collectors.toList());
        } else if (GuessMode.GUESS_TAG_CLOUD_BY_SPEAKER_MODE.equals(guessMode)) {
            Speaker speaker = ((TagCloudQuestion) question).getSpeaker();
            TagCloudAnswer tagCloudAnswer = (TagCloudAnswer) answerCache.computeIfAbsent(
                    speaker.getId(),
                    k -> new TagCloudAnswer(
                            speaker,
                            ((TagCloudQuestion) question).getLanguageWordFrequenciesMap())
            );

            return Collections.singletonList(tagCloudAnswer);
        } else if (GuessMode.GUESS_SPEAKER_BY_TAG_CLOUD_MODE.equals(guessMode)) {
            Speaker speaker = ((TagCloudQuestion) question).getSpeaker();
            SpeakerAnswer speakerAnswer = (SpeakerAnswer) answerCache.computeIfAbsent(
                    speaker.getId(),
                    k -> new SpeakerAnswer(speaker));

            return Collections.singletonList(speakerAnswer);
        } else {
            throw new IllegalArgumentException(String.format(UNKNOWN_GUESS_MODE_TEXT, guessMode));
        }
    }

    List<Answer> getAllAvailableAnswers(List<Question> questions, Question question, List<Answer> correctAnswers,
                                        GuessMode guessMode, Map<Long, Answer> answerCache) {
        if (GuessMode.GUESS_NAME_BY_PHOTO_MODE.equals(guessMode) ||
                GuessMode.GUESS_PHOTO_BY_NAME_MODE.equals(guessMode) ||
                GuessMode.GUESS_ACCOUNT_BY_SPEAKER_MODE.equals(guessMode) ||
                GuessMode.GUESS_SPEAKER_BY_ACCOUNT_MODE.equals(guessMode)) {
            return questions.stream()
                    .map(q -> {
                                Speaker speaker = ((SpeakerQuestion) q).getSpeaker();

                                return (SpeakerAnswer) answerCache.computeIfAbsent(
                                        speaker.getId(),
                                        k -> new SpeakerAnswer(speaker));
                            }
                    )
                    .collect(Collectors.toList());
        } else if (GuessMode.GUESS_TALK_BY_SPEAKER_MODE.equals(guessMode)) {
            List<Speaker> questionSpeakers = ((TalkQuestion) question).getSpeakers();
            Talk correctAnswerTalk = ((TalkAnswer) correctAnswers.get(0)).getTalk();

            return questions.stream()
                    .map(q -> ((TalkQuestion) q).getTalk())
                    .filter(t -> (t.getId() == correctAnswerTalk.getId()) || !t.getSpeakers().containsAll(questionSpeakers))
                    .map(t -> (TalkAnswer) answerCache.computeIfAbsent(
                            t.getId(),
                            k -> new TalkAnswer(t)))
                    .collect(Collectors.toList());
        } else if (GuessMode.GUESS_SPEAKER_BY_TALK_MODE.equals(guessMode)) {
            Set<Speaker> questionSpeakers = new HashSet<>(((TalkQuestion) question).getSpeakers());
            Set<Speaker> correctAnswerSpeakers = correctAnswers.stream()
                    .map(a -> ((SpeakerAnswer) a).getSpeaker())
                    .collect(Collectors.toSet());

            return questions.stream()
                    .map(q -> ((TalkQuestion) q).getTalk().getSpeakers())
                    .flatMap(Collection::stream)
                    .distinct()
                    .filter(s -> (correctAnswerSpeakers.contains(s) || !questionSpeakers.contains(s)))
                    .map(s -> (SpeakerAnswer) answerCache.computeIfAbsent(
                            s.getId(),
                            k -> new SpeakerAnswer(s)))
                    .collect(Collectors.toList());
        } else if (GuessMode.GUESS_COMPANY_BY_SPEAKER_MODE.equals(guessMode)) {
            Set<Company> questionCompanies = new HashSet<>(((CompanyBySpeakerQuestion) question).getCompanies());
            Set<Company> correctAnswerCompanies = correctAnswers.stream()
                    .map(a -> ((CompanyAnswer) a).getCompany())
                    .collect(Collectors.toSet());

            return questions.stream()
                    .map(q -> ((CompanyBySpeakerQuestion) q).getCompanies())
                    .flatMap(Collection::stream)
                    .distinct()
                    .filter(c -> (correctAnswerCompanies.contains(c) || !questionCompanies.contains(c)))
                    .map(c -> (CompanyAnswer) answerCache.computeIfAbsent(
                            c.getId(),
                            k -> new CompanyAnswer(c)))
                    .collect(Collectors.toList());
        } else if (GuessMode.GUESS_SPEAKER_BY_COMPANY_MODE.equals(guessMode)) {
            Company company = ((SpeakerByCompanyQuestion) question).getCompany();
            Set<Speaker> correctAnswerSpeakers = correctAnswers.stream()
                    .map(a -> ((SpeakerAnswer) a).getSpeaker())
                    .collect(Collectors.toSet());

            return questions.stream()
                    .map(q -> ((SpeakerByCompanyQuestion) q).getSpeakers())
                    .flatMap(Collection::stream)
                    .distinct()
                    .filter(s -> (correctAnswerSpeakers.contains(s) || !s.getCompanies().contains(company)))
                    .map(s -> (SpeakerAnswer) answerCache.computeIfAbsent(
                            s.getId(),
                            k -> new SpeakerAnswer(s)))
                    .collect(Collectors.toList());
        } else if (GuessMode.GUESS_TAG_CLOUD_BY_SPEAKER_MODE.equals(guessMode)) {
            return questions.stream()
                    .map(q -> {
                        Speaker speaker = ((TagCloudQuestion) q).getSpeaker();

                        return (TagCloudAnswer) answerCache.computeIfAbsent(
                                speaker.getId(),
                                k -> new TagCloudAnswer(
                                        speaker,
                                        ((TagCloudQuestion) q).getLanguageWordFrequenciesMap())
                        );
                    })
                    .collect(Collectors.toList());
        } else if (GuessMode.GUESS_SPEAKER_BY_TAG_CLOUD_MODE.equals(guessMode)) {
            return questions.stream()
                    .map(q -> {
                        Speaker speaker = ((TagCloudQuestion) q).getSpeaker();

                        return (SpeakerAnswer) answerCache.computeIfAbsent(
                                speaker.getId(),
                                k -> new SpeakerAnswer(speaker));
                    })
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(String.format(UNKNOWN_GUESS_MODE_TEXT, guessMode));
        }
    }
}
