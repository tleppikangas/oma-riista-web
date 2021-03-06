package fi.riista.feature.common.support;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import fi.riista.feature.account.area.union.PersonalAreaUnion;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementSenderType;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenOpsForTest;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadata;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.gamediary.srva.SrvaEventTypeEnum;
import fi.riista.feature.gamediary.srva.SrvaResultEnum;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.method.SrvaMethodEnum;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.allocation.HarvestPermitAllocation;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingMethod;
import fi.riista.feature.huntingclub.hunting.rejection.HarvestRejection;
import fi.riista.feature.huntingclub.hunting.rejection.ObservationRejection;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitation;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImport;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingAreaType;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.SpeciesEstimatedAppearance;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.TrendOfPopulationGrowth;
import fi.riista.feature.organization.AlueellinenRiistaneuvosto;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.feature.organization.ValtakunnallinenRiistaneuvosto;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.calendar.AdditionalCalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.calendar.Venue;
import fi.riista.feature.organization.jht.nomination.OccupationNomination;
import fi.riista.feature.organization.jht.training.JHTTraining;
import fi.riista.feature.organization.lupahallinta.LHOrganisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsTestDataPopulator;
import fi.riista.feature.organization.rhy.annualstats.audit.RhyAnnualStatisticsModeratorUpdateEvent;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import fi.riista.feature.organization.rhy.annualstats.statechange.RhyAnnualStatisticsStateChangeEvent;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.ProtectedAreaType;
import fi.riista.feature.permit.application.bird.area.BirdPermitApplicationProtectedArea;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCause;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartner;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionCompleteStatus;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDelivery;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReason;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.feature.permit.decision.derogation.PermitDecisionProtectedAreaType;
import fi.riista.feature.permit.decision.methods.ForbiddenMethodType;
import fi.riista.feature.permit.decision.methods.PermitDecisionForbiddenMethod;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.feature.permit.invoice.decision.PermitDecisionInvoice;
import fi.riista.feature.permit.invoice.harvest.PermitHarvestInvoice;
import fi.riista.feature.permit.invoice.payment.InvoicePaymentLine;
import fi.riista.feature.push.MobileClientDevice;
import fi.riista.feature.shootingtest.ShootingTestAttempt;
import fi.riista.feature.shootingtest.ShootingTestAttemptResult;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestParticipant;
import fi.riista.feature.shootingtest.ShootingTestType;
import fi.riista.feature.shootingtest.official.ShootingTestOfficial;
import fi.riista.feature.storage.backend.db.PersistentFileContent;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.StorageType;
import fi.riista.integration.common.entity.Integration;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermit;
import fi.riista.integration.mmm.transfer.AccountTransfer;
import fi.riista.integration.mmm.transfer.AccountTransferBatch;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import fi.riista.util.MediaTypeExtras;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberUtils;
import fi.riista.util.Patterns;
import fi.riista.util.ValueGenerator;
import fi.riista.validation.FinnishCreditorReferenceValidator;
import io.vavr.Tuple2;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.data.domain.Persistable;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static fi.riista.feature.gamediary.GameSpecies.isMooseOrDeerRequiringPermitForHunting;
import static fi.riista.util.DateUtil.currentYear;
import static fi.riista.util.DateUtil.huntingYear;
import static fi.riista.util.DateUtil.huntingYearBeginDate;
import static fi.riista.util.DateUtil.huntingYearEndDate;
import static fi.riista.util.DateUtil.localDateTime;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.DateUtil.today;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * This class can be used to create @Entity annotated objects or graphs for test code.
 */
public class EntitySupplier implements RhyAnnualStatisticsTestDataPopulator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(Patterns.DATE_YYYYMMDD);

    private static final int DEFAULT_PERMIT_AREA_SIZE = 34567;

    private final NumberGenerator numberGenerator;
    private final List<Persistable<?>> transientEntityList;
    private final Supplier<Riistakeskus> riistakeskusSupplier;

    public EntitySupplier(@Nonnull final NumberGenerator numberGenerator,
                          @Nonnull final List<Persistable<?>> transientEntityList,
                          @Nonnull final Supplier<Riistakeskus> riistakeskusSupplier) {

        this.numberGenerator = requireNonNull(numberGenerator, "numberGenerator is null");
        this.transientEntityList = requireNonNull(transientEntityList, "transientEntityList is null");
        this.riistakeskusSupplier = requireNonNull(riistakeskusSupplier, "riistakeskusSupplier is null");
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return numberGenerator;
    }

    public SystemUser newUser(final String username, final PasswordEncoder passwordEncoder) {
        return newUser(username, SystemUser.Role.ROLE_USER, passwordEncoder);
    }

    public SystemUser newUser(SystemUser.Role role, PasswordEncoder passwordEncoder) {
        return newUser(generateUsername(role), role, passwordEncoder);
    }

    public SystemUser newUser(String username, SystemUser.Role role, PasswordEncoder passwordEncoder) {
        return newUser(username, String.format("%12d", serial()), role, passwordEncoder);
    }

    public SystemUser newUser(String username, String password, SystemUser.Role role, PasswordEncoder passwordEncoder) {
        SystemUser user = new SystemUser();
        user.setUsername(username);
        user.setEmail(username + "@invalid");
        user.setRole(role);
        user.setActive(true);
        user.setFirstName(String.format("%s-%s", "FirstName", serial()));
        user.setLastName(String.format("%s-%s", "LastName", serial()));

        if (passwordEncoder != null) {
            user.setPasswordAsPlaintext(password, passwordEncoder);
        }

        return add(user);
    }

    public SystemUser newUser(Person person) {
        SystemUser user = newUser(SystemUser.Role.ROLE_USER, null);
        user.setPerson(person);
        return user;
    }

    public Person newPerson() {
        String nameNum = zeroPaddedNumber(3);

        return newPerson("FirstName-" + nameNum, "LastName-" + nameNum, ssn(), hunterNumber());
    }

    public Person newPerson(String firstName, String lastName, String ssn, String hunterNumber) {
        Person person = new Person();
        person.setSsn(ssn);
        person.setFirstName(firstName);
        person.setByName(firstName);
        person.setLastName(lastName);
        person.setLanguageCode(Locales.FI_LANG);
        person.setEmail(ValueGenerator.email(numberGenerator, person.getByName(), person.getLastName()));
        person.setPhoneNumber(phoneNumber());
        person.setHunterNumber(hunterNumber);
        person.setLhPersonId(lupaHallintaId());

        final LocalDate today = today();
        person.setHunterExamDate(today.minusYears(2).minusDays(10));
        person.setHuntingCardStart(today.minusYears(2));
        person.setHuntingCardEnd(today.plusYears(1));

        return add(person);
    }

    public Person newPersonWithAddress() {
        final Person person = newPerson();
        person.setMrAddress(newAddress());
        return person;
    }

    public Person newForeignPerson() {
        int serial = serial();

        Person person = new Person();
        person.setFirstName("FirstName-" + serial);
        person.setLastName("LastName-" + serial);
        person.setByName(person.getFirstName());
        person.setHunterNumber(hunterNumber());
        person.setDateOfBirth(new LocalDate(1950, 1, 1).plusDays(serial % (50 * 365)));
        return add(person);
    }

    public Person newForeignPerson(final String firstName, final String lastName, final LocalDate dob,
                                   final String hunterNumber) {
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setByName(person.getFirstName());
        person.setHunterNumber(hunterNumber);
        person.setDateOfBirth(dob);
        return add(person);
    }

    public Address newAddress() {
        int nameNum = serial();
        return newAddress("street " + nameNum, "city " + nameNum, "country" + nameNum);
    }

    public Municipality newMunicipality() {
        int nameNum = serial();
        return add(new Municipality(zeroPaddedNumber(3), "KuntaFI" + nameNum, "KuntaSV" + nameNum));
    }

    public Address newAddress(String street, String city, String country) {
        return newAddress(street, postalCode(), city, country);
    }

    public Address newAddress(String street, String postalCode, String city, String country) {
        return add(new Address(street, postalCode, city, country));
    }

    public ValtakunnallinenRiistaneuvosto newValtakunnallinenRiistaneuvosto(Riistakeskus rk, String nameFi,
                                                                            String nameSv) {
        return add(new ValtakunnallinenRiistaneuvosto(rk, nameFi, nameSv));
    }

    public RiistakeskuksenAlue newRiistakeskuksenAlue() {
        return newRiistakeskuksenAlue(zeroPaddedNumber(3));
    }

    public RiistakeskuksenAlue newRiistakeskuksenAlue(String officialCode) {
        return newRiistakeskuksenAlue(
                riistakeskusSupplier.get(),
                officialCode,
                "RiistakeskuksenAlueFI-" + officialCode,
                "RiistakeskuksenAlueSV-" + officialCode);
    }

    public RiistakeskuksenAlue newRiistakeskuksenAlue(
            Riistakeskus rk, String officialCode, String nameFi, String nameSv) {

        final RiistakeskuksenAlue rka = new RiistakeskuksenAlue(rk, nameFi, nameSv, officialCode);
        rka.setEmail(nameFi + "@invalid");
        return add(rka);
    }

    public AlueellinenRiistaneuvosto newAlueellinenRiistaneuvosto(
            RiistakeskuksenAlue rkAlue, String nameFi, String nameSv) {

        return add(new AlueellinenRiistaneuvosto(rkAlue, nameFi, nameSv));
    }

    public Riistanhoitoyhdistys newRiistanhoitoyhdistys(RiistakeskuksenAlue rka) {
        return newRiistanhoitoyhdistys(rka, zeroPaddedNumber(3));
    }

    public Riistanhoitoyhdistys newRiistanhoitoyhdistys(RiistakeskuksenAlue rka, String officialCode) {
        return newRiistanhoitoyhdistys(
                rka, officialCode, "RiistanhoitoyhdistysFI-" + officialCode, "RiistanhoitoyhdistysSV-" + officialCode);
    }

    public Riistanhoitoyhdistys newRiistanhoitoyhdistys(
            RiistakeskuksenAlue rka, String officialCode, String nameFi, String nameSv) {

        return add(new Riistanhoitoyhdistys(rka, nameFi, nameSv, officialCode));
    }

    public Riistanhoitoyhdistys newRiistanhoitoyhdistys() {
        return newRiistanhoitoyhdistys(newRiistakeskuksenAlue());
    }

    public Occupation newOccupation(Organisation org, Person person) {
        return newOccupation(org, person, some(OccupationType.getApplicableTypes(org.getOrganisationType())));
    }

    public Occupation newOccupation(Organisation org, Person person, OccupationType type) {
        return newOccupation(org, person, type, null, null);
    }

    public Occupation newOccupation(
            Organisation org, Person person, OccupationType type, LocalDate beginDate, LocalDate endDate) {

        Occupation occupation = new Occupation(person, org, type);
        occupation.setBeginDate(beginDate);
        occupation.setEndDate(endDate);
        return add(occupation);
    }

    public OccupationNomination newOccupationNomination(
            Riistanhoitoyhdistys rhy, OccupationType occupationType, Person person, Person rhyPerson) {
        final OccupationNomination occupationNomination = new OccupationNomination();
        occupationNomination.setNominationStatus(OccupationNomination.NominationStatus.EHDOLLA);
        occupationNomination.setOccupationType(occupationType);
        occupationNomination.setPerson(person);
        occupationNomination.setRhy(rhy);
        occupationNomination.setRhyPerson(rhyPerson);

        return add(occupationNomination);
    }

    public Occupation newDeletedOccupation(Organisation org, Person person, OccupationType type) {
        Occupation occupation = add(new Occupation(person, org, type));
        occupation.softDelete();
        return occupation;
    }

    public GameSpecies newGameSpecies() {
        int num = serial();
        return newGameSpecies(
                num, some(GameCategory.class), "SpeciesNameFi-" + num, "SpeciesNameSv-" + num, "SpeciesNameEn-" + num);
    }

    public GameSpecies newGameSpecies(int officialCode) {
        GameSpecies species = newGameSpecies(false);
        species.setOfficialCode(officialCode);
        return species;
    }

    public GameSpecies newGameSpecies(boolean multipleSpecimenAllowedOnHarvest) {
        GameSpecies species = newGameSpecies();
        species.setMultipleSpecimenAllowedOnHarvest(multipleSpecimenAllowedOnHarvest);
        return species;
    }

    public GameSpecies newGameSpecies(
            int officialCode, GameCategory category, String nameFi, String nameSv, String nameEn) {

        return add(new GameSpecies(officialCode, category, nameFi, nameSv, nameEn));
    }

    public GameSpecies newGameSpeciesMoose() {
        final GameSpecies species =
                newGameSpecies(GameSpecies.OFFICIAL_CODE_MOOSE, GameCategory.GAME_MAMMAL, "hirvi", "älg", "moose");
        species.setMultipleSpecimenAllowedOnHarvest(false);
        return species;
    }

    public GameSpecies newDeerSubjectToClubHunting() {
        final GameSpecies species = newGameSpecies(
                GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER,
                GameCategory.GAME_MAMMAL,
                "valkohäntäpeura",
                "vitsvanshjort",
                "white-tailed deer");
        species.setMultipleSpecimenAllowedOnHarvest(false);
        return species;
    }

    public GameSpecies newGameSpeciesNotSubjectToClubHunting() {
        return newGameSpecies(GameSpecies.OFFICIAL_CODE_ROE_DEER);
    }

    public HarvestPermit newHarvestPermit() {
        return newHarvestPermit(newRiistanhoitoyhdistys());
    }

    public HarvestPermit newHarvestPermit(String permitNumber) {
        return newHarvestPermit(newRiistanhoitoyhdistys(), permitNumber);
    }

    public HarvestPermit newHarvestPermit(Riistanhoitoyhdistys rhy) {
        return newHarvestPermit(rhy, permitNumber());
    }

    public HarvestPermit newHarvestPermit(final Riistanhoitoyhdistys rhy, final String permitNumber) {
        final HarvestPermit permit = HarvestPermit.create(permitNumber);
        final Person person = newPersonWithAddress();
        permit.setRhy(rhy);
        permit.setOriginalContactPerson(person);
        permit.setPermitHolder(PermitHolder.createHolderForPerson(person));
        permit.setPermitTypeCode("200");
        permit.setPermitType("testPermitType " + permit.getPermitTypeCode());

        return add(permit);
    }

    public HarvestPermit newHarvestPermit(Person contactPerson) {
        return newHarvestPermit(newRiistanhoitoyhdistys(), contactPerson);
    }

    public HarvestPermit newHarvestPermit(Riistanhoitoyhdistys rhy, Person contactPerson) {
        return newHarvestPermit(rhy, contactPerson, false);
    }

    public HarvestPermit newHarvestPermit(Person contactPerson, boolean harvestsAsList) {
        return newHarvestPermit(newRiistanhoitoyhdistys(), contactPerson, harvestsAsList);
    }

    public HarvestPermit newHarvestPermit(Riistanhoitoyhdistys rhy, Person contactPerson, boolean harvestsAsList) {
        HarvestPermit permit = newHarvestPermit(rhy);
        permit.setHarvestsAsList(harvestsAsList);
        add(permit);

        newHarvestPermitContactPerson(permit, contactPerson);

        return permit;
    }

    public HarvestPermit newHarvestPermit(boolean harvestsAsList) {
        return newHarvestPermit(newRiistanhoitoyhdistys(), harvestsAsList);
    }

    public HarvestPermit newHarvestPermit(Riistanhoitoyhdistys rhy, boolean harvestsAsList) {
        HarvestPermit permit = newHarvestPermit(rhy);
        permit.setHarvestsAsList(harvestsAsList);
        return permit;
    }

    public HarvestPermit newHarvestPermit(HarvestPermit originalPermit) {
        HarvestPermit permit = newHarvestPermit(originalPermit.getRhy());
        permit.setOriginalPermit(originalPermit);
        return permit;
    }

    public HarvestPermit newHarvestPermit(HarvestPermit originalPermit, Person contactPerson) {
        return newHarvestPermit(originalPermit, contactPerson, false);
    }

    public HarvestPermit newHarvestPermit(HarvestPermit originalPermit, Person contactPerson, boolean harvestsAsList) {
        HarvestPermit permit = newHarvestPermit(originalPermit.getRhy(), contactPerson);
        permit.setOriginalPermit(originalPermit);
        permit.setPermitTypeCode(PermitTypeCode.MOOSELIKE_AMENDMENT);
        permit.setHarvestsAsList(harvestsAsList);
        return permit;
    }

    public HarvestPermit newMooselikePermit(final Riistanhoitoyhdistys rhy) {
        return newMooselikePermit(rhy, currentYear());
    }

    public HarvestPermit newMooselikePermit(final Riistanhoitoyhdistys rhy, final int huntingYear) {
        final HarvestPermit permit = newHarvestPermit(rhy, permitNumber(huntingYear));
        permit.setPermitAreaSize(DEFAULT_PERMIT_AREA_SIZE);
        permit.setPermitTypeCode(PermitTypeCode.MOOSELIKE);
        return permit;
    }

    public HarvestPermit newMooselikePermit(final PermitDecision decision) {
        final Person contactPerson = Optional
                .ofNullable(decision.getContactPerson())
                .filter(person -> person.getAddress() != null)
                .orElseGet(this::newPersonWithAddress);

        final HarvestPermit permit = newMooselikePermit(decision.getRhy(), decision.getDecisionYear());
        permit.setPermitDecision(decision);
        permit.setOriginalContactPerson(contactPerson);

        return permit;
    }

    public HarvestPermit newHarvestPermitForHuntingGroup(HuntingClubGroup group) {
        final Organisation rhy = group.getParentOrganisation().getParentOrganisation();
        final HarvestPermit harvestPermit = newHarvestPermit((Riistanhoitoyhdistys) rhy);
        newHarvestPermitSpeciesAmount(harvestPermit, group.getSpecies());
        group.updateHarvestPermit(harvestPermit);
        return harvestPermit;
    }

    public HarvestPermitContactPerson newHarvestPermitContactPerson(HarvestPermit permit, Person person) {
        return add(new HarvestPermitContactPerson(permit, person));
    }

    public HarvestPermitSpeciesAmount newHarvestPermitSpeciesAmount(HarvestPermit permit, GameSpecies species) {
        return newHarvestPermitSpeciesAmount(permit, species, 1f);
    }

    public HarvestPermitSpeciesAmount newHarvestPermitSpeciesAmount(HarvestPermit permit) {
        return newHarvestPermitSpeciesAmount(permit, newGameSpecies(), permit.getPermitYear());
    }

    public HarvestPermitSpeciesAmount newHarvestPermitSpeciesAmount(int huntingYear) {
        return newHarvestPermitSpeciesAmount(newHarvestPermit(), newGameSpecies(), huntingYear);
    }

    public HarvestPermitSpeciesAmount newHarvestPermitSpeciesAmount(
            HarvestPermit permit, GameSpecies species, int huntingYear) {

        return newHarvestPermitSpeciesAmount(permit, species, huntingYear, 1f);
    }

    public HarvestPermitSpeciesAmount newHarvestPermitSpeciesAmount(
            HarvestPermit permit, GameSpecies species, float amount) {

        return newHarvestPermitSpeciesAmount(permit, species, permit.getPermitYear(), amount);
    }

    public HarvestPermitSpeciesAmount newHarvestPermitSpeciesAmount(
            HarvestPermit permit, GameSpecies species, int huntingYear, float amount) {

        HarvestPermitSpeciesAmount speciesAmount = new HarvestPermitSpeciesAmount();
        speciesAmount.setHarvestPermit(permit);
        speciesAmount.setGameSpecies(species);
        speciesAmount.setBeginDate(huntingYearBeginDate(huntingYear));
        speciesAmount.setEndDate(huntingYearEndDate(huntingYear));
        speciesAmount.setAmount(amount);
        return add(speciesAmount);
    }

    public HarvestPermitSpeciesAmount newHarvestPermitSpeciesAmount(final HarvestPermit permit,
                                                                    final PermitDecisionSpeciesAmount source) {

        final HarvestPermitSpeciesAmount speciesAmount = newHarvestPermitSpeciesAmount(
                permit, source.getGameSpecies(), source.resolveHuntingYear(), source.getAmount());
        speciesAmount.setBeginDate(source.getBeginDate());
        speciesAmount.setEndDate(source.getEndDate());
        speciesAmount.setBeginDate2(source.getBeginDate2());
        speciesAmount.setEndDate2(source.getEndDate2());
        return speciesAmount;
    }

    public HarvestPermitAllocation newHarvestPermitAllocation(
            HarvestPermit permit, GameSpecies species, HuntingClub club) {

        HarvestPermitAllocation allocation = new HarvestPermitAllocation();
        allocation.setHarvestPermit(permit);
        allocation.setGameSpecies(species);
        allocation.setHuntingClub(club);
        allocation.setAdultMales(nextPositiveIntAtMost(50));
        allocation.setAdultFemales(nextPositiveIntAtMost(50));
        allocation.setYoung(nextPositiveIntAtMost(50));
        allocation.setTotal(
                allocation.getAdultMales().floatValue()
                        + allocation.getAdultFemales().floatValue()
                        + 0.5f * allocation.getYoung().floatValue());
        return add(allocation);
    }

    /**
     * PERSIST BEFORE CALLING THIS!
     * <p>
     * If you do not persist before calling this, then persist will fail to:<br>
     * Referential integrity constraint violation: "FK_MOOSE_HUNTING_SUMMARY_HARVEST_PERMIT_PARTNERS: PUBLIC
     * .MOOSE_HUNTING_SUMMARY FOREIGN KEY(HARVEST_PERMIT_ID, CLUB_ID) REFERENCES PUBLIC.HARVEST_PERMIT_PARTNERS
     * (HARVEST_PERMIT_ID, ORGANISATION_ID)
     * </p>
     * <p>moose_hunting_summary table has foreign key to harvest_permit_partners table.
     * Hibernate has no knowledge of this, therefore it will arrange flushing in a way that moose_hunting_summary is
     * inserted before harvest_permit_partners table is populated.</p>
     * <p>Fix would be to create HarvestPermitPartner entity referencing to permit and club.</p>
     *
     * @param permit
     * @param club
     * @param huntingFinished
     * @return
     */
    public MooseHuntingSummary newMooseHuntingSummary(HarvestPermit permit, HuntingClub club, boolean huntingFinished) {
        final MooseHuntingSummary summary = new MooseHuntingSummary(club, permit);

        summary.setAreaSizeAndPopulation(new AreaSizeAndRemainingPopulation()
                .withTotalHuntingArea(DEFAULT_PERMIT_AREA_SIZE)
                .withEffectiveHuntingArea(23456)
                .withRemainingPopulationInTotalArea(789)
                .withRemainingPopulationInEffectiveArea(456));
        summary.setHuntingAreaType(MooseHuntingAreaType.SUMMER_PASTURE);

        summary.setWhiteTailedDeerAppearance(
                new SpeciesEstimatedAppearance(true, TrendOfPopulationGrowth.DECREASED, 123));
        summary.setRoeDeerAppearance(
                new SpeciesEstimatedAppearance(true, TrendOfPopulationGrowth.UNCHANGED, 456));
        summary.setWildForestReindeerAppearance(
                new SpeciesEstimatedAppearance(true, TrendOfPopulationGrowth.INCREASED, 789));
        summary.setFallowDeerAppearance(new SpeciesEstimatedAppearance(false, null, null));

        summary.setNumberOfDrownedMooses(1);
        summary.setNumberOfMoosesKilledByBear(2);
        summary.setNumberOfMoosesKilledByWolf(3);
        summary.setNumberOfMoosesKilledInTrafficAccident(4);
        summary.setNumberOfMoosesKilledByPoaching(5);
        summary.setNumberOfMoosesKilledInRutFight(6);
        summary.setNumberOfStarvedMooses(7);
        summary.setNumberOfMoosesDeceasedByOtherReason(8);
        summary.setCauseOfDeath("abc");

        LocalDate today = today();

        summary.setMooseHeatBeginDate(today.minusMonths(6));
        summary.setMooseHeatEndDate(today.minusMonths(5));
        summary.setMooseFawnBeginDate(today.minusMonths(4));
        summary.setMooseFawnEndDate(today.minusMonths(3));

        summary.setDeerFliesAppeared(true);
        summary.setDateOfFirstDeerFlySeen(today.minusMonths(4));
        summary.setDateOfFirstDeerFlySeen(today.minusMonths(3));
        summary.setTrendOfDeerFlyPopulationGrowth(TrendOfPopulationGrowth.UNCHANGED);
        summary.setNumberOfAdultMoosesHavingFlies(123);
        summary.setNumberOfAdultMoosesHavingFlies(456);

        summary.setHuntingEndDate(today);
        summary.setHuntingFinished(huntingFinished);
        summary.setObservationPolicyAdhered(true);

        return add(summary);
    }

    public BasicClubHuntingSummary newBasicHuntingSummary(
            HarvestPermitSpeciesAmount speciesAmount, HuntingClub club, boolean huntingFinished) {

        final BasicClubHuntingSummary summary = new BasicClubHuntingSummary(club, speciesAmount);

        summary.setHuntingFinished(huntingFinished);
        summary.setHuntingEndDate(today());
        summary.setAreaSizeAndPopulation(new AreaSizeAndRemainingPopulation()
                .withTotalHuntingArea(56789)
                .withEffectiveHuntingArea(45678)
                .withRemainingPopulationInTotalArea(678)
                .withRemainingPopulationInEffectiveArea(345));

        return add(summary);
    }

    public BasicClubHuntingSummary newModeratedBasicHuntingSummary(HarvestPermitSpeciesAmount speciesAmount,
                                                                   HuntingClub club) {

        BasicClubHuntingSummary summary = newBasicHuntingSummary(speciesAmount, club, true);

        summary.doModeratorOverride(
                speciesAmount.getLastDate(),
                new AreaSizeAndRemainingPopulation()
                        .withTotalHuntingArea(45678)
                        .withEffectiveHuntingArea(34567)
                        .withRemainingPopulationInTotalArea(567)
                        .withRemainingPopulationInEffectiveArea(234),
                HasHarvestCountsForPermit.of(98, 87, 76, 65, 54, 43));

        return summary;
    }

    public HarvestPermitArea newHarvestPermitArea() {
        final HarvestPermitArea area = new HarvestPermitArea();
        area.setHuntingYear(huntingYear());
        area.setZone(add(new GISZone()));
        area.setExternalId(externalAreaId());

        return add(area);
    }

    public HarvestPermitAreaPartner newHarvestPermitAreaPartner(HarvestPermitArea area, final HuntingClubArea source) {
        return add(new HarvestPermitAreaPartner(area, source, cloneGISZone(source.getZone())));
    }

    public GISZone cloneGISZone(final GISZone from) {
        final GISZone to = new GISZone();
        to.setSourceType(from.getSourceType());
        to.setExcludedGeom(from.getExcludedGeom());
        to.setComputedAreaSize(from.getComputedAreaSize());
        to.setWaterAreaSize(from.getWaterAreaSize());
        to.setStateLandAreaSize(from.getStateLandAreaSize());
        to.setPrivateLandAreaSize(from.getPrivateLandAreaSize());
        to.setMetsahallitusHirvi(new HashSet<>(from.getMetsahallitusHirvi()));
        return add(to);
    }

    public PermitDecision newPermitDecision(final Riistanhoitoyhdistys rhy) {
        return newPermitDecision(rhy, newGameSpecies());
    }

    public PermitDecision newPermitDecision(final Riistanhoitoyhdistys rhy, final GameSpecies species) {
        return newPermitDecision(rhy, singletonList(species));
    }

    public PermitDecision newPermitDecision(final Riistanhoitoyhdistys rhy, final List<GameSpecies> speciesList) {
        return newPermitDecision(newHarvestPermitApplication(rhy, newHarvestPermitArea(), speciesList));
    }

    public PermitDecision newPermitDecision(final HarvestPermitApplication application) {
        final PermitDecision decision = PermitDecision.createForApplication(application);

        decision.setHuntingClub(application.getHuntingClub());
        decision.setLockedDate(now());
        decision.setPublishDate(now());
        decision.setGrantStatus(PermitDecision.GrantStatus.UNCHANGED);
        decision.setPermitTypeCode(PermitTypeCode.getPermitTypeCode(application.getHarvestPermitCategory(), 1));

        final PermitDecisionDocument doc = new PermitDecisionDocument();
        decision.setDocument(doc);
        doc.setDecision("-");

        final PermitDecisionCompleteStatus complete = new PermitDecisionCompleteStatus();
        decision.setCompleteStatus(complete);

        complete.setAdditionalInfo(true);
        complete.setAdministrativeCourt(true);
        complete.setAppeal(true);
        complete.setApplication(true);
        complete.setApplicationReasoning(true);
        complete.setAttachments(true);
        complete.setDecision(true);
        complete.setDecisionReasoning(true);
        complete.setDelivery(true);
        complete.setExecution(true);
        complete.setLegalAdvice(true);
        complete.setNotificationObligation(true);
        complete.setPayment(true);
        complete.setProcessing(true);
        complete.setRestriction(true);

        decision.setStatusLocked();
        decision.setStatusPublished();

        return add(decision);
    }


    public PermitDecisionDelivery newPermitDecisionDelivery(String name, String email) {

        final PermitDecisionDelivery permitDecisionDelivery = new PermitDecisionDelivery();
        permitDecisionDelivery.setName(name);
        permitDecisionDelivery.setEmail(email);
        add(permitDecisionDelivery);
        return permitDecisionDelivery;

    }

    public DeliveryAddress newDeliveryAddressForPerson(Person person) {
        final DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setRecipient(person.getFullName());
        deliveryAddress.setStreetAddress(person.getAddress().getStreetAddress());
        deliveryAddress.setPostalCode(person.getAddress().getPostalCode());
        deliveryAddress.setCity(person.getAddress().getCity());
        return deliveryAddress;
    }

    public PermitDecisionDerogationReason newPermitDecisionDerogationReason(final PermitDecision decision,
                                                                            final PermitDecisionDerogationReasonType type) {
        final PermitDecisionDerogationReason permitDecisionDerogationReason =
                new PermitDecisionDerogationReason(decision, type);
        add(permitDecisionDerogationReason);
        return permitDecisionDerogationReason;
    }

    public PermitDecisionProtectedAreaType newPermitDecisionProtectedAreaType(final PermitDecision decision,
                                                                              final ProtectedAreaType type) {
        final PermitDecisionProtectedAreaType permitDecisionProtectedAreaType =
                new PermitDecisionProtectedAreaType(decision, type);
        add(permitDecisionProtectedAreaType);
        return permitDecisionProtectedAreaType;
    }

    public PermitDecisionSpeciesAmount newPermitDecisionSpeciesAmount(final PermitDecision decision,
                                                                      final GameSpecies species,
                                                                      final float amount) {

        final PermitDecisionSpeciesAmount speciesAmount = new PermitDecisionSpeciesAmount();
        speciesAmount.setPermitDecision(decision);
        speciesAmount.setGameSpecies(species);
        speciesAmount.setAmount(amount);

        final LocalDate huntingYearBegin = huntingYearBeginDate(huntingYear());
        final LocalDate huntingYearEnd = huntingYearEndDate(huntingYear());

        final LocalDate today = today();
        final LocalDate beginDate = today;
        final LocalDate endDate = today.plusMonths(1);

        speciesAmount.setBeginDate(huntingYearBegin.isBefore(beginDate) ? beginDate : huntingYearBegin);
        speciesAmount.setEndDate(huntingYearEnd.isAfter(endDate) ? endDate : huntingYearEnd);

        return add(speciesAmount);
    }

    public HarvestPermitApplication newHarvestPermitApplication(final Riistanhoitoyhdistys rhy,
                                                                final HarvestPermitArea permitArea,
                                                                final HarvestPermitCategory category) {
        final Person person = newPersonWithAddress();
        final HarvestPermitApplication application = new HarvestPermitApplication();
        application.setLocale(Locales.FI);
        application.setDecisionLocale(Locales.FI);
        application.setHarvestPermitCategory(category);
        application.setRhy(rhy);
        application.setContactPerson(person);
        application.setDeliveryAddress(newDeliveryAddressForPerson(person));
        application.setPermitHolder(PermitHolder.createHolderForPerson(person));
        application.setApplicationNumber(10000 + nextPositiveInt());
        application.setApplicationYear(currentYear());
        application.setArea(permitArea);
        application.setDeliveryByMail(false);
        application.setUuid(UUID.randomUUID());
        application.setSubmitDate(now());
        application.setStatus(HarvestPermitApplication.Status.ACTIVE);

        return add(application);
    }

    public CarnivorePermitApplication newCarnivorePermitApplication(HarvestPermitApplication application) {
        final CarnivorePermitApplication carnivorePermitApplication = CarnivorePermitApplication.create(application);
        carnivorePermitApplication.setAreaSize(1);
        carnivorePermitApplication.setGeoLocation(geoLocation());
        return add(carnivorePermitApplication);
    }

    public BirdPermitApplication newBirdPermitApplication(HarvestPermitApplication application) {
        final BirdPermitApplication birdPermitApplication = BirdPermitApplication.create(application);

        birdPermitApplication.setProtectedArea(newProtectedArea());
        birdPermitApplication.setCause(newPermitCauseInfo());
        return add(birdPermitApplication);
    }

    public BirdPermitApplicationProtectedArea newProtectedArea() {
        final BirdPermitApplicationProtectedArea protectedArea = new BirdPermitApplicationProtectedArea();
        protectedArea.setProtectedAreaType(ProtectedAreaType.BERRY_FARM);
        protectedArea.setGeoLocation(new GeoLocation(123, 234));
        protectedArea.setAreaSize(2000);
        protectedArea.setName("nimi");
        protectedArea.setStreetAddress("Katu 2");
        protectedArea.setPostalCode("12345");
        protectedArea.setCity("Kaupunki");
        protectedArea.setDescriptionOfRights("Omistaja");
        return protectedArea;
    }

    public BirdPermitApplicationCause newPermitCauseInfo() {
        return new BirdPermitApplicationCause();
    }

    public HarvestPermitApplication newHarvestPermitApplication(final Riistanhoitoyhdistys rhy,
                                                                final HarvestPermitArea permitArea,
                                                                final GameSpecies species) {

        return newHarvestPermitApplication(rhy, permitArea, singletonList(species));
    }

    public HarvestPermitApplication newHarvestPermitApplication(final Riistanhoitoyhdistys rhy,
                                                                final HarvestPermitArea permitArea,
                                                                final List<GameSpecies> speciesList) {

        final HarvestPermitApplication application =
                newHarvestPermitApplication(rhy, permitArea, HarvestPermitCategory.MOOSELIKE);

        for (int i = 0; i < speciesList.size(); i++) {
            final float amount = Integer.valueOf(10 + i * 5).floatValue();
            newHarvestPermitApplicationSpeciesAmount(application, speciesList.get(i), amount);
        }

        return application;
    }

    public HarvestPermitApplicationSpeciesAmount newHarvestPermitApplicationSpeciesAmount(final HarvestPermitApplication application,
                                                                                          final GameSpecies species) {

        return newHarvestPermitApplicationSpeciesAmount(application, species, 10.0f);
    }

    public HarvestPermitApplicationSpeciesAmount newHarvestPermitApplicationSpeciesAmount(final HarvestPermitApplication application,
                                                                                          final GameSpecies species,
                                                                                          final float amount) {
        return add(new HarvestPermitApplicationSpeciesAmount(application, species, amount));
    }

    public HarvestPermitApplicationSpeciesAmount newHarvestPermitApplicationSpeciesAmount(final HarvestPermitApplication application,
                                                                                          final GameSpecies species,
                                                                                          final float amount,
                                                                                          final int validityYears) {
        final HarvestPermitApplicationSpeciesAmount harvestPermitApplicationSpeciesAmount =
                new HarvestPermitApplicationSpeciesAmount(application, species, amount);
        harvestPermitApplicationSpeciesAmount.setValidityYears(validityYears);

        for (int i = 0; i < validityYears; ++i) {
            harvestPermitApplicationSpeciesAmount.setBeginDate(new LocalDate(application.getApplicationYear() + i, 1,
                    1));
            harvestPermitApplicationSpeciesAmount.setEndDate(new LocalDate(application.getApplicationYear() + i, 10,
                    1));
        }
        return add(harvestPermitApplicationSpeciesAmount);
    }

    public Harvest newHarvest() {
        return newHarvest(newPerson());
    }

    public Harvest newHarvest(Person author) {
        return newHarvest(newGameSpecies(), author, author, GeoLocation.Source.MANUAL);
    }

    public Harvest newHarvest(Person author, Person hunter) {
        return newHarvest(newGameSpecies(), author, hunter, GeoLocation.Source.MANUAL);
    }

    public Harvest newHarvest(Person author, GeoLocation location) {
        return newHarvest(newGameSpecies(), author, author, location, localDateTime(), 1);
    }

    public Harvest newHarvest(GameSpecies species) {
        Person hunter = newPerson();
        return newHarvest(species, hunter, hunter);
    }

    public Harvest newHarvest(Person author, HarvestPermit permit, GameSpecies species) {
        Harvest harvest = newHarvest(species, author, author);

        if (permit != null) {
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.PROPOSED);
            harvest.setRhy(permit.getRhy());

            permit.addHarvest(harvest);
        }

        return harvest;
    }

    public Harvest newHarvest(HarvestPermit permit, GameSpecies species) {
        Harvest harvest = newHarvest(species);

        if (permit != null) {
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.PROPOSED);
            harvest.setRhy(permit.getRhy());

            permit.addHarvest(harvest);
        }

        return harvest;
    }

    public Harvest newHarvest(GameSpecies species, Person hunter) {
        return newHarvest(species, hunter, hunter);
    }

    public Harvest newHarvest(GameSpecies species, Person author, Person hunter) {
        return newHarvest(species, author, hunter, GeoLocation.Source.MANUAL);
    }

    public Harvest newHarvest(Person hunter, GroupHuntingDay huntingDay) {
        return newHarvest(huntingDay.getGroup().getSpecies(), hunter, huntingDay);
    }

    public Harvest newHarvest(GameSpecies species, Person hunter, GroupHuntingDay huntingDay) {
        return newHarvest(species, hunter, huntingDay, null);
    }

    private Harvest newHarvest(GameSpecies species, Person hunter, GroupHuntingDay huntingDay, Person acceptor) {
        final Harvest harvest = newHarvest(species, hunter, huntingDay.getStartDate());
        harvest.updateHuntingDayOfGroup(huntingDay, acceptor);
        return harvest;
    }

    public Harvest newHarvest(Person hunter, LocalDate pointOfTime) {
        return newHarvest(newGameSpecies(), hunter, pointOfTime);
    }

    public Harvest newHarvest(GameSpecies species, Person hunter, LocalDate pointOfTime) {
        Harvest harvest = newHarvest(species, hunter);
        harvest.setPointOfTime(DateUtil.toDateNullSafe(pointOfTime));
        return harvest;
    }

    public Harvest newHarvest(GameSpecies species, Riistanhoitoyhdistys rhy) {
        Person hunter = newPerson();
        Harvest harvest = newHarvest(species, hunter, hunter);
        harvest.setRhy(rhy);
        return harvest;
    }

    public Harvest newHarvest(GameSpecies species, GameAge age, GameGender gender, Riistanhoitoyhdistys rhy) {
        Person person = newPerson();
        Harvest harvest = newHarvest(species, person, person, some(GeoLocation.Source.class));
        harvest.setRhy(rhy);
        newHarvestSpecimen(harvest, age, gender);
        harvest.setAmount(1);
        return harvest;
    }

    public HarvestChangeHistory newHarvestChangeHistory(final Harvest harvest, final HarvestReportState state,
                                                        final SystemUser approver) {
        final HarvestChangeHistory changeHistory = new HarvestChangeHistory();
        changeHistory.setPointOfTime(now());
        changeHistory.setHarvest(harvest);
        changeHistory.setHarvestReportState(state);
        changeHistory.setUserId(approver.getId());
        add(changeHistory);
        return changeHistory;
    }

    public Harvest newMobileHarvest() {
        return newMobileHarvest(newGameSpecies(), newPerson());
    }

    public Harvest newMobileHarvest(GameSpecies species) {
        return newMobileHarvest(species, newPerson());
    }

    public Harvest newMobileHarvest(Person author) {
        return newMobileHarvest(newGameSpecies(), author);
    }

    public Harvest newMobileHarvest(GameSpecies species, Person author) {
        Harvest harvest = newHarvest(species, author, author, GeoLocation.Source.GPS_DEVICE);
        harvest.setFromMobile(true);
        harvest.setMobileClientRefId((long) serial());
        return harvest;
    }

    public Harvest newMobileHarvest(GameSpecies species, Person hunter, LocalDate pointOfTime) {
        Harvest harvest = newMobileHarvest(species, hunter);
        harvest.setPointOfTime(DateUtil.toDateNullSafe(pointOfTime));
        return harvest;
    }

    public Harvest newMobileHarvest(GameSpecies species, Person author, GroupHuntingDay huntingDay) {
        return newMobileHarvest(species, author, huntingDay, null);
    }

    private Harvest newMobileHarvest(GameSpecies species, Person author, GroupHuntingDay huntingDay, Person acceptor) {
        final Harvest harvest = newMobileHarvest(species, author, huntingDay.getStartDate());
        harvest.updateHuntingDayOfGroup(huntingDay, acceptor);
        return harvest;
    }

    public Harvest newHarvest(GameSpecies species, Person author, Person hunter, GeoLocation.Source source) {
        return newHarvest(species, author, hunter, geoLocation(source), localDateTime(), 1);
    }

    public Harvest newHarvest(GameSpecies species, Person author, Person hunter, GeoLocation geoLocation,
                              LocalDateTime pointOfTime, int amount) {

        Harvest harvest = new Harvest(author, geoLocation, pointOfTime, species, amount);
        if (hunter != null) {
            harvest.setActualShooter(hunter);
        }
        harvest.setFromMobile(false);
        harvest.setDescription("description");
        return add(harvest);
    }

    public HarvestSpecimen newHarvestSpecimen(Harvest harvest) {
        final int gameSpeciesCode = harvest != null && harvest.getSpecies() != null
                ? harvest.getSpecies().getOfficialCode()
                : serial();

        final HarvestSpecimenOpsForTest specimenOps =
                new HarvestSpecimenOpsForTest(gameSpeciesCode, HarvestSpecVersion.MOST_RECENT, getNumberGenerator());

        final HarvestSpecimen specimen = new HarvestSpecimen(harvest);
        specimenOps.mutateContent(specimen, !isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode));
        return add(specimen);
    }

    public HarvestSpecimen newHarvestSpecimen(Harvest harvest, GameAge age, GameGender gender) {
        return newHarvestSpecimen(harvest, age, gender, weight());
    }

    public HarvestSpecimen newHarvestSpecimen(Harvest harvest, GameAge age, GameGender gender, Double weight) {
        return add(new HarvestSpecimen(harvest, age, gender, weight));
    }

    public ObservationSpecimen newObservationSpecimen(Observation obs) {
        return add(new ObservationSpecimen(obs));
    }

    public ObservationSpecimen newObservationSpecimen(Observation obs, ObservationContextSensitiveFields ctxFields) {
        ObservationSpecimen specimen = newObservationSpecimen(obs);

        if (ctxFields.getGender().isNonNullValueLegal()) {
            specimen.setGender(some(GameGender.class));
        }

        if (ctxFields.getAge().isNonNullValueLegal()) {
            specimen.setAge(some(ctxFields.getAllowedGameAges()));
        }

        final EnumSet<ObservedGameState> validStates = ctxFields.getAllowedGameStates();

        if (!validStates.isEmpty()) {
            specimen.setState(some(validStates));
        }

        final EnumSet<GameMarking> validMarkings = ctxFields.getAllowedGameMarkings();

        if (!validMarkings.isEmpty()) {
            specimen.setMarking(some(validMarkings));
        }

        return specimen;
    }

    public ObservationSpecimen newObservationSpecimen(
            Observation obs, ObservationMetadata metadata, boolean carnivoreAuthority) {

        ObservationSpecimen specimen = newObservationSpecimen(obs, metadata.getContextSensitiveFields());

        Tuple2<Double, Double> widthAndLength = metadata.generateWidthAndLengthOfPaw(carnivoreAuthority);
        specimen.setWidthOfPaw(widthAndLength._1);
        specimen.setLengthOfPaw(widthAndLength._2);

        return specimen;
    }

    public GameDiaryImage newGameDiaryImage(Harvest harvest) {
        return newGameDiaryImage(harvest, newPersistentFileContent().getMetadata());
    }

    public GameDiaryImage newGameDiaryImage(Observation observation) {
        return newGameDiaryImage(observation, newPersistentFileContent().getMetadata());
    }

    public GameDiaryImage newGameDiaryImage(SrvaEvent srvaEvent) {
        return add(new GameDiaryImage(srvaEvent, newPersistentFileContent().getMetadata()));
    }

    public GameDiaryImage newGameDiaryImage(Harvest harvest, PersistentFileMetadata metadata) {
        return add(new GameDiaryImage(harvest, metadata));
    }

    public GameDiaryImage newGameDiaryImage(Observation observation, PersistentFileMetadata metadata) {
        return add(new GameDiaryImage(observation, metadata));
    }

    public PersistentFileContent newPersistentFileContent() {
        final PersistentFileMetadata metadata = newPersistentFileMetadata(StorageType.LOCAL_DATABASE);
        return add(new PersistentFileContent(metadata, "xyzzy".getBytes()));
    }

    public PersistentFileMetadata newPersistentFileMetadata(StorageType storageType) {
        final PersistentFileMetadata pfm = new PersistentFileMetadata();
        pfm.setId(UUID.randomUUID());
        pfm.setStorageType(storageType);
        pfm.setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        return add(pfm);
    }

    public HarvestArea newHarvestArea(Riistanhoitoyhdistys rhy) {
        String name = "HarvestArea-" + serial();
        return newHarvestArea(HarvestArea.HarvestAreaType.PORONHOITOALUE, name, name, Sets.newHashSet(rhy));
    }

    public HarvestArea newHarvestArea(
            HarvestArea.HarvestAreaType harvestAreaType, String nameFi, String nameSv, Set<Riistanhoitoyhdistys> rhys) {

        return add(new HarvestArea(harvestAreaType, nameFi, nameSv, rhys));
    }

    public HarvestSeason newHarvestSeason(GameSpecies species) {
        final String name = "HarvestSeason-" + serial();
        final LocalDate begin = DateUtil.today();
        return add(new HarvestSeason(name, name, species, begin, begin.plusDays(1), today().plusDays(1)));
    }

    public HarvestSeason newHarvestSeason(GameSpecies species, LocalDate beginDate, LocalDate endDate,
                                          LocalDate endOfReportingDate) {
        String name = "HarvestSeason-" + serial();
        return add(new HarvestSeason(name, name, species, beginDate, endDate, endOfReportingDate));
    }

    public HarvestQuota newHarvestQuota(HarvestSeason harvestSeason, HarvestArea harvestArea, int quota) {
        HarvestQuota q = new HarvestQuota(harvestSeason, harvestArea, quota);
        return add(q);
    }

    public Observation newObservation() {
        return newObservation(newGameSpecies());
    }

    public Observation newObservation(final boolean withinMooseHunting) {
        final Observation observation = newObservation(newGameSpecies());
        observation.setWithinMooseHunting(withinMooseHunting);
        return observation;
    }

    public Observation newObservation(Person observer) {
        return newObservation(newGameSpecies(), observer);
    }

    public Observation newObservation(GameSpecies species) {
        return newObservation(species, newPerson());
    }

    public Observation newObservation(GameSpecies species, Person observer) {
        return newObservation(species, observer, observer);
    }

    public Observation newObservation(GameSpecies species, Person author, Person observer) {
        return newObservation(species, author, observer, geoLocation(GeoLocation.Source.MANUAL), localDateTime(), 1);
    }

    public Observation newObservation(GameSpecies species, Person observer, final boolean withinMooseHunting) {
        final Observation observation = newObservation(species, observer);
        observation.setWithinMooseHunting(withinMooseHunting);
        return observation;
    }

    public Observation newObservation(ObservationMetadata metadata) {
        return newObservation(newPerson(), metadata);
    }

    public Observation newObservation(Person observer, ObservationMetadata metadata) {
        return newObservation(observer, metadata, o -> {
        });
    }

    public Observation newObservation(Person observer, ObservationMetadata metadata, boolean carnivoreAuthority) {
        return newObservation(observer, metadata, o -> decorateObservation(o, metadata, carnivoreAuthority));
    }

    public Observation newObservation(Person observer, GroupHuntingDay huntingDay) {
        return newObservation(huntingDay.getGroup().getSpecies(), observer, huntingDay);
    }

    public Observation newObservation(GameSpecies species, Person observer, GroupHuntingDay huntingDay) {
        return newObservation(species, observer, huntingDay, null);
    }

    private Observation newObservation(GameSpecies species, Person observer, GroupHuntingDay huntingDay,
                                       Person acceptor) {
        final Observation observation = newObservation(species, observer, huntingDay.getStartDate());
        observation.updateHuntingDayOfGroup(huntingDay, acceptor);
        observation.setWithinMooseHunting(true);
        return observation;
    }

    public Observation newObservation(Person observer, LocalDate date) {
        return newObservation(newGameSpecies(), observer, date);
    }

    public Observation newObservation(GameSpecies species, Person observer, LocalDate date) {
        return newObservation(species, observer, observer, geoLocation(GeoLocation.Source.MANUAL),
                date.toLocalDateTime(new LocalTime(9, 0)), 1);
    }

    private Observation newObservation(
            GameSpecies species, Person author, Person observer, GeoLocation geoLocation, LocalDateTime pointOfTime,
            int amount) {

        final Observation observation = new Observation(author, geoLocation, pointOfTime, species, amount);

        // The observation type is selected because it is applicable for all
        // species. With this observation type, the amount field is mandatory
        // but any other additional fields are optional.
        observation.setObservationType(ObservationType.NAKO);

        if (author != observer) {
            observation.setObserver(observer);
        }
        observation.setDescription("description");
        observation.setFromMobile(false);

        return add(observation);
    }

    private Observation newObservation(Person observer, ObservationMetadata metadata, Consumer<Observation> decorator) {
        requireNonNull(metadata);

        final Observation observation = newObservation(metadata.getSpecies(), observer);
        observation.setWithinMooseHunting(metadata.getWithinMooseHunting());
        observation.setObservationType(metadata.getObservationType());
        observation.setAmount(metadata.isAmountRequired() ? 1 : null);

        final ObservationContextSensitiveFields ctxFields = metadata.getContextSensitiveFields();

        if (ctxFields.getMooselikeMaleAmount().isNonNullValueLegal()) {
            observation.setMooselikeMaleAmount(nextPositiveIntAtMost(50));
        }
        if (ctxFields.getMooselikeFemaleAmount().isNonNullValueLegal()) {
            observation.setMooselikeFemaleAmount(nextPositiveIntAtMost(50));
        }
        if (ctxFields.getMooselikeCalfAmount().isNonNullValueLegal()) {
            observation.setMooselikeCalfAmount(nextPositiveIntAtMost(50));
        }
        if (ctxFields.getMooselikeFemale1CalfAmount().isNonNullValueLegal()) {
            observation.setMooselikeFemale1CalfAmount(nextPositiveIntAtMost(50));
        }
        if (ctxFields.getMooselikeFemale2CalfsAmount().isNonNullValueLegal()) {
            observation.setMooselikeFemale2CalfsAmount(nextPositiveIntAtMost(50));
        }
        if (ctxFields.getMooselikeFemale3CalfsAmount().isNonNullValueLegal()) {
            observation.setMooselikeFemale3CalfsAmount(nextPositiveIntAtMost(50));
        }
        if (ctxFields.getMooselikeFemale4CalfsAmount().isNonNullValueLegal()) {
            observation.setMooselikeFemale4CalfsAmount(nextPositiveIntAtMost(50));
        }
        if (ctxFields.getMooselikeUnknownSpecimenAmount().isNonNullValueLegal()) {
            observation.setMooselikeUnknownSpecimenAmount(nextPositiveIntAtMost(50));
        }

        // TODO Currently setting non-null amount contradicts with metadata settings. This will be
        // fixed soon in a subsequent changeset.
        if (observation.hasMinimumSetOfNonnullAmountsCommonToAllMooselikeSpecies()) {
            observation.setAmount(observation.getSumOfMooselikeAmounts());
        }

        decorator.accept(observation);

        return observation;
    }

    public Observation newMobileObservation(Person observer, ObservationMetadata metadata) {
        return newMobileObservation(observer, metadata, o -> {
        });
    }

    public Observation newMobileObservation(Person observer, ObservationMetadata metadata, boolean carnivoreAuthority) {
        return newMobileObservation(observer, metadata, o -> decorateObservation(o, metadata, carnivoreAuthority));
    }

    public Observation newMobileObservation(Person observer, ObservationMetadata metadata, LocalDate date) {
        return newMobileObservation(observer, metadata, o -> {
            o.setPointOfTime(DateUtil.toDateNullSafe(date.toLocalDateTime(new LocalTime(9, 0))));
        });
    }

    public Observation newMobileObservation(Person observer, ObservationMetadata metadata, GroupHuntingDay huntingDay) {
        return newMobileObservation(observer, metadata, huntingDay, null);
    }

    private Observation newMobileObservation(
            Person observer, ObservationMetadata metadata, GroupHuntingDay huntingDay, Person acceptor) {

        final Observation observation = newMobileObservation(observer, metadata, huntingDay.getStartDate());
        observation.updateHuntingDayOfGroup(huntingDay, acceptor);
        return observation;
    }

    private Observation newMobileObservation(
            Person observer, ObservationMetadata metadata, Consumer<Observation> decorator) {

        return newObservation(observer, metadata, decorator.andThen(o -> {
            o.setFromMobile(true);
            o.setMobileClientRefId((long) serial());
            o.getGeoLocation().setSource(GeoLocation.Source.GPS_DEVICE);
        }));
    }

    private void decorateObservation(
            Observation observation, ObservationMetadata metadata, boolean carnivoreAuthority) {

        final ObservationContextSensitiveFields ctxFields = metadata.getContextSensitiveFields();

        if (ctxFields.getVerifiedByCarnivoreAuthority().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal()) {
            observation.setVerifiedByCarnivoreAuthority(someBoolean());
        }
        if (ctxFields.getObserverName().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal()) {
            observation.setObserverName("observerName-" + nextPositiveInt());
        }
        if (ctxFields.getObserverPhoneNumber().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal()) {
            observation.setObserverPhoneNumber(phoneNumber());
        }
        if (ctxFields.getOfficialAdditionalInfo().toSimpleFieldPresence(carnivoreAuthority).isNonNullValueLegal()) {
            observation.setOfficialAdditionalInfo("officialAdditionalInfo-" + nextPositiveInt());
        }
    }

    public HuntingClubMemberInvitation newHuntingClubInvitation(HuntingClub club) {
        return newHuntingClubInvitation(newPerson(), club, OccupationType.SEURAN_JASEN);
    }

    public HuntingClubMemberInvitation newHuntingClubInvitation(
            Person person, HuntingClub club, OccupationType occType) {

        return add(new HuntingClubMemberInvitation(person, club, occType));
    }

    public HuntingClub newHuntingClub() {
        return newHuntingClub(newRiistanhoitoyhdistys());
    }

    public HuntingClub newHuntingClub(Riistanhoitoyhdistys rhy) {
        final String numberRepr = zeroPaddedNumber(6);
        return newHuntingClub(rhy, "Club-" + numberRepr, "ClubSV-" + numberRepr);
    }

    public HuntingClub newHuntingClub(Riistanhoitoyhdistys rhy, String nameFi, String nameSv) {
        return add(new HuntingClub(rhy, nameFi, nameSv, zeroPaddedNumber(7)));
    }

    public Occupation newHuntingClubMember(HuntingClub club, OccupationType type) {
        return newOccupation(club, newPerson(), type);
    }

    public HuntingClubGroup newHuntingClubGroup() {
        return newHuntingClubGroup(newHuntingClub(), newGameSpecies());
    }

    public HuntingClubGroup newHuntingClubGroup(HuntingClub club) {
        return newHuntingClubGroup(club, newGameSpecies());
    }

    public HuntingClubGroup newHuntingClubGroup(GameSpecies species) {
        return newHuntingClubGroup(newHuntingClub(), species);
    }

    public HuntingClubGroup newHuntingClubGroup(HuntingClub club, GameSpecies species) {
        return newHuntingClubGroup(club, species, huntingYear());
    }

    public HuntingClubGroup newHuntingClubGroup(HuntingClub club, HarvestPermitSpeciesAmount speciesAmount) {
        HuntingClubGroup group =
                newHuntingClubGroup(club, speciesAmount.getGameSpecies(), speciesAmount.resolveHuntingYear());
        group.updateHarvestPermit(speciesAmount.getHarvestPermit());
        return group;
    }

    public HuntingClubGroup newHuntingClubGroup(
            HuntingClub club, GameSpecies species, int firstCalendarYearOfHuntingYear) {

        final String suffix = zeroPaddedNumber(6);

        return add(newHuntingClubGroup(
                club, "Group" + suffix, "GroupSV" + suffix, species, firstCalendarYearOfHuntingYear));
    }

    public HuntingClubGroup newHuntingClubGroup(
            HuntingClub club, String nameFi, String nameSv, GameSpecies species, int firstCalendarYearOfHuntingYear) {

        return add(new HuntingClubGroup(club, nameFi, nameSv, species, firstCalendarYearOfHuntingYear));
    }

    public HuntingClubGroup newHuntingClubGroupWithAreaContaining(GeoLocation location) {
        return newHuntingClubGroupWithAreaContaining(location, newHuntingClub(), newGameSpecies());
    }

    public HuntingClubGroup newHuntingClubGroupWithAreaContaining(HuntingClub club, GeoLocation location) {
        return newHuntingClubGroupWithAreaContaining(location, club, newGameSpecies());
    }

    public HuntingClubGroup newHuntingClubGroupWithAreaContaining(GeoLocation location, GameSpecies species) {
        return newHuntingClubGroupWithAreaContaining(location, newHuntingClub(), species);
    }

    public HuntingClubGroup newHuntingClubGroupWithAreaContaining(
            GeoLocation location, HuntingClub club, GameSpecies species) {

        return newHuntingClubGroupWithArea(location, true, club, species);
    }

    public HuntingClubGroup newHuntingClubGroupWithAreaNotContaining(GeoLocation location) {
        return newHuntingClubGroupWithArea(location, false, newHuntingClub(), newGameSpecies());
    }

    public HuntingClubGroup newHuntingClubGroupWithAreaNotContaining(HuntingClub club, GeoLocation location) {
        return newHuntingClubGroupWithArea(location, false, club, newGameSpecies());
    }

    private HuntingClubGroup newHuntingClubGroupWithArea(
            GeoLocation location, boolean areaContainsLocation, HuntingClub club, GameSpecies species) {

        final GISZone zone = areaContainsLocation ? newGISZoneContaining(location) : newGISZoneNotContaining(location);

        final HuntingClubArea area = newHuntingClubArea(club);
        area.setZone(zone);

        final HuntingClubGroup group = newHuntingClubGroup(club, species);
        group.setHuntingArea(area);
        return group;
    }

    public HuntingClubGroup newHuntingClubGroupWithArea(final HuntingClubArea area) {
        final HuntingClubGroup group = newHuntingClubGroup(area.getClub(), newGameSpecies());
        group.setHuntingArea(area);
        return group;
    }

    public Occupation newHuntingClubGroupMember(HuntingClubGroup group, OccupationType type) {
        return newOccupation(group, newPerson(), type);
    }

    public Occupation newHuntingClubGroupMember(Person person, HuntingClubGroup group) {
        return newHuntingClubGroupMember(person, group, OccupationType.RYHMAN_JASEN);
    }

    public Occupation newHuntingClubGroupMember(Person person, HuntingClubGroup group, OccupationType occupationType) {
        return add(new Occupation(person, group, occupationType));
    }

    public HuntingClubArea newHuntingClubArea(HuntingClub club) {
        return newHuntingClubArea(club, "Area", "AreaSV", huntingYear());
    }

    public HuntingClubArea newHuntingClubArea(HuntingClub club, GISZone zone) {
        HuntingClubArea area = newHuntingClubArea(club);
        area.setZone(zone);
        return area;
    }

    public HuntingClubArea newHuntingClubArea(HuntingClub club, String fi, String sv, int year) {
        return add(new HuntingClubArea(club, fi, sv, year, year, UUID.randomUUID().toString()));
    }

    public GroupHuntingDay newGroupHuntingDay(HuntingClubGroup group, LocalDate date) {
        final DateTime startTime = DateUtil.toDateTimeNullSafe(date, new LocalTime(6, 0));
        final DateTime endTime = DateUtil.toDateTimeNullSafe(date, new LocalTime(21, 0));

        GroupHuntingDay huntingDay = new GroupHuntingDay(group, startTime, endTime);
        huntingDay.setHuntingMethod(some(GroupHuntingMethod.class));

        if (huntingDay.getHuntingMethod().isWithHound()) {
            huntingDay.setNumberOfHounds(4);
        }

        return add(huntingDay);
    }

    public HarvestRejection newHarvestRejection(HuntingClubGroup group, Harvest harvest) {
        return add(new HarvestRejection(group, harvest));
    }

    public ObservationRejection newObservationRejection(HuntingClubGroup group, Observation observation) {
        return add(new ObservationRejection(group, observation));
    }

    public MooseDataCardImport newMooseDataCardImport(HuntingClubGroup group) {
        MooseDataCardImport imp = new MooseDataCardImport();
        imp.setGroup(group);
        imp.setXmlFileMetadata(newPersistentFileContent().getMetadata());
        imp.setPdfFileMetadata(newPersistentFileContent().getMetadata());
        imp.setFilenameTimestamp(DateUtil.toDateTodayNullSafe(LocalTime.now()));
        return add(imp);
    }

    public GISZone newGISZone() {
        return add(new GISZone());
    }

    public GISZone newGISZone(double computedAreaSize) {
        final GISZone zone = newGISZone();
        zone.setComputedAreaSize(computedAreaSize);
        zone.setWaterAreaSize(computedAreaSize / 2);
        zone.setPrivateLandAreaSize(computedAreaSize / 3);
        zone.setStateLandAreaSize(computedAreaSize / 4);
        return zone;
    }

    public GISZone newGISZoneContaining(GeoLocation location) {
        GISZone zone = newGISZone();
        zone.setGeom(ValueGenerator.geometryContaining(location));
        return zone;
    }

    public GISZone newGISZoneNotContaining(GeoLocation location) {
        GISZone zone = newGISZone();
        zone.setGeom(ValueGenerator.geometryNotContaining(location));
        return zone;
    }

    public GISHirvitalousalue newGISHirvitalousalue() {
        return newGISHirvitalousalueContaining(new GeoLocation(0, 0));
    }

    public GISHirvitalousalue newGISHirvitalousalueContaining(final GeoLocation geoLocation) {
        final int serial = serial() % 1_000_000;
        final String formattedNumber = htaNumber();

        final GISHirvitalousalue hta =
                new GISHirvitalousalue(formattedNumber,
                        "fooFI" + serial,
                        "foo" + serial,
                        "fooSV" + serial,
                        ValueGenerator.geometryContaining(geoLocation));
        hta.setId(serial);
        return add(hta);
    }

    public Venue newVenue() {
        return newVenue(newAddress());
    }

    public Venue newVenue(Address address) {
        return newVenue("Tapahtumapaikka-" + serial(), address);
    }

    public Venue newVenue(String name, Address address) {
        return add(new Venue(address, name));
    }

    public CalendarEvent newCalendarEvent(CalendarEventType eventType) {
        return newCalendarEvent(newRiistanhoitoyhdistys(), eventType);
    }

    public CalendarEvent newCalendarEvent(Organisation organisation) {
        return newCalendarEvent(organisation, some(CalendarEventType.class));
    }

    public CalendarEvent newCalendarEvent(Organisation organisation, CalendarEventType eventType) {
        return newCalendarEvent(organisation, eventType, today());
    }

    public CalendarEvent newCalendarEvent(Organisation organisation, CalendarEventType eventType, boolean visibility) {
        CalendarEvent event = newCalendarEvent(organisation, eventType, today(), newVenue());
        event.setPublicVisibility(visibility);
        return event;
    }

    public CalendarEvent newCalendarEvent(Organisation organisation, CalendarEventType eventType, LocalDate date) {
        return newCalendarEvent(organisation, eventType, date, newVenue());
    }

    public CalendarEvent newCalendarEvent(Organisation organisation, CalendarEventType eventType, LocalDate date,
                                          boolean excludedFromStatistics) {
        CalendarEvent event = newCalendarEvent(organisation, eventType, date, newVenue());
        event.setExcludedFromStatistics(excludedFromStatistics);
        return event;
    }

    public CalendarEvent newCalendarEvent(Organisation organisation, boolean isShootingTestEvent, LocalDate date) {
        final EnumSet<CalendarEventType> eventTypes =
                isShootingTestEvent ? CalendarEventType.shootingTestTypes() : CalendarEventType.nonShootingTestTypes();

        return newCalendarEvent(organisation, some(eventTypes), date, newVenue());
    }

    public CalendarEvent newCalendarEvent(Organisation organisation,
                                          CalendarEventType eventType,
                                          LocalDate date,
                                          Venue venue) {

        LocalTime beginTime = new LocalTime(18, 0);
        LocalTime endTime = beginTime.plusHours(3);

        CalendarEvent event =
                new CalendarEvent(organisation, venue, eventType, date.toDate(), beginTime, "name", "description");
        event.setEndTime(endTime);

        event.setPublicVisibility(true);

        return add(event);
    }

    public AdditionalCalendarEvent newAdditionalCalendarEvent(CalendarEvent event,
                                                              LocalDate date,
                                                              LocalTime beginTime,
                                                              Venue venue) {
        AdditionalCalendarEvent additionalCalendarEvent = new AdditionalCalendarEvent(date.toDate(), beginTime, beginTime.plusHours(2), event, venue);
        return add(additionalCalendarEvent);
    }

    public ObservationBaseFields newObservationBaseFields(GameSpecies species, ObservationSpecVersion specVersion) {
        return newObservationBaseFields(species, Required.VOLUNTARY, specVersion);
    }

    public ObservationBaseFields newObservationBaseFields(GameSpecies species,
                                                          Required withinMooseHunting,
                                                          ObservationSpecVersion specVersion) {

        ObservationBaseFields baseFields = new ObservationBaseFields(species, specVersion.getMetadataVersion());
        baseFields.setWithinMooseHunting(withinMooseHunting);
        return add(baseFields);
    }

    public ObservationContextSensitiveFields newObservationContextSensitiveFields(GameSpecies species,
                                                                                  boolean withinMooseHunting,
                                                                                  ObservationType observationType,
                                                                                  ObservationSpecVersion specVersion) {

        final ObservationContextSensitiveFields fields = new ObservationContextSensitiveFields(
                species, withinMooseHunting, observationType, specVersion.getMetadataVersion());

        fields.setExtendedAgeRange(true);
        fields.setAmount(DynamicObservationFieldPresence.YES);
        fields.setGender(Required.VOLUNTARY);
        fields.setAge(Required.VOLUNTARY);
        fields.setWounded(Required.VOLUNTARY);
        fields.setDead(Required.VOLUNTARY);
        fields.setOnCarcass(Required.VOLUNTARY);
        fields.setCollarOrRadioTransmitter(Required.VOLUNTARY);
        fields.setLegRingOrWingMark(Required.VOLUNTARY);
        fields.setEarMark(Required.VOLUNTARY);

        if (specVersion.supportsLargeCarnivoreFields()) {
            fields.setWidthOfPaw(DynamicObservationFieldPresence.VOLUNTARY);

            if (species.isLargeCarnivore()) {
                fields.setLengthOfPaw(DynamicObservationFieldPresence.VOLUNTARY_CARNIVORE_AUTHORITY);
            }
        }

        return add(fields);
    }

    public SrvaEvent newSrvaEvent() {
        return newSrvaEvent(newPerson());
    }

    public SrvaEvent newSrvaEvent(Riistanhoitoyhdistys rhy) {
        return newSrvaEvent(newPerson(), newGameSpecies(), rhy);
    }

    public SrvaEvent newSrvaEvent(Person person) {
        return newSrvaEvent(person, newRiistanhoitoyhdistys());
    }

    public SrvaEvent newSrvaEvent(Person person, Riistanhoitoyhdistys rhy) {
        return newSrvaEvent(person, newGameSpecies(), rhy);
    }

    public SrvaEvent newSrvaEvent(Person person, GameSpecies species) {
        return newSrvaEvent(person, species, newRiistanhoitoyhdistys());
    }

    public SrvaEvent newSrvaEvent(Person author, GameSpecies species, Riistanhoitoyhdistys rhy) {
        final SrvaEvent srvaEventEntity = new SrvaEvent();
        srvaEventEntity.setEventName(some(SrvaEventNameEnum.class));
        srvaEventEntity.setEventType(some(SrvaEventTypeEnum.getBySrvaEvent(srvaEventEntity.getEventName())));
        srvaEventEntity.setGeoLocation(geoLocation());
        srvaEventEntity.setPointOfTime(now().toDate());
        srvaEventEntity.setSpecies(species);
        srvaEventEntity.setTotalSpecimenAmount(1);
        srvaEventEntity.setAuthor(author);
        srvaEventEntity.setDescription("description");
        srvaEventEntity.setPersonCount(nextPositiveIntAtMost(15));
        srvaEventEntity.setTimeSpent(nextPositiveIntAtMost(50));
        srvaEventEntity.setEventResult(some(SrvaResultEnum.class));
        srvaEventEntity.setOtherMethodDescription("other method description");
        srvaEventEntity.setOtherTypeDescription("other type description");
        srvaEventEntity.setRhy(rhy);
        srvaEventEntity.setState(SrvaEventStateEnum.UNFINISHED);

        return add(srvaEventEntity);
    }

    public SrvaSpecimen newSrvaSpecimen(SrvaEvent srvaEvent) {
        return newSrvaSpecimen(srvaEvent, some(GameAge.class), some(GameGender.class));
    }

    public SrvaSpecimen newSrvaSpecimen(SrvaEvent srvaEvent, GameAge age, GameGender gender) {

        SrvaSpecimen specimen = new SrvaSpecimen(srvaEvent);
        specimen.setAge(age);
        specimen.setGender(gender);

        return add(specimen);
    }

    public SrvaMethod newSrvaMethod(SrvaEvent srvaEvent) {
        return newSrvaMethod(srvaEvent, some(SrvaMethodEnum.class), true);
    }

    public SrvaMethod newSrvaMethod(SrvaEvent srvaEvent, SrvaMethodEnum name, boolean isChecked) {

        SrvaMethod method = new SrvaMethod(srvaEvent);
        method.setName(name);
        method.setChecked(isChecked);

        return add(method);
    }

    public Announcement newAnnouncement(final SystemUser fromUser,
                                        final Organisation fromOrganisation,
                                        final AnnouncementSenderType senderType) {
        return add(new Announcement(
                "AnnouncementBody-" + serial(),
                "AnnouncementSubject-" + serial(),
                fromUser,
                fromOrganisation,
                senderType));
    }

    public AnnouncementSubscriber newAnnouncementSubscriber(final Announcement announcement,
                                                            final Organisation organisation,
                                                            final OccupationType occupationType) {
        return add(new AnnouncementSubscriber(announcement, organisation, occupationType));
    }

    public LHOrganisation newLHOrganisation() {
        return newLHOrganisation(newRiistanhoitoyhdistys());
    }

    public LHOrganisation newLHOrganisation(Riistanhoitoyhdistys rhy) {
        return newLHOrganisation(rhy, zeroPaddedNumber(7));
    }

    private LHOrganisation newLHOrganisation(Riistanhoitoyhdistys rhy, String officialCode) {
        LHOrganisation o = new LHOrganisation();
        o.setNameFinnish("LHOrganisation" + officialCode);
        o.setNameSwedish("LHOrganisation" + officialCode);
        o.setOfficialCode(officialCode);
        o.setRhyOfficialCode(rhy.getOfficialCode());
        return add(o);
    }

    public JHTTraining newJHTTraining(OccupationType occupationType, Person person) {
        final JHTTraining training = new JHTTraining();
        training.setOccupationType(occupationType);
        training.setPerson(person);
        training.setTrainingDate(today());
        training.setTrainingType(JHTTraining.TrainingType.SAHKOINEN);
        return add(training);
    }

    public Integration newIntegration(String id) {
        Integration i = new Integration();
        i.setId(id);
        return add(i);
    }

    public MobileClientDevice newMobileClientDevice(final Person person) {
        final MobileClientDevice device = new MobileClientDevice();
        device.setPerson(person);
        device.setClientVersion("1");
        device.setDeviceName("android");
        device.setPlatform(MobileClientDevice.Platform.ANDROID);
        device.setPushToken(UUID.randomUUID().toString());

        return add(device);
    }

    public ShootingTestEvent newShootingTestEvent() {
        return newShootingTestEvent(newCalendarEvent(some(CalendarEventType.shootingTestTypes())));
    }

    public ShootingTestEvent newShootingTestEvent(Riistanhoitoyhdistys rhy) {
        return newShootingTestEvent(rhy, some(CalendarEventType.shootingTestTypes()));
    }

    public ShootingTestEvent newShootingTestEvent(Riistanhoitoyhdistys rhy, CalendarEventType calendarEventType) {
        Preconditions.checkArgument(calendarEventType.isShootingTest(), "event type not related to shooting tests");
        return newShootingTestEvent(newCalendarEvent(rhy, calendarEventType));
    }

    public ShootingTestEvent newShootingTestEvent(Riistanhoitoyhdistys rhy, LocalDate date) {
        return newShootingTestEvent(newCalendarEvent(rhy, CalendarEventType.AMPUMAKOE, date));
    }

    public ShootingTestEvent newShootingTestEvent(CalendarEvent calendarEvent) {
        return add(new ShootingTestEvent(calendarEvent));
    }

    public ShootingTestOfficial newShootingTestOfficial(ShootingTestEvent event, Person person) {
        Organisation rhy = event.getCalendarEvent().getOrganisation();
        return add(new ShootingTestOfficial(event, newOccupation(rhy, person,
                OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA)));
    }

    public ShootingTestParticipant newShootingTestParticipant() {
        return newShootingTestParticipant(newShootingTestEvent());
    }

    public ShootingTestParticipant newShootingTestParticipant(ShootingTestEvent event) {
        return newShootingTestParticipant(event, newPerson());
    }

    public ShootingTestParticipant newShootingTestParticipant(ShootingTestEvent event, Person person) {
        return add(new ShootingTestParticipant(event, person));
    }

    public ShootingTestAttempt newShootingTestAttempt() {
        return newShootingTestAttempt(newShootingTestParticipant(), ShootingTestAttemptResult.QUALIFIED);
    }

    public ShootingTestAttempt newShootingTestAttempt(ShootingTestParticipant participant) {
        return newShootingTestAttempt(participant, some(ShootingTestAttemptResult.class));
    }

    public ShootingTestAttempt newShootingTestAttempt(ShootingTestParticipant participant, ShootingTestType type) {
        return newShootingTestAttempt(participant, type, ShootingTestAttemptResult.QUALIFIED);
    }

    public ShootingTestAttempt newShootingTestAttempt(ShootingTestParticipant participant,
                                                      ShootingTestAttemptResult result) {

        return newShootingTestAttempt(participant, ShootingTestType.MOOSE, result);
    }

    public ShootingTestAttempt newShootingTestAttempt(final ShootingTestParticipant participant,
                                                      final ShootingTestType type,
                                                      final ShootingTestAttemptResult result) {

        final int hits = result == ShootingTestAttemptResult.QUALIFIED ? type.getNumberOfHitsToQualify() : 0;
        return add(new ShootingTestAttempt(participant, type, result, hits, null));
    }

    public RhyAnnualStatistics newRhyAnnualStatistics() {
        return newRhyAnnualStatistics(newRiistanhoitoyhdistys());
    }

    public RhyAnnualStatistics newRhyAnnualStatistics(Riistanhoitoyhdistys rhy) {
        return newRhyAnnualStatistics(rhy, currentYear());
    }

    public RhyAnnualStatistics newRhyAnnualStatistics(Riistanhoitoyhdistys rhy, int year) {
        final RhyAnnualStatistics entity = new RhyAnnualStatistics(rhy, year);
        populate(entity);
        return add(entity);
    }

    public RhyAnnualStatisticsStateChangeEvent newRhyAnnualStatisticsStateChangeEvent(final RhyAnnualStatistics statistics,
                                                                                      final RhyAnnualStatisticsState state) {

        return add(new RhyAnnualStatisticsStateChangeEvent(statistics, state));
    }

    public RhyAnnualStatisticsModeratorUpdateEvent newRhyAnnualStatisticsModeratorUpdateEvent(final RhyAnnualStatistics statistics,
                                                                                              final AnnualStatisticGroup group,
                                                                                              final DateTime eventTime,
                                                                                              final long userId) {

        return add(new RhyAnnualStatisticsModeratorUpdateEvent(statistics, group, eventTime, userId));
    }

    public PermitDecisionInvoice newPermitDecisionInvoice() {
        return newPermitDecisionInvoice(newRiistanhoitoyhdistys());
    }

    public PermitDecisionInvoice newPermitDecisionInvoice(final Riistanhoitoyhdistys rhy) {
        return newPermitDecisionInvoice(newPermitDecision(rhy));
    }

    public PermitDecisionInvoice newPermitDecisionInvoice(final PermitDecision decision) {
        final LocalDate invoiceDate = decision.getPublishDate().toLocalDate();
        final Invoice invoice = newInvoice(decision, InvoiceType.PERMIT_PROCESSING, invoiceDate, new BigDecimal(90));

        return add(new PermitDecisionInvoice(decision, invoice));
    }

    public PermitHarvestInvoice newPermitHarvestInvoice(final HarvestPermitSpeciesAmount speciesAmount) {
        return newPermitHarvestInvoice(speciesAmount, today());
    }

    public PermitHarvestInvoice newPermitHarvestInvoice(final HarvestPermitSpeciesAmount speciesAmount,
                                                        final LocalDate invoiceDate) {

        final PermitDecision decision = speciesAmount.getHarvestPermit().getPermitDecision();

        final Invoice invoice = newInvoice(decision, InvoiceType.PERMIT_HARVEST, invoiceDate, new BigDecimal(120));

        return add(new PermitHarvestInvoice(invoice, speciesAmount));
    }

    public PermitHarvestInvoice newPermitHarvestInvoice() {
        return newPermitHarvestInvoice(newRiistanhoitoyhdistys(), newGameSpecies(), today());
    }

    public PermitHarvestInvoice newPermitHarvestInvoice(final Riistanhoitoyhdistys rhy) {
        return newPermitHarvestInvoice(rhy, newGameSpecies(), today());
    }

    public PermitHarvestInvoice newPermitHarvestInvoice(final GameSpecies species) {
        return newPermitHarvestInvoice(newRiistanhoitoyhdistys(), species, today());
    }

    public PermitHarvestInvoice newPermitHarvestInvoice(final LocalDate invoiceDate) {
        return newPermitHarvestInvoice(newRiistanhoitoyhdistys(), newGameSpecies(), invoiceDate);
    }

    public PermitHarvestInvoice newPermitHarvestInvoice(final Riistanhoitoyhdistys rhy, final LocalDate invoiceDate) {
        return newPermitHarvestInvoice(rhy, newGameSpecies(), invoiceDate);
    }

    public PermitHarvestInvoice newPermitHarvestInvoice(final Riistanhoitoyhdistys rhy, final GameSpecies species) {
        return newPermitHarvestInvoice(rhy, species, today());
    }

    public PermitHarvestInvoice newPermitHarvestInvoice(final Riistanhoitoyhdistys rhy,
                                                        final GameSpecies species,
                                                        final LocalDate invoiceDate) {

        final PermitDecision decision = newPermitDecision(rhy, species);
        final HarvestPermit permit = newMooselikePermit(decision);
        final HarvestPermitSpeciesAmount speciesAmount = newHarvestPermitSpeciesAmount(permit, species);
        return newPermitHarvestInvoice(speciesAmount, invoiceDate);
    }

    public Invoice newInvoice(final PermitDecision decision,
                              final InvoiceType type,
                              final LocalDate invoiceDate,
                              final BigDecimal amount) {

        final HarvestPermitApplication application = decision.getApplication();
        final boolean electronicInvoicing =
                type == InvoiceType.PERMIT_HARVEST || !Boolean.TRUE.equals(application.getDeliveryByMail());

        final InvoiceState state;

        if (!NumberUtils.bigDecimalIsPositive(amount)) {
            state = InvoiceState.VOID;
        } else if (electronicInvoicing) {
            state = InvoiceState.DELIVERED;
        } else {
            state = InvoiceState.CREATED;
        }

        final Invoice invoice = new Invoice(type, electronicInvoicing);
        invoice.setInvoiceNumber(200000 + serial());
        invoice.updateInvoiceAndDueDate(invoiceDate);
        invoice.setState(state);
        invoice.setAmount(amount);
        invoice.setIbanAndBic(bankAccount());
        invoice.setCreditorReference(invoiceCreditorReference(decision, type));

        final DeliveryAddress deliveryAddress = requireNonNull(decision.getDeliveryAddress());
        final Address recipientAddress = new Address();
        recipientAddress.setStreetAddress(deliveryAddress.getStreetAddress());
        recipientAddress.setPostalCode(deliveryAddress.getPostalCode());
        recipientAddress.setCity(deliveryAddress.getCity());
        recipientAddress.setCountry(deliveryAddress.getCountry());
        add(recipientAddress);

        invoice.setRecipientName(deliveryAddress.getRecipient());
        invoice.setRecipientAddress(recipientAddress);

        invoice.setPdfFileMetadata(newPersistentFileContent().getMetadata());

        return add(invoice);
    }

    protected CreditorReference invoiceCreditorReference(final PermitDecision decision,
                                                         final InvoiceType type) {

        final StringBuilder creditorRefBuilder = new StringBuilder(decision.getDecisionYear())
                .append(String.format("%08d", decision.getDecisionNumber()));

        switch (type) {
            case PERMIT_PROCESSING:
                break;
            case PERMIT_HARVEST:
                // Add unique part to reference since there might exist multiple for one permit decision.
                creditorRefBuilder.append(serial());
                break;
            default:
                throw new UnsupportedOperationException();
        }

        final String base = creditorRefBuilder.toString();
        creditorRefBuilder.append(FinnishCreditorReferenceValidator.calculateChecksum(base));
        return CreditorReference.fromNullable(creditorRefBuilder.toString());
    }

    public InvoicePaymentLine newInvoicePaymentLine(final Invoice invoice) {
        return newInvoicePaymentLine(invoice, invoice.getAmount());
    }

    public InvoicePaymentLine newInvoicePaymentLine(final Invoice invoice, final BigDecimal amount) {
        return newInvoicePaymentLine(invoice, amount, today());
    }

    public InvoicePaymentLine newInvoicePaymentLine(final Invoice invoice,
                                                    final BigDecimal amount,
                                                    final LocalDate paymentDate) {

        return add(new InvoicePaymentLine(invoice, paymentDate, amount));
    }

    public InvoicePaymentLine newInvoicePaymentLine(final Invoice invoice, final AccountTransfer accountTransfer) {
        return add(new InvoicePaymentLine(invoice, accountTransfer));
    }

    public AccountTransferBatch newAccountTransferBatch(final LocalDate accountStatementDate) {
        final String batchName = "accountStatementBatch-" + DATE_FORMATTER.print(accountStatementDate);
        return add(new AccountTransferBatch(accountStatementDate, batchName, accountStatementDate.plusDays(1)));
    }

    public AccountTransfer newAccountTransfer(final AccountTransferBatch batch,
                                              final LocalDate transactionDate,
                                              final LocalDate bookingDate) {

        final AccountTransfer transfer = new AccountTransfer();
        transfer.setBatch(batch);
        transfer.setCreditorIban(iban());
        transfer.setCreditorReference(creditorReference());
        transfer.setAmount(new BigDecimal(nextPositiveInt()));
        transfer.setTransactionDate(transactionDate);
        transfer.setBookingDate(bookingDate);
        transfer.setDebtorName("Debtor-" + serial());
        transfer.setAccountServiceReference("ArchivalID-" + serial());
        return add(transfer);
    }

    public AccountTransfer newAccountTransfer(final AccountTransferBatch batch,
                                              final Invoice invoice,
                                              final LocalDate transactionDate,
                                              final LocalDate bookingDate) {

        final AccountTransfer transfer = new AccountTransfer();
        transfer.setBatch(batch);
        transfer.setCreditorIban(invoice.getIban());
        transfer.setCreditorReference(invoice.getCreditorReference());
        transfer.setAmount(invoice.getAmount());
        transfer.setTransactionDate(transactionDate);
        transfer.setBookingDate(bookingDate);
        transfer.setDebtorName(invoice.getRecipientName());
        transfer.setAccountServiceReference("ArchivalID-" + serial());
        return add(transfer);
    }

    public AccountTransfer newAccountTransfer(final LocalDate transactionDate, final LocalDate bookingDate) {
        return newAccountTransfer(newAccountTransferBatch(bookingDate), transactionDate, bookingDate);
    }

    public AccountTransfer newAccountTransfer(final Invoice invoice,
                                              final LocalDate transactionDate,
                                              final LocalDate bookingDate) {

        return newAccountTransfer(newAccountTransferBatch(bookingDate), invoice, transactionDate, bookingDate);
    }


    public PersonalAreaUnion newPersonalAreaUnion(final String name, final Person person) {
        final PersonalAreaUnion personalAreaUnion = new PersonalAreaUnion();
        personalAreaUnion.setName(name);
        personalAreaUnion.setHarvestPermitArea(newHarvestPermitArea());
        personalAreaUnion.setPerson(person);
        return add(personalAreaUnion);

    }
    public HarvestPermitApplicationAttachment newHarvestPermitApplicationAttachment(final HarvestPermitApplication application) {
        final PersistentFileMetadata persistentFileMetadata = newPersistentFileMetadata();
        final HarvestPermitApplicationAttachment attachment = new HarvestPermitApplicationAttachment();
        attachment.setHarvestPermitApplication(application);
        attachment.setAttachmentType(HarvestPermitApplicationAttachment.Type.PROTECTED_AREA);
        attachment.setAttachmentMetadata(persistentFileMetadata);
        return add(attachment);
    }

    public PersistentFileMetadata newPersistentFileMetadata() {
        final PersistentFileMetadata persistentFileMetadata = new PersistentFileMetadata();
        persistentFileMetadata.setId(UUID.randomUUID());
        persistentFileMetadata.setContentType("test");
        persistentFileMetadata.setStorageType(StorageType.LOCAL_FOLDER);
        return add(persistentFileMetadata);
    }

    public MetsahallitusPermit newMetsahallitusPermit() {
        return newMetsahallitusPermit("Karhulupa", "Karhualue", hunterNumber());
    }

    public MetsahallitusPermit newMetsahallitusPermit(final String permitType, final String areaName,
                                                      final String hunterNumber) {
        final MetsahallitusPermit metsahallitusPermit = new MetsahallitusPermit();
        metsahallitusPermit.setStatus("160");
        metsahallitusPermit.setAreaNumber(zeroPaddedNumber(4));
        metsahallitusPermit.setPermitIdentifier(zeroPaddedNumber(5));
        metsahallitusPermit.setPermitType(String.format("%s-fi", permitType));
        metsahallitusPermit.setPermitTypeSwedish(String.format("%s-sv", permitType));
        metsahallitusPermit.setPermitTypeEnglish(String.format("%s-en", permitType));
        metsahallitusPermit.setBeginDate(today().minusMonths(1));
        metsahallitusPermit.setEndDate(today().plusMonths(1));
        metsahallitusPermit.setAreaName(String.format("%s-fi", areaName));
        metsahallitusPermit.setAreaNameSwedish(String.format("%s-sv", areaName));
        metsahallitusPermit.setAreaNameEnglish(String.format("%s-en", areaName));
        metsahallitusPermit.setUrl("https://riista.fi");
        metsahallitusPermit.setHarvestReportSubmitted(false);
        metsahallitusPermit.setHunterNumber(hunterNumber);
        return add(metsahallitusPermit);
    }

    public PermitDecisionForbiddenMethod newPermitDecisionForbiddenMethod(final PermitDecision permitDecision,
                                                                          final GameSpecies gameSpecies,
                                                                          final ForbiddenMethodType forbiddenMethodType) {
        final PermitDecisionForbiddenMethod permitDecisionForbiddenMethod = new PermitDecisionForbiddenMethod();
        permitDecisionForbiddenMethod.setPermitDecision(permitDecision);
        permitDecisionForbiddenMethod.setGameSpecies(gameSpecies);
        permitDecisionForbiddenMethod.setMethod(forbiddenMethodType);
        return add(permitDecisionForbiddenMethod);
    }

    protected <T extends Persistable<?>> T add(@Nonnull final T object) {
        requireNonNull(object);
        transientEntityList.add(object);
        return object;
    }

    protected int serial() {
        return getNumberGenerator().nextInt();
    }

    protected String lupaHallintaId() {
        return zeroPaddedNumber(8);
    }

    private String generateUsername(SystemUser.Role role) {
        requireNonNull(role);

        final String basename;

        switch (role) {
            case ROLE_ADMIN:
                basename = "admin";
                break;
            case ROLE_MODERATOR:
                basename = "moderator";
                break;
            case ROLE_REST:
                basename = "api-user";
                break;
            default:
                basename = "user";
        }

        return basename + "-" + serial();
    }
}
