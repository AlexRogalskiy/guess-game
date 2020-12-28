package guess.service;

import guess.dao.SpeakerDao;
import guess.domain.Language;
import guess.domain.source.Speaker;
import guess.util.LocalizationUtils;
import guess.util.SearchUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Speaker service implementation.
 */
@Service
public class SpeakerServiceImpl implements SpeakerService {
    private final SpeakerDao speakerDao;

    @Autowired
    public SpeakerServiceImpl(SpeakerDao speakerDao) {
        this.speakerDao = speakerDao;
    }

    @Override
    public Speaker getSpeakerById(long id) {
        return speakerDao.getSpeakerById(id);
    }

    @Override
    public List<Speaker> getSpeakersByFirstLetter(String firstLetter, Language language) {
        return speakerDao.getSpeakers().stream()
                .filter(s -> {
                    String name = LocalizationUtils.getString(s.getNameWithLastNameFirst(), language);
                    String nameFirstLetter;

                    if (name != null) {
                        String trimmedName = name.trim();

                        nameFirstLetter = (trimmedName.length() > 0) ? trimmedName.substring(0, 1) : null;
                    } else {
                        nameFirstLetter = null;
                    }

                    return firstLetter.equalsIgnoreCase(nameFirstLetter);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Speaker> getSpeakers(String name, String company, String twitter, String gitHub, boolean isJavaChampion, boolean isMvp) {
        String trimmedLowerCasedName = SearchUtils.trimAndLowerCase(name);
        String trimmedLowerCasedCompany = SearchUtils.trimAndLowerCase(company);
        String trimmedLowerCasedTwitter = SearchUtils.trimAndLowerCase(twitter);
        String trimmedLowerCasedGitHub = SearchUtils.trimAndLowerCase(gitHub);
        boolean isNameSet = SearchUtils.isStringSet(trimmedLowerCasedName);
        boolean isCompanySet = SearchUtils.isStringSet(trimmedLowerCasedCompany);
        boolean isTwitterSet = SearchUtils.isStringSet(trimmedLowerCasedTwitter);
        boolean isGitHubSet = SearchUtils.isStringSet(trimmedLowerCasedGitHub);

        if (!isNameSet && !isCompanySet && !isTwitterSet && !isGitHubSet && !isJavaChampion && !isMvp) {
            return Collections.emptyList();
        } else {
            return speakerDao.getSpeakers().stream()
                    .filter(s -> ((!isNameSet || SearchUtils.isSubstringFound(trimmedLowerCasedName, s.getName())) &&
                            (!isCompanySet || isSpeakerCompanyFound(s, trimmedLowerCasedCompany)) &&
                            (!isTwitterSet || SearchUtils.isSubstringFound(trimmedLowerCasedTwitter, s.getTwitter())) &&
                            (!isGitHubSet || SearchUtils.isSubstringFound(trimmedLowerCasedGitHub, s.getGitHub())) &&
                            (!isJavaChampion || s.isJavaChampion()) &&
                            (!isMvp || s.isAnyMvp())))
                    .collect(Collectors.toList());
        }
    }

    static boolean isSpeakerCompanyFound(Speaker speaker, String trimmedLowerCasedCompany) {
        return speaker.getCompanies().stream()
                .anyMatch(c -> SearchUtils.isSubstringFound(trimmedLowerCasedCompany, c.getName()));
    }
}
