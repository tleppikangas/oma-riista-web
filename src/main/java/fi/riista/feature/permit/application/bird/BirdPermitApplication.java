package fi.riista.feature.permit.application.bird;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.bird.area.BirdPermitApplicationProtectedArea;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCause;
import fi.riista.feature.permit.application.bird.forbidden.BirdPermitApplicationForbiddenMethods;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class BirdPermitApplication extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "bird_permit_application_id";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @NotNull
    @JoinColumn(unique = true, nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication harvestPermitApplication;

    @Embedded
    @Valid
    private BirdPermitApplicationProtectedArea protectedArea;

    @Embedded
    @Valid
    private BirdPermitApplicationCause cause;

    @Embedded
    @Valid
    private BirdPermitApplicationForbiddenMethods forbiddenMethods;

    public static BirdPermitApplication create(HarvestPermitApplication application) {
        BirdPermitApplication birdApplication = new BirdPermitApplication();
        birdApplication.setHarvestPermitApplication(application);
        birdApplication.setForbiddenMethods(new BirdPermitApplicationForbiddenMethods());
        birdApplication.setCause(new BirdPermitApplicationCause());
        birdApplication.setProtectedArea(new BirdPermitApplicationProtectedArea());
        return birdApplication;
    }

    @Transient
    public boolean isLimitlessPermitAllowed() {
        return protectedArea != null && !protectedArea.getProtectedAreaType().equals(ProtectedAreaType.OTHER) &&
                cause != null && cause.hasOnlyCausesAllowingLimitlessPermit() &&
                hasOnlyUnprotectedSpecies();
    }

    @Transient
    public boolean hasOnlyUnprotectedSpecies() {
        return getHarvestPermitApplication().getSpeciesAmounts()
                .stream().allMatch(spa -> GameCategory.UNPROTECTED.equals(spa.getGameSpecies().getCategory()));
    }

    // ACCESSORS

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    private Long id;

    public HarvestPermitApplication getHarvestPermitApplication() {
        return harvestPermitApplication;
    }

    public void setHarvestPermitApplication(HarvestPermitApplication harvestPermitApplication) {
        this.harvestPermitApplication = harvestPermitApplication;
    }

    public BirdPermitApplicationProtectedArea getProtectedArea() {
        return protectedArea;
    }

    public void setProtectedArea(BirdPermitApplicationProtectedArea protectedArea) {
        this.protectedArea = protectedArea;
    }

    public BirdPermitApplicationCause getCause() {
        return cause;
    }

    public void setCause(BirdPermitApplicationCause cause) {
        this.cause = cause;
    }

    public BirdPermitApplicationForbiddenMethods getForbiddenMethods() {
        return forbiddenMethods;
    }

    public void setForbiddenMethods(BirdPermitApplicationForbiddenMethods forbiddenMethods) {
        this.forbiddenMethods = forbiddenMethods;
    }
}
