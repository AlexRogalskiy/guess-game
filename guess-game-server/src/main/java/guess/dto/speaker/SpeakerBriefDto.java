package guess.dto.speaker;

import guess.domain.Language;
import guess.domain.source.Speaker;
import guess.dto.company.CompanyDto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Speaker DTO (brief).
 */
public class SpeakerBriefDto extends SpeakerSuperBriefDto {
    public static class SpeakerBriefDtoDegrees {
        private final boolean javaChampion;
        private final boolean mvp;
        private final boolean mvpReconnect;
        private final boolean anyMvp;

        public SpeakerBriefDtoDegrees(boolean javaChampion, boolean mvp, boolean mvpReconnect, boolean anyMvp) {
            this.javaChampion = javaChampion;
            this.mvp = mvp;
            this.mvpReconnect = mvpReconnect;
            this.anyMvp = anyMvp;
        }
    }

    private final String photoFileName;
    private final List<CompanyDto> companies;
    private final String twitter;
    private final String gitHub;
    private final String habr;
    private final boolean javaChampion;
    private final boolean mvp;
    private final boolean mvpReconnect;
    private final boolean anyMvp;

    public SpeakerBriefDto(SpeakerSuperBriefDto speakerSuperBriefDto, String photoFileName, List<CompanyDto> companies,
                           String twitter, String gitHub, String habr, SpeakerBriefDtoDegrees degrees) {
        super(speakerSuperBriefDto.getId(), speakerSuperBriefDto.getDisplayName());

        this.photoFileName = photoFileName;
        this.companies = companies;
        this.twitter = twitter;
        this.gitHub = gitHub;
        this.habr = habr;
        this.javaChampion = degrees.javaChampion;
        this.mvp = degrees.mvp;
        this.mvpReconnect = degrees.mvpReconnect;
        this.anyMvp = degrees.anyMvp;
    }

    public String getPhotoFileName() {
        return photoFileName;
    }

    public List<CompanyDto> getCompanies() {
        return companies;
    }

    public String getTwitter() {
        return twitter;
    }

    public String getGitHub() {
        return gitHub;
    }

    public String getHabr() {
        return habr;
    }

    public boolean isJavaChampion() {
        return javaChampion;
    }

    public boolean isMvp() {
        return mvp;
    }

    public boolean isMvpReconnect() {
        return mvpReconnect;
    }

    public boolean isAnyMvp() {
        return anyMvp;
    }

    public static SpeakerBriefDto convertToBriefDto(SpeakerSuperBriefDto speakerSuperBriefDto, Speaker speaker, Language language) {
        return new SpeakerBriefDto(
                speakerSuperBriefDto,
                speaker.getPhotoFileName(),
                CompanyDto.convertToDto(speaker.getCompanies(), language),
                speaker.getTwitter(),
                speaker.getGitHub(),
                speaker.getHabr(),
                new SpeakerBriefDtoDegrees(
                        speaker.isJavaChampion(),
                        speaker.isMvp(),
                        speaker.isMvpReconnect(),
                        speaker.isAnyMvp()
                ));
    }

    public static SpeakerBriefDto convertToBriefDto(Speaker speaker, Language language) {
        return convertToBriefDto(convertToSuperBriefDto(speaker, language), speaker, language);
    }

    public static List<SpeakerBriefDto> convertToBriefDto(List<Speaker> speakers, Language language) {
        return speakers.stream()
                .map(s -> convertToBriefDto(s, language))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
