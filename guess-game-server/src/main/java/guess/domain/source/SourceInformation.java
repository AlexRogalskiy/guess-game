package guess.domain.source;

import java.util.List;
import java.util.Objects;

/**
 * Source information.
 */
public class SourceInformation {
    private final List<Place> places;
    private final List<Organizer> organizers;
    private final List<EventType> eventTypes;
    private final List<Event> events;
    private final List<Company> companies;
    private final List<CompanySynonyms> companySynonyms;
    private final List<Speaker> speakers;
    private final List<Talk> talks;

    public SourceInformation(List<Place> places, List<Organizer> organizers, List<EventType> eventTypes, List<Event> events,
                             List<Company> companies, List<CompanySynonyms> companySynonyms, List<Speaker> speakers,
                             List<Talk> talks) {
        this.eventTypes = eventTypes;
        this.places = places;
        this.organizers = organizers;
        this.events = events;
        this.companies = companies;
        this.companySynonyms = companySynonyms;
        this.speakers = speakers;
        this.talks = talks;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public List<Organizer> getOrganizers() {
        return organizers;
    }

    public List<EventType> getEventTypes() {
        return eventTypes;
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public List<CompanySynonyms> getCompanySynonyms() {
        return companySynonyms;
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public List<Talk> getTalks() {
        return talks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceInformation that = (SourceInformation) o;
        return Objects.equals(places, that.places) &&
                Objects.equals(organizers, that.organizers) &&
                Objects.equals(eventTypes, that.eventTypes) &&
                Objects.equals(events, that.events) &&
                Objects.equals(companies, that.companies) &&
                Objects.equals(companySynonyms, that.companySynonyms) &&
                Objects.equals(speakers, that.speakers) &&
                Objects.equals(talks, that.talks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(places, organizers, eventTypes, events, companies, companySynonyms, speakers, talks);
    }

    @Override
    public String toString() {
        return "SourceInformation{" +
                "places=" + places +
                ", organizers=" + organizers +
                ", eventTypes=" + eventTypes +
                ", events=" + events +
                ", companies=" + companies +
                ", companySynonyms=" + companySynonyms +
                ", speakers=" + speakers +
                ", talks=" + talks +
                '}';
    }
}
