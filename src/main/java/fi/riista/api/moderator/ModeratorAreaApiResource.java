package fi.riista.api.moderator;

import fi.riista.feature.moderatorarea.ModeratorAreaCrudFeature;
import fi.riista.feature.moderatorarea.ModeratorAreaDTO;
import fi.riista.feature.moderatorarea.ModeratorAreaExcelFeature;
import fi.riista.feature.moderatorarea.ModeratorAreaListFeature;
import fi.riista.feature.moderatorarea.ModeratorAreaListRequestDTO;
import fi.riista.feature.moderatorarea.ModeratorAreaPrintFeature;
import fi.riista.feature.moderatorarea.ModeratorAreaZoneFeature;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.geojson.FeatureCollection;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Locale;

@RestController
@RequestMapping(value = ModeratorAreaApiResource.RESOURCE_URL)
public class ModeratorAreaApiResource {
    static final String RESOURCE_URL = "/api/v1/moderator/area";

    @Resource
    private ModeratorAreaListFeature moderatorAreaListFeature;

    @Resource
    private ModeratorAreaCrudFeature moderatorAreaCrudFeature;

    @Resource
    private ModeratorAreaZoneFeature moderatorAreaZoneFeature;

    @Resource
    private ModeratorAreaExcelFeature moderatorAreaExcelFeature;

    @Resource
    private ModeratorAreaPrintFeature moderatorAreaPrintFeature;

    @Resource
    private MapPdfRemoteService mapPdfRemoteService;

    @PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public Slice<ModeratorAreaDTO> listPage(final @RequestBody @Validated ModeratorAreaListRequestDTO dto) {
        return moderatorAreaListFeature.slice(dto);
    }

    @GetMapping(value = "/{id:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public ModeratorAreaDTO read(@PathVariable Long id) {
        return moderatorAreaCrudFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ModeratorAreaDTO createArea(@RequestBody @Validated ModeratorAreaDTO dto) {
        return moderatorAreaCrudFeature.create(dto);
    }

    @PutMapping(value = "/{id:\\d+}",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ModeratorAreaDTO updateArea(@PathVariable Long id, @RequestBody @Validated ModeratorAreaDTO dto) {
        dto.setId(id);
        return moderatorAreaCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id:\\d+}")
    public void deleteArea(@PathVariable Long id) {
        moderatorAreaCrudFeature.delete(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/features", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection geoJSON(@PathVariable long id) {
        return moderatorAreaZoneFeature.geoJSON(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/{id:\\d+}/features", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public void updateGeoJSON(@PathVariable long id, @RequestBody @Valid FeatureCollection featureCollection) {
        final long zoneId = moderatorAreaZoneFeature.updateGeoJSON(id, featureCollection);
        moderatorAreaZoneFeature.updateAreaSize(zoneId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/combinedFeatures", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection combinedGeoJSON(@PathVariable long id) {
        return moderatorAreaZoneFeature.combinedGeoJSON(id);
    }

    @GetMapping(value = "/{id:\\d+}/excel/{type}")
    public ModelAndView exportExcel(@PathVariable final long id, @PathVariable final String type) {
        if ("all".equals(type)) {
            return new ModelAndView(moderatorAreaExcelFeature.exportAll(id));

        } else if ("changed".equals(type)) {
            return new ModelAndView(moderatorAreaExcelFeature.exportChanged(id));
        }

        throw new IllegalArgumentException("Unknown excel type: " + type);
    }

    @PostMapping(value = "/{id:\\d+}/print", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> print(@PathVariable final long id, final Locale locale,
                                   @ModelAttribute @Valid final MapPdfParameters dto) {
        return mapPdfRemoteService.renderPdf(dto, () -> moderatorAreaPrintFeature.getModel(id, dto.getOverlay(),
                locale));
    }

}
