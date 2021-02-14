package guess.domain.source;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Event.
 */
public class Event extends Nameable {
    public static class EventDates {
        private final LocalDate startDate;
        private final LocalDate endDate;

        public EventDates(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    public static class EventLinks {
        private final List<LocaleItem> siteLink;
        private final String youtubeLink;

        public EventLinks(List<LocaleItem> siteLink, String youtubeLink) {
            this.siteLink = siteLink;
            this.youtubeLink = youtubeLink;
        }
    }

    private long eventTypeId;
    private EventType eventType;

    private LocalDate startDate;
    private LocalDate endDate;
    private List<LocaleItem> siteLink;
    private String youtubeLink;

    private long placeId;
    private Place place;

    private String timeZone;
    private ZoneId timeZoneId;

    private List<Long> talkIds;
    private List<Talk> talks = new ArrayList<>();

    public Event() {
    }

    public Event(Nameable nameable, EventType eventType, EventDates dates, EventLinks links, Place place, String timeZone,
                 List<Talk> talks) {
        super(nameable.getId(), nameable.getName());

        this.eventType = eventType;
        this.startDate = dates.startDate;
        this.endDate = dates.endDate;
        this.siteLink = links.siteLink;
        this.youtubeLink = links.youtubeLink;

        this.place = place;
        this.placeId = place.getId();

        this.timeZone = timeZone;
        this.timeZoneId = (timeZone != null) ? ZoneId.of(timeZone) : null;

        this.talks = talks;
        this.talkIds = talks.stream()
                .map(Talk::getId)
                .collect(Collectors.toList());
    }

    public long getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(long eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<LocaleItem> getSiteLink() {
        return siteLink;
    }

    public void setSiteLink(List<LocaleItem> siteLink) {
        this.siteLink = siteLink;
    }

    public String getYoutubeLink() {
        return youtubeLink;
    }

    public void setYoutubeLink(String youtubeLink) {
        this.youtubeLink = youtubeLink;
    }

    public long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(long placeId) {
        this.placeId = placeId;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public ZoneId getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(ZoneId timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public List<Long> getTalkIds() {
        return talkIds;
    }

    public void setTalkIds(List<Long> talkIds) {
        this.talkIds = talkIds;
    }

    public List<Talk> getTalks() {
        return talks;
    }

    public void setTalks(List<Talk> talks) {
        this.talks = talks;
    }

    public ZoneId getFinalTimeZoneId() {
        return (timeZoneId != null) ? timeZoneId : eventType.getTimeZoneId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(eventType, event.eventType) &&
                Objects.equals(startDate, event.startDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, startDate);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + getId() +
                ", eventType=" + eventType +
                ", name=" + getName() +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", place=" + place +
                ", talks=" + talks +
                '}';
    }
}
