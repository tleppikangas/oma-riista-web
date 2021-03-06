package fi.riista.feature.organization.rhy.annualstats;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.announcement.AnnouncementSenderType;
import fi.riista.feature.announcement.QAnnouncement;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.srva.QSrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.feature.gamediary.srva.SrvaEventTypeEnum;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.organization.calendar.CalendarEventRepository;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.calendar.QCalendarEvent;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.shootingtest.QShootingTestAttempt;
import fi.riista.feature.shootingtest.QShootingTestEvent;
import fi.riista.feature.shootingtest.QShootingTestParticipant;
import fi.riista.feature.shootingtest.ShootingTestType;
import fi.riista.sql.SQRhy;
import io.vavr.Lazy;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.iban4j.Iban;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.querydsl.core.types.dsl.Expressions.cases;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.gamediary.srva.SrvaEventNameEnum.ACCIDENT;
import static fi.riista.feature.gamediary.srva.SrvaEventStateEnum.APPROVED;
import static fi.riista.feature.permit.application.HarvestPermitApplication.Status.ACTIVE;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.QUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.REBATED;
import static fi.riista.util.DateUtil.createDateInterval;
import static fi.riista.util.DateUtil.today;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public class AnnualStatisticsResolver {

    private static final Integer[] SRVA_GAME_SPECIES_CODES = new Integer[]{
            OFFICIAL_CODE_MOOSE,
            OFFICIAL_CODE_WHITE_TAILED_DEER,
            OFFICIAL_CODE_ROE_DEER,
            OFFICIAL_CODE_WILD_FOREST_REINDEER,
            OFFICIAL_CODE_FALLOW_DEER,
            OFFICIAL_CODE_WILD_BOAR,
            OFFICIAL_CODE_LYNX,
            OFFICIAL_CODE_BEAR,
            OFFICIAL_CODE_WOLF,
            OFFICIAL_CODE_WOLVERINE
    };

    private final int calendarYear;

    private final Supplier<Iban> ibanSupplier;
    private final Supplier<Integer> rhyMemberCountSupplier;
    private final Supplier<Long> rhyLandAreaSizeSupplier;
    private final Supplier<Map<OccupationType, Long>> occupationTypeCountsSupplier;
    private final Supplier<Map<CalendarEventType, Long>> eventTypeCountsSupplier;
    private final Supplier<Map<ShootingTestType, Tuple2<Integer, Integer>>> shootingTestCountsSupplier;
    private final Supplier<Tuple2<Integer, Integer>> srvaContributionSupplier;
    private final Supplier<Map<SrvaEventNameEnum, Map<Integer, Integer>>> srvaEventsCountsSupplier;
    private final Supplier<Map<SrvaEventTypeEnum, Integer>> srvaAccidentCountsSupplier;
    private final Supplier<Integer> harvestPermitApplicationCountSupplier;
    private final Supplier<Integer> announcementCountSupplier;
    private final Supplier<Map<CalendarEventType, Integer>> eventParticipantCountSupplier;

    public AnnualStatisticsResolver(@Nonnull final Riistanhoitoyhdistys rhy,
                                    final int calendarYear,
                                    @Nonnull final OccupationRepository occupationRepository,
                                    @Nonnull final CalendarEventRepository eventRepository,
                                    @Nonnull final JPAQueryFactory jpaQueryFactory,
                                    @Nonnull final SQLQueryFactory sqlQueryFactory) {

        requireNonNull(rhy, "rhy is null");
        requireNonNull(occupationRepository, "occupationRepository is null");
        requireNonNull(eventRepository, "eventRepository is null");
        requireNonNull(jpaQueryFactory, "jpaQueryFactory is null");
        requireNonNull(sqlQueryFactory, "sqlQueryFactory is null");

        this.calendarYear = calendarYear;

        final LocalDate firstDayOfYear = new LocalDate(calendarYear, 1, 1);
        final LocalDate lastDayOfYear = new LocalDate(calendarYear, 12, 31);
        final LocalDate today = today();

        final Interval yearAsInterval = createDateInterval(firstDayOfYear, lastDayOfYear);

        this.ibanSupplier = Lazy.of(() -> getIbanFromPreviousYear(rhy, calendarYear, jpaQueryFactory));

        this.rhyMemberCountSupplier = Lazy.of(() -> {
            return Long.valueOf(countMembers(rhy, jpaQueryFactory)).intValue();
        });
        this.rhyLandAreaSizeSupplier = Lazy.of(() -> getRhyLandAreaSize(rhy, sqlQueryFactory));

        this.occupationTypeCountsSupplier = Lazy.of(() -> {
            return countOccupiedPersons(rhy, yearAsInterval, occupationRepository);
        });

        this.eventTypeCountsSupplier = Lazy.of(() -> {
            final LocalDate lastDate = today.isBefore(lastDayOfYear) ? today : lastDayOfYear;
            return eventRepository.countEventTypes(rhy, firstDayOfYear, lastDate);
        });
        this.shootingTestCountsSupplier = Lazy.of(() -> countShootingTestAttempts(rhy, calendarYear, jpaQueryFactory));

        this.srvaContributionSupplier = Lazy.of(() -> countSrvaContribution(rhy, calendarYear, jpaQueryFactory));
        this.srvaEventsCountsSupplier = Lazy.of(() -> {
            return countSrvaEventsByCategoryAndSpeciesCode(rhy, calendarYear, jpaQueryFactory);
        });
        this.srvaAccidentCountsSupplier = Lazy.of(() -> {
            return countSrvaAccidentsByType(rhy, calendarYear, jpaQueryFactory);
        });

        this.harvestPermitApplicationCountSupplier = Lazy.of(() -> {
            return Long.valueOf(countHarvestPermitApplicationPartners(rhy, calendarYear, jpaQueryFactory)).intValue();
        });

        this.announcementCountSupplier = Lazy.of(() -> {
            return Long.valueOf(countAnnouncements(rhy, calendarYear, jpaQueryFactory)).intValue();
        });

        this.eventParticipantCountSupplier = Lazy.of(() -> {
            final LocalDate lastDate = today.isBefore(lastDayOfYear) ? today : lastDayOfYear;
            return eventRepository.countEventParticipants(rhy, firstDayOfYear, lastDate);
        });
    }

    public int getCalendarYear() {
        return calendarYear;
    }

    public Iban getIbanFromPreviousYear() {
        return ibanSupplier.get();
    }

    public int getNumberOfRhyMembers() {
        return rhyMemberCountSupplier.get();
    }

    public Long getRhyLandAreaSize() {
        return rhyLandAreaSizeSupplier.get();
    }

    public Integer getRhyLandAreaSizeInHectares() {
        return Optional.ofNullable(getRhyLandAreaSize())
                .map(landAreaSizeInSquareMeters -> landAreaSizeInSquareMeters / 10_000)
                .map(Long::intValue)
                .orElse(null);
    }

    public int getOccupationTypeCount(final OccupationType occupationType) {
        return occupationTypeCountsSupplier.get().getOrDefault(occupationType, 0L).intValue();
    }

    public int getEventTypeCount(final CalendarEventType eventType) {
        return eventTypeCountsSupplier.get().getOrDefault(eventType, 0L).intValue();
    }

    public int getEventParticipantsCount(final CalendarEventType eventType) {
        return eventParticipantCountSupplier.get().getOrDefault(eventType, 0).intValue();
    }

    public int getShootingTestTotalCount(final ShootingTestType testType) {
        final Tuple2<Integer, Integer> tuple = shootingTestCountsSupplier.get().get(testType);
        return tuple == null ? 0 : tuple._1;
    }

    public int getShootingTestQualifiedCount(final ShootingTestType testType) {
        final Tuple2<Integer, Integer> tuple = shootingTestCountsSupplier.get().get(testType);
        return tuple == null ? 0 : tuple._2;
    }

    public Tuple2<Integer, Integer> getTimeSpentAndPersonCountFromSrvaEvents() {
        return srvaContributionSupplier.get();
    }

    public SrvaSpeciesCountStatistics getSrvaEventCounts(@Nonnull final SrvaEventNameEnum category) {
        final SrvaSpeciesCountStatistics stats = new SrvaSpeciesCountStatistics();

        int mooses = 0;
        int whiteTailedDeers = 0;
        int roeDeers = 0;
        int wildForestReindeers = 0;
        int fallowDeers = 0;
        int wildBoars = 0;
        int lynxes = 0;
        int bears = 0;
        int wolfs = 0;
        int wolverines = 0;

        final Map<Integer, Integer> countBySpeciesCode = srvaEventsCountsSupplier.get().get(category);

        if (countBySpeciesCode != null) {
            mooses = countBySpeciesCode.getOrDefault(OFFICIAL_CODE_MOOSE, 0);
            whiteTailedDeers = countBySpeciesCode.getOrDefault(OFFICIAL_CODE_WHITE_TAILED_DEER, 0);
            roeDeers = countBySpeciesCode.getOrDefault(OFFICIAL_CODE_ROE_DEER, 0);
            wildForestReindeers = countBySpeciesCode.getOrDefault(OFFICIAL_CODE_WILD_FOREST_REINDEER, 0);
            fallowDeers = countBySpeciesCode.getOrDefault(OFFICIAL_CODE_FALLOW_DEER, 0);
            wildBoars = countBySpeciesCode.getOrDefault(OFFICIAL_CODE_WILD_BOAR, 0);
            lynxes = countBySpeciesCode.getOrDefault(OFFICIAL_CODE_LYNX, 0);
            bears = countBySpeciesCode.getOrDefault(OFFICIAL_CODE_BEAR, 0);
            wolfs = countBySpeciesCode.getOrDefault(OFFICIAL_CODE_WOLF, 0);
            wolverines = countBySpeciesCode.getOrDefault(OFFICIAL_CODE_WOLVERINE, 0);
        }

        stats.setMooses(mooses);
        stats.setWhiteTailedDeers(whiteTailedDeers);
        stats.setRoeDeers(roeDeers);
        stats.setWildForestReindeers(wildForestReindeers);
        stats.setFallowDeers(fallowDeers);
        stats.setWildBoars(wildBoars);
        stats.setLynxes(lynxes);
        stats.setBears(bears);
        stats.setWolves(wolfs);
        stats.setWolverines(wolverines);

        return stats;
    }

    public int getSrvaAccidentCount(@Nonnull final SrvaEventTypeEnum type) {
        if (!SrvaEventTypeEnum.getBySrvaEvent(ACCIDENT).contains(type)) {
            throw new IllegalStateException("Non-accident type given: " + type.name());
        }

        return srvaAccidentCountsSupplier.get().getOrDefault(type, 0);
    }

    public int getNumberOfHarvestPermitApplicationPartners() {
        return harvestPermitApplicationCountSupplier.get();
    }

    public int getNumberOfAnnouncements() {
        return announcementCountSupplier.get();
    }

    private static Iban getIbanFromPreviousYear(final Riistanhoitoyhdistys rhy,
                                                final int calendarYear,
                                                final JPAQueryFactory queryFactory) {

        final QRhyAnnualStatistics ANNUAL_STATISTICS = QRhyAnnualStatistics.rhyAnnualStatistics;

        return queryFactory
                .select(ANNUAL_STATISTICS.basicInfo.iban)
                .from(ANNUAL_STATISTICS)
                .where(ANNUAL_STATISTICS.rhy.eq(rhy))
                .where(ANNUAL_STATISTICS.year.eq(calendarYear - 1))
                .fetchOne();
    }

    private static Map<OccupationType, Long> countOccupiedPersons(final Riistanhoitoyhdistys rhy,
                                                                  final Interval interval,
                                                                  final OccupationRepository occupationRepository) {

        return occupationRepository.findActiveByOrganisation(rhy, interval)
                .stream()
                .map(occupation -> Tuple.of(occupation.getOccupationType(), occupation.getPerson().getId()))
                .distinct()
                .collect(groupingBy(Tuple2::_1, counting()));
    }

    private static long countMembers(final Riistanhoitoyhdistys rhy, final JPAQueryFactory queryFactory) {
        final QPerson PERSON = QPerson.person;
        return queryFactory.select(PERSON).from(PERSON).where(PERSON.rhyMembership.eq(rhy)).fetchCount();
    }

    private static Long getRhyLandAreaSize(final Riistanhoitoyhdistys rhy, final SQLQueryFactory queryFactory) {
        final SQRhy RHY = SQRhy.rhy;

        final BigInteger landAreaSize =
                queryFactory.select(RHY.maaAla).from(RHY).where(RHY.id.eq(rhy.getOfficialCode())).fetchOne();

        return landAreaSize != null ? landAreaSize.longValue() : null;
    }

    private static Map<ShootingTestType, Tuple2<Integer, Integer>> countShootingTestAttempts(final Riistanhoitoyhdistys rhy,
                                                                                             final int calendarYear,
                                                                                             final JPAQueryFactory queryFactory) {

        final QShootingTestAttempt ATTEMPT = QShootingTestAttempt.shootingTestAttempt;
        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;
        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;

        final NumberExpression<Integer> qualifiedCount = cases()
                .when(ATTEMPT.result.eq(QUALIFIED)).then(1)
                .otherwise(0)
                .sum();

        return queryFactory
                .select(ATTEMPT.type, ATTEMPT.count(), qualifiedCount)
                .from(ATTEMPT)
                .join(ATTEMPT.participant, PARTICIPANT)
                .join(PARTICIPANT.shootingTestEvent, EVENT)
                .join(EVENT.calendarEvent, CALENDAR_EVENT)
                .where(CALENDAR_EVENT.organisation.eq(rhy))
                .where(CALENDAR_EVENT.date.year().eq(calendarYear))
                .where(CALENDAR_EVENT.excludedFromStatistics.ne(true))
                .where(ATTEMPT.result.ne(REBATED))
                .groupBy(ATTEMPT.type)
                .fetch()
                .stream()
                .collect(toMap(
                        t -> t.get(ATTEMPT.type),
                        t -> Tuple.of(t.get(ATTEMPT.count()).intValue(), t.get(qualifiedCount).intValue())));
    }

    private static Tuple2<Integer, Integer> countSrvaContribution(final Riistanhoitoyhdistys rhy,
                                                                  final int calendarYear,
                                                                  final JPAQueryFactory queryFactory) {

        final QSrvaEvent SRVA_EVENT = QSrvaEvent.srvaEvent;

        return queryFactory
                .select(Projections.constructor(Tuple2.class, SRVA_EVENT.timeSpent.sum(), SRVA_EVENT.personCount.sum()))
                .from(SRVA_EVENT)
                .where(SRVA_EVENT.rhy.eq(rhy))
                .where(SRVA_EVENT.state.eq(APPROVED))
                .where(SRVA_EVENT.pointOfTime.year().eq(calendarYear))
                .fetchOne();
    }

    private static Map<SrvaEventNameEnum, Map<Integer, Integer>> countSrvaEventsByCategoryAndSpeciesCode(final Riistanhoitoyhdistys rhy,
                                                                                                         final int calendarYear,
                                                                                                         final JPAQueryFactory queryFactory) {

        final QSrvaEvent SRVA_EVENT = QSrvaEvent.srvaEvent;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        return queryFactory
                .select(SRVA_EVENT.eventName, SPECIES.officialCode, SRVA_EVENT.count())
                .from(SRVA_EVENT)
                .join(SRVA_EVENT.species, SPECIES)
                .where(SRVA_EVENT.rhy.eq(rhy)
                        .and(SRVA_EVENT.state.eq(APPROVED))
                        .and(SRVA_EVENT.pointOfTime.year().eq(calendarYear))
                        .and(SPECIES.officialCode.in(SRVA_GAME_SPECIES_CODES)))
                .groupBy(SRVA_EVENT.eventName, SPECIES.officialCode)
                .fetch()
                .stream()
                .collect(groupingBy(
                        t -> t.get(SRVA_EVENT.eventName),
                        toMap(t -> t.get(SPECIES.officialCode), t -> t.get(SRVA_EVENT.count()).intValue())));
    }

    private static Map<SrvaEventTypeEnum, Integer> countSrvaAccidentsByType(final Riistanhoitoyhdistys rhy,
                                                                            final int calendarYear,
                                                                            final JPAQueryFactory queryFactory) {

        final QSrvaEvent SRVA_EVENT = QSrvaEvent.srvaEvent;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        return queryFactory
                .select(SRVA_EVENT.eventType, SRVA_EVENT.count())
                .from(SRVA_EVENT)
                .join(SRVA_EVENT.species, SPECIES)
                .where(SRVA_EVENT.rhy.eq(rhy)
                        .and(SRVA_EVENT.state.eq(APPROVED))
                        .and(SRVA_EVENT.pointOfTime.year().eq(calendarYear))
                        .and(SPECIES.officialCode.in(SRVA_GAME_SPECIES_CODES))
                        .and(SRVA_EVENT.eventName.eq(ACCIDENT)))
                .groupBy(SRVA_EVENT.eventType)
                .fetch()
                .stream()
                .collect(toMap(t -> t.get(SRVA_EVENT.eventType), t -> t.get(SRVA_EVENT.count()).intValue()));
    }

    private static long countHarvestPermitApplicationPartners(final Riistanhoitoyhdistys rhy,
                                                              final int calendarYear,
                                                              final JPAQueryFactory queryFactory) {

        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QHuntingClub CLUB = QHuntingClub.huntingClub;

        return queryFactory
                .select(CLUB).distinct()
                .from(APPLICATION)
                .join(APPLICATION.permitPartners, CLUB)
                .where(APPLICATION.rhy.eq(rhy))
                .where(APPLICATION.status.eq(ACTIVE))
                .where(APPLICATION.applicationYear.eq(calendarYear))
                .fetchCount();
    }

    private static long countAnnouncements(final Riistanhoitoyhdistys rhy,
                                           final int calendarYear,
                                           final JPAQueryFactory queryFactory) {

        final QAnnouncement ANNOUNCEMENT = QAnnouncement.announcement;

        return queryFactory
                .selectFrom(ANNOUNCEMENT)
                .where(ANNOUNCEMENT.senderType.eq(AnnouncementSenderType.TOIMINNANOHJAAJA))
                .where(ANNOUNCEMENT.fromOrganisation.eq(rhy))
                .where(ANNOUNCEMENT.lifecycleFields.creationTime.year().eq(calendarYear))
                .fetchCount();
    }
}
