package guess.dao;

import guess.domain.source.Event;
import guess.domain.source.Talk;

import java.time.LocalDate;
import java.util.List;

/**
 * Event DAO.
 */
public interface EventDao {
    List<Event> getEvents();

    Event getEventById(long id);

    List<Event> getEventsByEventTypeId(long eventTypeId);

    List<Event> getEventsFromDate(LocalDate date);

    Event getEventByTalk(Talk talk);
}
