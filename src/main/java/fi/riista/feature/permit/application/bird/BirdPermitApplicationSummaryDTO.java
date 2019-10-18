package fi.riista.feature.permit.application.bird;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.DeliveryAddressDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.bird.amount.BirdPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.bird.area.BirdPermitApplicationProtectedAreaDTO;
import fi.riista.feature.permit.application.bird.attachments.BirdPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCauseDTO;
import fi.riista.feature.permit.application.bird.damage.BirdPermitApplicationDamageDTO;
import fi.riista.feature.permit.application.bird.forbidden.BirdPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.bird.forbidden.BirdPermitApplicationForbiddenMethodsSpeciesDTO;
import fi.riista.feature.permit.application.bird.period.BirdPermitApplicationSpeciesPeriodDTO;
import fi.riista.feature.permit.application.bird.population.BirdPermitApplicationSpeciesPopulationDTO;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class BirdPermitApplicationSummaryDTO {

    public BirdPermitApplicationSummaryDTO(final Builder builder) {
        this.id = builder.id;
        this.status = builder.status;
        this.applicationNumber = builder.applicationNumber;
        this.applicationName = builder.applicationName;
        this.huntingYear = builder.huntingYear;
        this.harvestPermitCategory = builder.harvestPermitCategory;
        this.submitDate = builder.submitDate;
        this.contactPerson = builder.contactPerson;
        this.permitHolder = builder.permitHolder;
        this.speciesAmounts = builder.speciesAmounts;
        this.speciesPeriods = builder.speciesPeriods;
        this.damage = builder.damage;
        this.population = builder.population;
        this.areaAttachments = builder.areaAttachments;
        this.otherAttachments = builder.otherAttachments;
        this.permitCause = builder.permitCause;
        this.protectedArea = builder.protectedArea;
        this.forbiddenMethods = builder.forbiddenMethods;
        this.email1 = builder.email1;
        this.email2 = builder.email2;
        this.deliveryByMail = builder.deliveryByMail;
        this.deliveryAddress = builder.deliveryAddress;
        this.decisionLanguage = builder.decisionLanguage;
        this.validityYears = builder.validityYears;
    }

    public static BirdPermitApplicationSummaryDTO create(final @Nonnull HarvestPermitApplication entity,
                                                         final @Nonnull BirdPermitApplication birdPermitApplication) {
        requireNonNull(entity);
        requireNonNull(birdPermitApplication);

        return new Builder()
                .withId(entity.getId())
                .withStatus(entity.getStatus())
                .withApplicationNumber(entity.getApplicationNumber())
                .withApplicationName(entity.getHarvestPermitCategory().getApplicationName().getTranslation(entity.getLocale()))
                .withHuntingYear(entity.getApplicationYear())
                .withCategory(entity.getHarvestPermitCategory())
                .withSubmitDate(DateUtil.toLocalDateTimeNullSafe(entity.getSubmitDate()))
                .withContactPerson(Optional.ofNullable(entity.getContactPerson())
                        .map(PersonContactInfoDTO::create)
                        .orElse(null))
                .withPermitHolder(PermitHolderDTO.createFrom(entity.getPermitHolder()))
                .withSpeciesAmounts(entity.getSpeciesAmounts().stream()
                        .map(BirdPermitApplicationSpeciesAmountDTO::new)
                        .collect(Collectors.toList()))
                .withSpeciesPeriods(entity.getSpeciesAmounts().stream()
                        .map(BirdPermitApplicationSpeciesPeriodDTO::new)
                        .collect(Collectors.toList()))
                .withDamage(entity.getSpeciesAmounts().stream()
                        .map(BirdPermitApplicationDamageDTO::new)
                        .collect(Collectors.toList()))
                .withPopulation(entity.getSpeciesAmounts().stream()
                        .map(BirdPermitApplicationSpeciesPopulationDTO::new)
                        .collect(Collectors.toList()))
                .withAreaAttachments(entity.getAttachments().stream()
                        .filter(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.PROTECTED_AREA)
                        .map(BirdPermitApplicationAttachmentDTO::new)
                        .collect(Collectors.toList()))
                .withOtherAttachments(entity.getAttachments().stream()
                        .filter(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.OTHER)
                        .map(BirdPermitApplicationAttachmentDTO::new)
                        .collect(Collectors.toList()))
                .withPermitCause(Optional.ofNullable(birdPermitApplication.getCause())
                        .map(BirdPermitApplicationCauseDTO::createFrom)
                        .orElse(null))
                .withProtectedArea(Optional.ofNullable(birdPermitApplication.getProtectedArea())
                        .map(BirdPermitApplicationProtectedAreaDTO::createFrom)
                        .orElse(null))
                .withDeviationJustification(Optional.ofNullable(birdPermitApplication.getForbiddenMethods())
                        .map(deviation -> BirdPermitApplicationForbiddenMethodsDTO.createFrom(deviation, entity.getSpeciesAmounts().stream()
                                .map(BirdPermitApplicationForbiddenMethodsSpeciesDTO::new)
                                .collect(Collectors.toList())))
                        .orElse(null))
                .withEmail1(Optional.ofNullable(entity.getEmail1()).orElse(null))
                .withEmail2(Optional.ofNullable(entity.getEmail2()).orElse(null))
                .withDeliveryByMail(Optional.ofNullable(entity.getDeliveryByMail()).orElse(null))
                .withDeliveryAddress(DeliveryAddressDTO.fromNullable(entity.getDeliveryAddress()))
                .withDecisionLanguage(entity.getDecisionLocale().getLanguage())
                .withValidityYears(entity.getValidityYears())
                .build();
    }

    private final long id;
    private final HarvestPermitApplication.Status status;
    private final Integer applicationNumber;
    private final String applicationName;
    private final int huntingYear;
    private final HarvestPermitCategory harvestPermitCategory;
    private final LocalDateTime submitDate;
    private final PersonContactInfoDTO contactPerson;
    private final PermitHolderDTO permitHolder;
    private final List<BirdPermitApplicationSpeciesAmountDTO> speciesAmounts;
    private final List<BirdPermitApplicationSpeciesPeriodDTO> speciesPeriods;
    private final List<BirdPermitApplicationDamageDTO> damage;
    private final List<BirdPermitApplicationSpeciesPopulationDTO> population;
    private final List<BirdPermitApplicationAttachmentDTO> areaAttachments;
    private final List<BirdPermitApplicationAttachmentDTO> otherAttachments;
    private final BirdPermitApplicationCauseDTO permitCause;
    private final BirdPermitApplicationProtectedAreaDTO protectedArea;
    private final BirdPermitApplicationForbiddenMethodsDTO forbiddenMethods;
    private final String email1;
    private final String email2;
    private final Boolean deliveryByMail;
    private final DeliveryAddressDTO deliveryAddress;
    private final String decisionLanguage;
    private final Integer validityYears;

    public long getId() {
        return id;
    }

    public HarvestPermitApplication.Status getStatus() {
        return status;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
    }

    public LocalDateTime getSubmitDate() {
        return submitDate;
    }

    public PersonContactInfoDTO getContactPerson() {
        return contactPerson;
    }

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public List<BirdPermitApplicationSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public List<BirdPermitApplicationSpeciesPeriodDTO> getSpeciesPeriods() {
        return speciesPeriods;
    }

    public List<BirdPermitApplicationDamageDTO> getDamage() {
        return damage;
    }

    public List<BirdPermitApplicationSpeciesPopulationDTO> getPopulation() {
        return population;
    }

    public List<BirdPermitApplicationAttachmentDTO> getAreaAttachments() {
        return areaAttachments;
    }

    public List<BirdPermitApplicationAttachmentDTO> getOtherAttachments() {
        return otherAttachments;
    }

    public BirdPermitApplicationCauseDTO getPermitCause() {
        return permitCause;
    }

    public BirdPermitApplicationProtectedAreaDTO getProtectedArea() {
        return protectedArea;
    }

    public BirdPermitApplicationForbiddenMethodsDTO getForbiddenMethods() {
        return forbiddenMethods;
    }

    public String getEmail1() {
        return email1;
    }

    public String getEmail2() {
        return email2;
    }

    public Boolean getDeliveryByMail() {
        return deliveryByMail;
    }

    public DeliveryAddressDTO getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getDecisionLanguage() {
        return decisionLanguage;
    }

    public Integer getValidityYears() {
        return validityYears;
    }

    public static final class Builder {
        private long id;
        private HarvestPermitApplication.Status status;
        private Integer applicationNumber;
        private String applicationName;
        private int huntingYear;
        private HarvestPermitCategory harvestPermitCategory;
        private LocalDateTime submitDate;
        private PersonContactInfoDTO contactPerson;
        private PermitHolderDTO permitHolder;
        private List<BirdPermitApplicationSpeciesAmountDTO> speciesAmounts;
        private List<BirdPermitApplicationSpeciesPeriodDTO> speciesPeriods;
        private List<BirdPermitApplicationDamageDTO> damage;
        private List<BirdPermitApplicationSpeciesPopulationDTO> population;
        private List<BirdPermitApplicationAttachmentDTO> areaAttachments;
        private List<BirdPermitApplicationAttachmentDTO> otherAttachments;
        private BirdPermitApplicationCauseDTO permitCause;
        private BirdPermitApplicationProtectedAreaDTO protectedArea;
        private BirdPermitApplicationForbiddenMethodsDTO forbiddenMethods;
        private String email1;
        private String email2;
        private Boolean deliveryByMail;
        private DeliveryAddressDTO deliveryAddress;
        private String decisionLanguage;
        private Integer validityYears;

        private Builder() {
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withStatus(HarvestPermitApplication.Status status) {
            this.status = status;
            return this;
        }

        public Builder withApplicationNumber(Integer applicationNumber) {
            this.applicationNumber = applicationNumber;
            return this;
        }

        public Builder withApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder withHuntingYear(int huntingYear) {
            this.huntingYear = huntingYear;
            return this;
        }

        public Builder withCategory(HarvestPermitCategory harvestPermitCategory) {
            this.harvestPermitCategory = harvestPermitCategory;
            return this;
        }

        public Builder withSubmitDate(LocalDateTime submitDate) {
            this.submitDate = submitDate;
            return this;
        }

        public Builder withContactPerson(PersonContactInfoDTO contactPerson) {
            this.contactPerson = contactPerson;
            return this;
        }

        public Builder withPermitHolder(PermitHolderDTO permitHolder) {
            this.permitHolder = permitHolder;
            return this;
        }

        public Builder withSpeciesAmounts(List<BirdPermitApplicationSpeciesAmountDTO> speciesAmounts) {
            this.speciesAmounts = speciesAmounts;
            return this;
        }

        public Builder withSpeciesPeriods(List<BirdPermitApplicationSpeciesPeriodDTO> speciesPeriods) {
            this.speciesPeriods = speciesPeriods;
            return this;
        }

        public Builder withDamage(List<BirdPermitApplicationDamageDTO> damage) {
            this.damage = damage;
            return this;
        }

        public Builder withPopulation(List<BirdPermitApplicationSpeciesPopulationDTO> population) {
            this.population = population;
            return this;
        }

        public Builder withAreaAttachments(List<BirdPermitApplicationAttachmentDTO> areaAttachments) {
            this.areaAttachments = areaAttachments;
            return this;
        }

        public Builder withOtherAttachments(List<BirdPermitApplicationAttachmentDTO> otherAttachments) {
            this.otherAttachments = otherAttachments;
            return this;
        }

        public Builder withPermitCause(BirdPermitApplicationCauseDTO permitCause) {
            this.permitCause = permitCause;
            return this;
        }

        public Builder withProtectedArea(BirdPermitApplicationProtectedAreaDTO protectedArea) {
            this.protectedArea = protectedArea;
            return this;
        }

        public Builder withDeviationJustification(BirdPermitApplicationForbiddenMethodsDTO forbiddenMethods) {
            this.forbiddenMethods = forbiddenMethods;
            return this;
        }

        public Builder withEmail1(String email1) {
            this.email1 = email1;
            return this;
        }

        public Builder withEmail2(String email2) {
            this.email2 = email2;
            return this;
        }

        public Builder withDeliveryByMail(Boolean deliveryByMail) {
            this.deliveryByMail = deliveryByMail;
            return this;
        }

        public Builder withDeliveryAddress(DeliveryAddressDTO deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        public Builder withDecisionLanguage(String language) {
            this.decisionLanguage = language;
            return this;
        }

        public Builder withValidityYears(Integer validityYears) {
            this.validityYears = validityYears;
            return this;
        }

        public BirdPermitApplicationSummaryDTO build() {
            return new BirdPermitApplicationSummaryDTO(this);
        }
    }
}
