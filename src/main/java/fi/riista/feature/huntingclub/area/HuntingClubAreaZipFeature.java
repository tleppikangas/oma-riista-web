package fi.riista.feature.huntingclub.area;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.zip.OmaRiistaAreaZip;
import fi.riista.feature.gis.zip.OmaRiistaAreaZipBuilder;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.GISUtils;
import org.geojson.FeatureCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Locale;

import static java.util.Collections.singleton;

@Component
public class HuntingClubAreaZipFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Resource
    private GISZoneRepository zoneRepository;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> exportZip(final long clubAreaId,
                                            final Locale locale) throws IOException {
        final HuntingClubArea huntingClubArea = requireEntityService.requireHuntingClubArea(clubAreaId, EntityPermission.READ);
        final GISZone zone = huntingClubArea.getZone();

        if (zone == null) {
            throw new MissingHuntingClubAreaGeometryException();
        }

        final FeatureCollection featureCollection = zoneRepository.getCombinedFeatures(
                singleton(zone.getId()), GISUtils.SRID.WGS84);

        final OmaRiistaAreaZip areaZip = new OmaRiistaAreaZipBuilder(objectMapper)
                .withGeoJson(featureCollection)
                .withMetadata(huntingClubArea, zone, locale)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(ContentDispositionUtil.header(areaZip.getFilename()))
                .body(areaZip.getData());
    }

}
