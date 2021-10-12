package guess.dto.statistics.olap;

import guess.domain.Language;
import guess.domain.statistics.olap.OlapEntityMetrics;
import guess.domain.statistics.olap.dimension.City;
import guess.util.LocalizationUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OLAP city metrics DTO.
 */
public class OlapCityMetricsDto extends OlapEntityMetricsDto {
    public OlapCityMetricsDto(long id, String name, List<Long> measureValues, Long total) {
        super(id, name, measureValues, total);
    }

    public static OlapCityMetricsDto convertToDto(OlapEntityMetrics<City> cityMetrics, Language language) {
        var city = cityMetrics.getEntity();
        var name = LocalizationUtils.getString(city.getName(), language);

        return new OlapCityMetricsDto(
                city.getId(),
                name,
                cityMetrics.getMeasureValues(),
                cityMetrics.getTotal());
    }

    public static List<OlapCityMetricsDto> convertToDto(List<OlapEntityMetrics<City>> cityMetricsList, Language language) {
        return cityMetricsList.stream()
                .map(cm -> convertToDto(cm, language))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "OlapCityMetricsDto{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                '}';
    }
}
