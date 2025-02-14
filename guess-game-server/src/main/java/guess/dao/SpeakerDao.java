package guess.dao;

import guess.domain.source.Speaker;

import java.util.List;

/**
 * Speaker DAO.
 */
public interface SpeakerDao {
    List<Speaker> getSpeakers();

    Speaker getSpeakerById(long id);

    List<Speaker> getSpeakerByIds(List<Long> ids);
}
