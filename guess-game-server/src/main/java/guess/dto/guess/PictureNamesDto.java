package guess.dto.guess;

import guess.domain.Language;
import guess.domain.question.QuestionAnswers;
import guess.domain.question.SpeakerQuestion;
import guess.domain.source.Speaker;
import guess.util.LocalizationUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Picture, names DTO.
 */
public class PictureNamesDto extends QuestionAnswersDto {
    private final String fileName;

    private final String name0;
    private final String name1;
    private final String name2;
    private final String name3;

    public PictureNamesDto(String questionSetName, int currentIndex, int totalNumber, String logoFileName,
                           long id0, long id1, long id2, long id3,
                           boolean invalid0, boolean invalid1, boolean invalid2, boolean invalid3,
                           String fileName, String name0, String name1, String name2, String name3) {
        super(questionSetName, currentIndex, totalNumber, logoFileName, id0, id1, id2, id3, invalid0, invalid1, invalid2, invalid3);

        this.fileName = fileName;
        this.name0 = name0;
        this.name1 = name1;
        this.name2 = name2;
        this.name3 = name3;
    }

    public String getFileName() {
        return fileName;
    }

    public String getName0() {
        return name0;
    }

    public String getName1() {
        return name1;
    }

    public String getName2() {
        return name2;
    }

    public String getName3() {
        return name3;
    }

    public static PictureNamesDto convertToDto(String questionSetName, int currentIndex, int totalNumber, String logoFileName,
                                               QuestionAnswers questionAnswers, List<Long> wrongAnswerIds, Language language) {
        Speaker speaker0 = ((SpeakerQuestion) questionAnswers.getAnswers().get(0)).getSpeaker();
        Speaker speaker1 = ((SpeakerQuestion) questionAnswers.getAnswers().get(1)).getSpeaker();
        Speaker speaker2 = ((SpeakerQuestion) questionAnswers.getAnswers().get(2)).getSpeaker();
        Speaker speaker3 = ((SpeakerQuestion) questionAnswers.getAnswers().get(3)).getSpeaker();

        Set<Speaker> speakerDuplicates = LocalizationUtils.getSpeakerDuplicates(
                Arrays.asList(speaker0, speaker1, speaker2, speaker3),
                language,
                s -> LocalizationUtils.getString(s.getName(), language),
                s -> true);

        String name0 = LocalizationUtils.getSpeakerName(speaker0, language, speakerDuplicates);
        String name1 = LocalizationUtils.getSpeakerName(speaker1, language, speakerDuplicates);
        String name2 = LocalizationUtils.getSpeakerName(speaker2, language, speakerDuplicates);
        String name3 = LocalizationUtils.getSpeakerName(speaker3, language, speakerDuplicates);

        return new PictureNamesDto(questionSetName, currentIndex, totalNumber, logoFileName,
                questionAnswers.getAnswers().get(0).getId(), questionAnswers.getAnswers().get(1).getId(),
                questionAnswers.getAnswers().get(2).getId(), questionAnswers.getAnswers().get(3).getId(),
                wrongAnswerIds.contains(questionAnswers.getAnswers().get(0).getId()),
                wrongAnswerIds.contains(questionAnswers.getAnswers().get(1).getId()),
                wrongAnswerIds.contains(questionAnswers.getAnswers().get(2).getId()),
                wrongAnswerIds.contains(questionAnswers.getAnswers().get(3).getId()),
                ((SpeakerQuestion) questionAnswers.getQuestion()).getSpeaker().getFileName(),
                name0, name1, name2, name3);
    }
}
