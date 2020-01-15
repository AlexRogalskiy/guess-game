package guess.util;

import guess.domain.Conference;
import guess.domain.Language;
import guess.domain.source.*;
import guess.domain.source.contentful.event.ContentfulEventResponse;
import guess.domain.source.contentful.eventtype.ContentfulEventTypeResponse;
import guess.domain.source.contentful.locale.ContentfulLocale;
import guess.domain.source.contentful.locale.ContentfulLocaleResponse;
import guess.domain.source.contentful.speaker.ContentfulSpeakerResponse;
import guess.domain.source.contentful.talk.ContentfulTalkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Contentful utility methods.
 */
public class ContentfulUtils {
    private enum ConferenceSpaceInfo {
        // Joker, JPoint, JBreak, TechTrain, C++ Russia, Hydra, SPTDC, DevOops, SmartData
        COMMON_SPACE_INFO("oxjq45e8ilak", "fdc0ca21c8c39ac5a33e1e20880cae6836ae837af73c2cfc822650483ee388fe", "fields.speaker", "fields.javaChampion"),
        // HolysJS
        HOLYS_JS_SPACE_INFO("nn534z2fqr9f", "1ca5b5d059930cd6681083617578e5a61187d1a71cbd75d4e0059cca3dc85f8c", "fields.speakers"),
        // DotNext
        DOT_NEXT_SPACE_INFO("9n3x4rtjlya6", "14e1427f8fbee9e5a089cd634fc60189c7aff2814b496fb0ad957b867a59503b", "fields.speaker", "fields.mvp"),
        // Heisenbug
        HEISENBUG_SPACE_INFO("ut4a3ciohj8i", "e7edd5951d844b80ef41166e30cb9645e4f89d11c8ac9eecdadb2a38c061b980", "fields.speaker"),
        // Mobius
        MOBIUS_SPACE_INFO("2grufn031spf", "d0c680ed11f68287348b6b8481d3313fde8c2d23cc8ce24a2b0ae254dd779e6d", "fields.speaker");

        private final String spaceId;
        private final String accessToken;
        private final String speakerFlagFieldName;
        private final String speakerAdditionalFieldNames;

        ConferenceSpaceInfo(String spaceId, String accessToken, String speakerFlagFieldName, String speakerAdditionalFieldNames) {
            this.spaceId = spaceId;
            this.accessToken = accessToken;
            this.speakerFlagFieldName = speakerFlagFieldName;
            this.speakerAdditionalFieldNames = speakerAdditionalFieldNames;
        }

        ConferenceSpaceInfo(String spaceId, String accessToken, String speakerFlagFieldName) {
            this(spaceId, accessToken, speakerFlagFieldName, null);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ContentfulUtils.class);

    private static final String BASE_URL = "https://cdn.contentful.com/spaces/{spaceId}/{entityName}";
    private static final String MAIN_SPACE_ID = "2jxgmeypnru5";
    private static final String MAIN_ACCESS_TOKEN = "08f9e9e80ee347bd9f6017bf76f0a290c2ff0c28000946f7079f94a78974f090";

    private static Map<String, String> LOCALE_CODE_MAP = new HashMap<>() {{
        put("ru-RU", Language.RUSSIAN.getCode());
    }};

    private static Map<Conference, ConferenceSpaceInfo> CONFERENCE_SPACE_INFO_MAP = new HashMap<>() {{
        put(Conference.JOKER, ConferenceSpaceInfo.COMMON_SPACE_INFO);
        put(Conference.JPOINT, ConferenceSpaceInfo.COMMON_SPACE_INFO);
        put(Conference.JBREAK, ConferenceSpaceInfo.COMMON_SPACE_INFO);
        put(Conference.TECH_TRAIN, ConferenceSpaceInfo.COMMON_SPACE_INFO);
        put(Conference.CPP_RUSSIA, ConferenceSpaceInfo.COMMON_SPACE_INFO);
        put(Conference.HYDRA, ConferenceSpaceInfo.COMMON_SPACE_INFO);
        put(Conference.SPTDC, ConferenceSpaceInfo.COMMON_SPACE_INFO);
        put(Conference.DEV_OOPS, ConferenceSpaceInfo.COMMON_SPACE_INFO);
        put(Conference.SMART_DATA, ConferenceSpaceInfo.COMMON_SPACE_INFO);
        put(Conference.HOLY_JS, ConferenceSpaceInfo.HOLYS_JS_SPACE_INFO);
        put(Conference.DOT_NEXT, ConferenceSpaceInfo.DOT_NEXT_SPACE_INFO);
        put(Conference.HEISENBUG, ConferenceSpaceInfo.HEISENBUG_SPACE_INFO);
        put(Conference.MOBIUS, ConferenceSpaceInfo.MOBIUS_SPACE_INFO);
    }};

    private static final RestTemplate restTemplate;

    static {
        List<HttpMessageConverter<?>> converters = new ArrayList<>() {{
            add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
            add(new MappingJackson2HttpMessageConverter());
        }};
        restTemplate = new RestTemplate(converters);
    }

    /**
     * Gets locale codes.
     *
     * @param spaceId     space identifier
     * @param accessToken access token
     * @return locale codes
     */
    private static List<String> getLocales(String spaceId, String accessToken) {
        // https://cdn.contentful.com/spaces/{spaceId}/locales?access_token={accessToken}
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("access_token", accessToken);
        URI uri = builder
                .buildAndExpand(spaceId, "locales")
                .encode()
                .toUri();
        ContentfulLocaleResponse response = restTemplate.getForObject(uri, ContentfulLocaleResponse.class);

        return Objects.requireNonNull(response)
                .getItems().stream()
                .map(ContentfulLocale::getCode)
                .collect(Collectors.toList());
    }

    /**
     * Gets event types.
     *
     * @param spaceId     space identifier
     * @param accessToken access token
     * @param locale      locale
     * @return event types
     */
    private static List<EventType> getEventTypes(String spaceId, String accessToken, String locale) {
        // https://cdn.contentful.com/spaces/{spaceId}/entries?access_token={accessToken}&locale={locale}&content_type=eventsList&select={fields}
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("access_token", accessToken)
                .queryParam("locale", locale)
                .queryParam("content_type", "eventsList")
                .queryParam("select", "fields.eventName")
                .queryParam("limit", 1000);
        URI uri = builder
                .buildAndExpand(spaceId, "entries")
                .encode()
                .toUri();
        ContentfulEventTypeResponse response = restTemplate.getForObject(uri, ContentfulEventTypeResponse.class);

        return Objects.requireNonNull(response)
                .getItems().stream()
                .map(et -> new EventType(
                        0L,
                        Collections.singletonList(new LocaleItem(
                                transformLocale(locale),
                                getSafeTrimmedString(et.getFields().getEventName()))),
                        null,
                        Collections.emptyList()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Gets events
     *
     * @param spaceId     space identifier
     * @param accessToken access token
     * @param locale      locale
     * @return events
     */
    private static List<Event> getEvents(String spaceId, String accessToken, String locale) {
        // https://cdn.contentful.com/spaces/{spaceId}/entries?access_token={accessToken}&locale={locale}&content_type=eventsCalendar&select={fields}
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("access_token", accessToken)
                .queryParam("locale", locale)
                .queryParam("content_type", "eventsCalendar")
                .queryParam("select", "fields.conferenceName")
                .queryParam("limit", 1000);
        URI uri = builder
                .buildAndExpand(spaceId, "entries")
                .encode()
                .toUri();
        ContentfulEventResponse response = restTemplate.getForObject(uri, ContentfulEventResponse.class);

        return Objects.requireNonNull(response)
                .getItems().stream()
                .map(e -> new Event(
                        null,
                        Collections.singletonList(new LocaleItem(
                                transformLocale(locale),
                                getSafeTrimmedString(e.getFields().getConferenceName()))),
                        null,
                        null,
                        null,
                        null,
                        new ArrayList<>()))
                .collect(Collectors.toList());
    }

    /**
     * Gets speakers.
     *
     * @param spaceId              space identifier
     * @param accessToken          access token
     * @param speakerFieldName     speaker flag field name
     * @param additionalFieldNames additional field names
     * @param conferenceCode       conference code
     * @return speaker map
     */
    private static Map<String, Speaker> getSpeakers(String spaceId, String accessToken, String speakerFieldName, String additionalFieldNames, String conferenceCode) {
        // https://cdn.contentful.com/spaces/{spaceId}/entries?access_token={accessToken}&content_type=people&select={fields}&{speakerFieldName}=true&limit=1000
        StringBuilder selectingFields = new StringBuilder("sys.id,fields.name,fields.nameEn,fields.company,fields.companyEn,fields.bio,fields.bioEn,fields.sdSpeaker,fields.twitter,fields.gitHub");

        if ((additionalFieldNames != null) && !additionalFieldNames.isEmpty()) {
            selectingFields.append(",").append(additionalFieldNames);
        }

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("access_token", accessToken)
                .queryParam("content_type", "people")
                .queryParam("select", selectingFields.toString())
                .queryParam(speakerFieldName, "true")   // only speakers
                .queryParam("limit", 1000);

        if ((conferenceCode != null) && !conferenceCode.isEmpty()) {
            builder.queryParam("fields.conferences", conferenceCode);
        }

        URI uri = builder
                .buildAndExpand(spaceId, "entries")
                .encode()
                .toUri();
        ContentfulSpeakerResponse response = restTemplate.getForObject(uri, ContentfulSpeakerResponse.class);
        AtomicLong id = new AtomicLong();

        return Objects.requireNonNull(response)
                .getItems().stream()
                .filter(s -> (s.getFields().getSdSpeaker() == null) || !s.getFields().getSdSpeaker())   // not demo stage
                .collect(Collectors.toMap(
                        s -> s.getSys().getId(),
                        s -> new Speaker(
                                id.getAndIncrement(),
                                null,
                                Arrays.asList(
                                        new LocaleItem(
                                                Language.ENGLISH.getCode(),
                                                getSafeTrimmedString(s.getFields().getNameEn())),
                                        new LocaleItem(
                                                Language.RUSSIAN.getCode(),
                                                getSafeTrimmedString(s.getFields().getName()))),
                                Arrays.asList(
                                        new LocaleItem(
                                                Language.ENGLISH.getCode(),
                                                getSafeTrimmedString(s.getFields().getCompanyEn())),
                                        new LocaleItem(
                                                Language.RUSSIAN.getCode(),
                                                getSafeTrimmedString(s.getFields().getCompany()))),
                                Arrays.asList(
                                        new LocaleItem(
                                                Language.ENGLISH.getCode(),
                                                getSafeTrimmedString(s.getFields().getBioEn())),
                                        new LocaleItem(
                                                Language.RUSSIAN.getCode(),
                                                getSafeTrimmedString(s.getFields().getBio()))),
                                getSafeTrimmedString(s.getFields().getTwitter()),
                                getSafeCleanedGitHub(s.getFields().getGitHub()),
                                (s.getFields().getJavaChampion() != null) ? s.getFields().getJavaChampion() : false,
                                (s.getFields().getMvp() != null) ? s.getFields().getMvp() : false
                        )
                ));
    }

    /**
     * Gets speakers.
     *
     * @param conference     conference
     * @param conferenceCode conference code
     * @return speaker map
     */
    public static Map<String, Speaker> getSpeakers(Conference conference, String conferenceCode) {
        ConferenceSpaceInfo conferenceSpaceInfo = CONFERENCE_SPACE_INFO_MAP.get(conference);

        return getSpeakers(conferenceSpaceInfo.spaceId, conferenceSpaceInfo.accessToken, conferenceSpaceInfo.speakerFlagFieldName,
                conferenceSpaceInfo.speakerAdditionalFieldNames, conferenceCode);
    }

    /**
     * Gets talks.
     *
     * @param spaceId        space identifier
     * @param accessToken    access token
     * @param conferenceCode conference code
     * @param speakerMap     speaker map
     * @return talks
     */
    private static List<Talk> getTalks(String spaceId, String accessToken, String conferenceCode, Map<String, Speaker> speakerMap) {
        // https://cdn.contentful.com/spaces/{spaceId}/entries?access_token={accessToken}&content_type=talks&select={fields}&order={fields}&limit=1000&fields.conferences={conferenceCode}
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("access_token", accessToken)
                .queryParam("content_type", "talks")
                .queryParam("select", "fields.name,fields.nameEn,fields.short,fields.shortEn,fields.long,fields.longEn,fields.speakers,fields.video,fields.sdTrack,fields.demoStage")
                .queryParam("order", "fields.talkDay,fields.trackTime,fields.track")
                .queryParam("limit", 1000);

        if ((conferenceCode != null) && !conferenceCode.isEmpty()) {
            builder.queryParam("fields.conferences", conferenceCode);
        }

        URI uri = builder
                .buildAndExpand(spaceId, "entries")
                .encode()
                .toUri();
        ContentfulTalkResponse response = restTemplate.getForObject(uri, ContentfulTalkResponse.class);

        return Objects.requireNonNull(response)
                .getItems().stream()
                .filter(t -> ((t.getFields().getSdTrack() == null) || !t.getFields().getSdTrack()) &&
                        ((t.getFields().getDemoStage() == null) || !t.getFields().getDemoStage()))   // not demo stage
                .map(t -> {
                    List<Speaker> speakers = (speakerMap != null) ?
                            t.getFields().getSpeakers().stream()
                                    .map(s -> {
                                        String speakerId = s.getSys().getId();
                                        Speaker speaker = speakerMap.get(speakerId);
                                        return Objects.requireNonNull(speaker,
                                                () -> String.format("Speaker id %s not found", speakerId));
                                    })
                                    .collect(Collectors.toList()) :
                            Collections.emptyList();

                    return new Talk(
                            0L,
                            Arrays.asList(
                                    new LocaleItem(
                                            Language.ENGLISH.getCode(),
                                            getSafeTrimmedString(t.getFields().getNameEn())),
                                    new LocaleItem(
                                            Language.RUSSIAN.getCode(),
                                            getSafeTrimmedString(t.getFields().getName()))),
                            Arrays.asList(
                                    new LocaleItem(
                                            Language.ENGLISH.getCode(),
                                            getSafeTrimmedString(t.getFields().getShortEn())),
                                    new LocaleItem(
                                            Language.RUSSIAN.getCode(),
                                            getSafeTrimmedString(t.getFields().getShortRu()))),
                            Arrays.asList(
                                    new LocaleItem(
                                            Language.ENGLISH.getCode(),
                                            getSafeTrimmedString(t.getFields().getLongEn())),
                                    new LocaleItem(
                                            Language.RUSSIAN.getCode(),
                                            getSafeTrimmedString(t.getFields().getLongRu()))),
                            t.getFields().getVideo(),
                            speakers);
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets talks
     *
     * @param conference     conference
     * @param conferenceCode conference code
     * @param speakerMap     speaker map
     * @return talks
     */
    public static List<Talk> getTalks(Conference conference, String conferenceCode, Map<String, Speaker> speakerMap) {
        ConferenceSpaceInfo conferenceSpaceInfo = CONFERENCE_SPACE_INFO_MAP.get(conference);

        return getTalks(conferenceSpaceInfo.spaceId, conferenceSpaceInfo.accessToken, conferenceCode, speakerMap);
    }

    /**
     * Transforms locale code.
     *
     * @param locale source locale code
     * @return result locale code
     */
    private static String transformLocale(String locale) {
        return LOCALE_CODE_MAP.getOrDefault(locale, locale);
    }

    /**
     * Gets name, speaker map.
     *
     * @return name, speaker map
     */
    public static Map<String, Speaker> getNameSpeakerMap() {
        Map<String, Speaker> result = new HashMap<>();

        for (ConferenceSpaceInfo conferenceSpaceInfo : ConferenceSpaceInfo.values()) {
            Collection<Speaker> speakers = getSpeakers(
                    conferenceSpaceInfo.spaceId, conferenceSpaceInfo.accessToken, conferenceSpaceInfo.speakerFlagFieldName,
                    conferenceSpaceInfo.speakerAdditionalFieldNames, null).values();

            for (Speaker speaker : speakers) {
                String englishName = LocalizationUtils.getString(speaker.getName(), Language.ENGLISH);
                String russianName = LocalizationUtils.getString(speaker.getName(), Language.RUSSIAN);

                if ((englishName != null) && !englishName.isEmpty() &&
                        (russianName != null) && !russianName.isEmpty()) {
                    result.put(englishName.trim(), speaker);
                }
            }
        }

        return result;
    }

    private static String getSafeTrimmedString(String value) {
        return (value != null) ? value.trim() : value;
    }

    private static String getSafeCleanedGitHub(String value) {
        //TODO: implement
//        log.info("GitHub: {}", value);

        return (value != null) ? value.trim() : value;
    }

    public static void main(String[] args) {
        List<String> locales = getLocales(MAIN_SPACE_ID, MAIN_ACCESS_TOKEN);
        log.info("Locales: {}, {}", locales.size(), locales);

        log.info("Event types");

        for (String locale : locales) {
            List<EventType> eventTypes = getEventTypes(MAIN_SPACE_ID, MAIN_ACCESS_TOKEN, locale);
            log.info("Event types (locale: {}): {}, {}", locale, eventTypes.size(), eventTypes);
        }

        log.info("Events");

        for (String locale : locales) {
            List<Event> events = getEvents(MAIN_SPACE_ID, MAIN_ACCESS_TOKEN, locale);
            log.info("Events (locale: {}): {}, {}", locale, events.size(), events);
        }

        for (ConferenceSpaceInfo conferenceSpaceInfo : ConferenceSpaceInfo.values()) {
            log.info("Conference space info: {}", conferenceSpaceInfo);

            Collection<Speaker> speakers = getSpeakers(
                    conferenceSpaceInfo.spaceId, conferenceSpaceInfo.accessToken, conferenceSpaceInfo.speakerFlagFieldName,
                    conferenceSpaceInfo.speakerAdditionalFieldNames, null).values();
            log.info("Speakers: {}, {}", speakers.size(), speakers);

            List<Talk> talks = getTalks(conferenceSpaceInfo.spaceId, conferenceSpaceInfo.accessToken, null, null);
            log.info("Talks: {}, {}", talks.size(), talks);
        }
    }
}
