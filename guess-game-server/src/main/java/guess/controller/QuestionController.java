package guess.controller;

import guess.dao.exception.QuestionSetNotExistsException;
import guess.domain.GuessMode;
import guess.domain.Language;
import guess.domain.source.Event;
import guess.domain.source.EventType;
import guess.dto.event.EventSuperBriefDto;
import guess.dto.event.EventDto;
import guess.dto.start.EventTypeBriefDto;
import guess.dto.start.EventTypeDto;
import guess.service.EventTypeService;
import guess.service.LocaleService;
import guess.service.QuestionService;
import guess.util.LocalizationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Comparator;
import java.util.List;

/**
 * Question controller.
 */
@Controller
@RequestMapping("/api/question")
public class QuestionController {
    private final QuestionService questionService;
    private final EventTypeService eventTypeService;
    private final LocaleService localeService;

    @Autowired
    public QuestionController(QuestionService questionService, EventTypeService eventTypeService, LocaleService localeService) {
        this.questionService = questionService;
        this.eventTypeService = eventTypeService;
        this.localeService = localeService;
    }

    @GetMapping("/event-types")
    @ResponseBody
    public List<EventTypeBriefDto> getEventTypes(HttpSession httpSession) {
        List<EventType> eventTypes = eventTypeService.getEventTypes();
        Language language = localeService.getLanguage(httpSession);
        Comparator<EventType> comparatorByIsConference = Comparator.comparing(EventType::isEventTypeConference).reversed();
        Comparator<EventType> comparatorByInactive = Comparator.comparing(EventType::isInactive);
        Comparator<EventType> comparatorByName = Comparator.comparing(et -> LocalizationUtils.getString(et.getName(), language), String.CASE_INSENSITIVE_ORDER);

        eventTypes.sort(comparatorByIsConference.thenComparing(comparatorByInactive).thenComparing(comparatorByName));

        return EventTypeDto.convertToBriefDto(eventTypes, language);
    }

    @GetMapping("/events")
    @ResponseBody
    public List<EventSuperBriefDto> getEvents(@RequestParam List<Long> eventTypeIds, HttpSession httpSession) {
        List<Event> events = questionService.getEvents(eventTypeIds);
        Language language = localeService.getLanguage(httpSession);

        events.sort(Comparator.comparing(Event::getStartDate).reversed());

        return EventDto.convertToSuperBriefDto(events, language);
    }

    @GetMapping("/quantities")
    @ResponseBody
    public List<Integer> getQuantities(@RequestParam List<Long> eventTypeIds, @RequestParam List<Long> eventIds,
                                       @RequestParam String guessMode) throws QuestionSetNotExistsException {
        return questionService.getQuantities(eventTypeIds, eventIds, GuessMode.valueOf(guessMode));
    }
}
