package guess.controller;

import guess.domain.Language;
import guess.domain.source.EventType;
import guess.domain.statistics.EventStatistics;
import guess.domain.statistics.EventTypeStatistics;
import guess.domain.statistics.SpeakerStatistics;
import guess.dto.eventtype.EventTypeSuperBriefDto;
import guess.dto.statistics.*;
import guess.service.LocaleService;
import guess.service.StatisticsService;
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
 * Statistics controller.
 */
@Controller
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final LocaleService localeService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService, LocaleService localeService) {
        this.statisticsService = statisticsService;
        this.localeService = localeService;
    }

    @GetMapping("/event-type-statistics")
    @ResponseBody
    public EventTypeStatisticsDto getEventTypeStatistics(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                                         HttpSession httpSession) {
        EventTypeStatistics eventTypeStatistics = statisticsService.getEventTypeStatistics(conferences, meetups);
        Language language = localeService.getLanguage(httpSession);
        EventTypeStatisticsDto eventTypeStatisticsDto = EventTypeStatisticsDto.convertToDto(eventTypeStatistics, language);

        eventTypeStatisticsDto.getEventTypeMetricsList().sort(Comparator.comparing(EventTypeMetricsDto::getSortName, String.CASE_INSENSITIVE_ORDER));

        return eventTypeStatisticsDto;
    }

    @GetMapping("/event-statistics")
    @ResponseBody
    public EventStatisticsDto getEventStatistics(@RequestParam(required = false) Long eventTypeId, HttpSession httpSession) {
        EventStatistics eventStatistics = statisticsService.getEventStatistics(eventTypeId);
        Language language = localeService.getLanguage(httpSession);
        EventStatisticsDto eventStatisticsDto = EventStatisticsDto.convertToDto(eventStatistics, language);

        eventStatisticsDto.getEventMetricsList().sort(Comparator.comparing(EventMetricsDto::getName, String.CASE_INSENSITIVE_ORDER));

        return eventStatisticsDto;
    }

    @GetMapping("/speaker-statistics")
    @ResponseBody
    public SpeakerStatisticsDto getSpeakerStatistics(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                                     @RequestParam(required = false) Long eventTypeId, HttpSession httpSession) {
        SpeakerStatistics speakerStatistics = statisticsService.getSpeakerStatistics(conferences, meetups, eventTypeId);
        Language language = localeService.getLanguage(httpSession);
        SpeakerStatisticsDto speakerStatisticsDto = SpeakerStatisticsDto.convertToDto(speakerStatistics, language);
        Comparator<SpeakerMetricsDto> comparatorByTalksQuantity = Comparator.comparing(SpeakerMetricsDto::getTalksQuantity).reversed();
        Comparator<SpeakerMetricsDto> comparatorByEventsQuantity = Comparator.comparing(SpeakerMetricsDto::getEventsQuantity).reversed();
        Comparator<SpeakerMetricsDto> comparatorByEventTypesQuantity = Comparator.comparing(SpeakerMetricsDto::getEventTypesQuantity).reversed();

        speakerStatisticsDto.getSpeakerMetricsList().sort(comparatorByTalksQuantity.thenComparing(comparatorByEventsQuantity).thenComparing(comparatorByEventTypesQuantity));

        return speakerStatisticsDto;
    }

    @GetMapping("/conferences")
    @ResponseBody
    public List<EventTypeSuperBriefDto> getConferences(HttpSession httpSession) {
        List<EventType> eventTypes = statisticsService.getConferences();
        Language language = localeService.getLanguage(httpSession);
        List<EventTypeSuperBriefDto> eventTypeSuperBriefDtoList = EventTypeSuperBriefDto.convertToSuperBriefDto(eventTypes, language);

        eventTypeSuperBriefDtoList.sort(Comparator.comparing(EventTypeSuperBriefDto::getName, String.CASE_INSENSITIVE_ORDER));

        return eventTypeSuperBriefDtoList;
    }
}
