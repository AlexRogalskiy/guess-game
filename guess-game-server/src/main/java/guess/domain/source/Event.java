package guess.domain.source;

import java.time.LocalDate;
import java.util.List;

/**
 * Event.
 */
public class Event extends AbstractEvent {
    //TODO: delete
    public record EventDates(LocalDate startDate, LocalDate endDate) {
    }

    private List<EventDays> days;
    private LocalDate startDate;    //TODO: delete
    private LocalDate endDate;      //TODO: delete

    private long placeId;           //TODO: delete
    private Place place;            //TODO: delete

    public Event() {
    }

    public Event(Nameable nameable, EventType eventType, List<EventDays> days, EventDates dates, EventLinks links, Place place,
                 String timeZone, List<Talk> talks) {
        super(nameable, eventType, links, timeZone, talks);

        this.days = days;
        this.startDate = dates.startDate;   //TODO: delete
        this.endDate = dates.endDate;       //TODO: delete

        this.place = place;                 //TODO: delete
        this.placeId = place.getId();       //TODO: delete
    }

    public List<EventDays> getDays() {
        return days;
    }

    public void setDays(List<EventDays> days) {
        this.days = days;
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

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public LocalDate getFirstStartDate() {
        if ((days != null) && !days.isEmpty()) {
            return days.get(0).getStartDate();
        } else {
            return null;
        }
    }

    public LocalDate getLastEndDate() {
        if ((days != null) && !days.isEmpty()) {
            return days.get(days.size() - 1).getEndDate();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + getId() +
                ", eventType=" + getEventType() +
                ", name=" + getName() +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", place=" + place +
                ", talks=" + getTalks() +
                '}';
    }
}
