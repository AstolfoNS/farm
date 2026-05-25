package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.common.enums.CropStatus;
import cn.jxufe.farm.dao.SeedGrowthStageDao;
import cn.jxufe.farm.dao.SeedTypeDao;
import cn.jxufe.farm.dao.SoilTypeDao;
import cn.jxufe.farm.dao.UserCropDao;
import cn.jxufe.farm.dao.UserCropActionLogDao;
import cn.jxufe.farm.dao.UserPlotDao;
import cn.jxufe.farm.entity.SeedGrowthStage;
import cn.jxufe.farm.entity.SeedType;
import cn.jxufe.farm.entity.SoilType;
import cn.jxufe.farm.entity.UserCrop;
import cn.jxufe.farm.entity.UserCropActionLog;
import cn.jxufe.farm.entity.UserPlot;
import cn.jxufe.farm.service.CropStatusSchedulerService;
import cn.jxufe.farm.service.FarmRealtimePushService;
import cn.jxufe.farm.service.GameplayCoreService;
import jakarta.annotation.PreDestroy;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class CropStatusSchedulerServiceImp implements CropStatusSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(CropStatusSchedulerServiceImp.class);

    private final UserCropDao userCropDao;

    private final UserPlotDao userPlotDao;

    private final SoilTypeDao soilTypeDao;

    private final SeedTypeDao seedTypeDao;

    private final SeedGrowthStageDao seedGrowthStageDao;

    private final GameplayCoreService gameplayCoreService;

    private final FarmRealtimePushService farmRealtimePushService;

    private final UserCropActionLogDao userCropActionLogDao;

    private final int partitionSize;

    private final ExecutorService schedulerExecutor;

    public CropStatusSchedulerServiceImp(
            UserCropDao userCropDao,
            UserPlotDao userPlotDao,
            SoilTypeDao soilTypeDao,
            SeedTypeDao seedTypeDao,
            SeedGrowthStageDao seedGrowthStageDao,
            GameplayCoreService gameplayCoreService,
            FarmRealtimePushService farmRealtimePushService,
            UserCropActionLogDao userCropActionLogDao,
            @Value("${farm.gameplay.realtime.scheduler.parallelism:4}") int parallelism,
            @Value("${farm.gameplay.realtime.scheduler.partition-size:500}") int partitionSize
    ) {
        this.userCropDao = userCropDao;
        this.userPlotDao = userPlotDao;
        this.soilTypeDao = soilTypeDao;
        this.seedTypeDao = seedTypeDao;
        this.seedGrowthStageDao = seedGrowthStageDao;
        this.gameplayCoreService = gameplayCoreService;
        this.farmRealtimePushService = farmRealtimePushService;
        this.userCropActionLogDao = userCropActionLogDao;

        int safeParallelism = Math.max(1, parallelism);
        this.partitionSize = Math.max(100, partitionSize);
        this.schedulerExecutor = Executors.newFixedThreadPool(safeParallelism, new SchedulerThreadFactory());
    }

    @Override
    @Scheduled(fixedDelayString = "${farm.gameplay.realtime.crop-status-refresh-interval-ms:1000}")
    public void scheduleTick() {
        List<UserCrop> crops = userCropDao.findByIsDeletedFalseOrderByIdAsc();
        if (crops.isEmpty()) {
            return;
        }

        CropRuntimeContext context = buildRuntimeContext(crops);
        OffsetDateTime now = OffsetDateTime.now();
        List<List<UserCrop>> partitions = partition(crops, partitionSize);
        List<Future<PartitionResult>> futures = new ArrayList<>(partitions.size());
        for (List<UserCrop> part : partitions) {
            futures.add(schedulerExecutor.submit(() -> processPartition(part, context, now)));
        }

        List<CropRuntimeChange> changes = new ArrayList<>();
        Set<Long> changedUserIds = new HashSet<>();
        for (Future<PartitionResult> future : futures) {
            try {
                PartitionResult result = future.get();
                if (result == null) {
                    continue;
                }
                if (result.changes != null && !result.changes.isEmpty()) {
                    changes.addAll(result.changes);
                }
                if (result.changedUserIds != null && !result.changedUserIds.isEmpty()) {
                    changedUserIds.addAll(result.changedUserIds);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.warn("作物状态调度线程被中断", ex);
                return;
            } catch (ExecutionException ex) {
                log.warn("作物状态分片任务执行失败", ex);
            }
        }

        if (!changes.isEmpty()) {
            ApplyResult applyResult = applyChanges(crops, changes, now);
            if (!applyResult.changedCrops.isEmpty()) {
                userCropDao.saveAll(applyResult.changedCrops);
            }
            if (!applyResult.bugSpawnLogs.isEmpty()) {
                userCropActionLogDao.saveAll(applyResult.bugSpawnLogs);
            }
        }
        if (!changedUserIds.isEmpty()) {
            farmRealtimePushService.pushOverviewToOnlineUsers(changedUserIds);
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        schedulerExecutor.shutdownNow();
    }

    private CropRuntimeContext buildRuntimeContext(List<UserCrop> crops) {
        CropRuntimeContext context = new CropRuntimeContext();

        List<Long> plotIds = crops.stream().map(UserCrop::getPlotId).distinct().collect(Collectors.toList());
        List<Long> seedTypeIds = crops.stream().map(UserCrop::getSeedTypeId).distinct().collect(Collectors.toList());

        List<UserPlot> plots = plotIds.isEmpty()
                ? List.of()
                : userPlotDao.findByIdInAndIsDeletedFalse(plotIds);
        context.plotById = plots.stream().collect(Collectors.toMap(UserPlot::getId, v1 -> v1, (a1, b1) -> a1));

        List<Long> soilTypeIds = plots.stream().map(UserPlot::getSoilTypeId).distinct().collect(Collectors.toList());
        List<SoilType> soils = soilTypeIds.isEmpty()
                ? List.of()
                : soilTypeDao.findByIdInAndIsDeletedFalse(soilTypeIds);
        context.soilById = soils.stream().collect(Collectors.toMap(SoilType::getId, v -> v, (a, b) -> a));

        List<SeedType> seedTypes = seedTypeIds.isEmpty()
                ? List.of()
                : seedTypeDao.findByIdInAndIsDeletedFalse(seedTypeIds);
        context.seedTypeById = seedTypes.stream().collect(Collectors.toMap(SeedType::getId, v -> v, (a, b) -> a));

        List<SeedGrowthStage> stageRows = seedTypeIds.isEmpty()
                ? List.of()
                : seedGrowthStageDao.findBySeedTypeIdInAndIsDeletedFalseOrderBySeedTypeIdAscStageIndexAsc(seedTypeIds);
        Map<Long, List<SeedGrowthStage>> stageBySeedTypeId = new HashMap<>();
        for (SeedGrowthStage row : stageRows) {
            stageBySeedTypeId.computeIfAbsent(row.getSeedTypeId(), k -> new ArrayList<>()).add(row);
        }
        context.stageBySeedTypeId = stageBySeedTypeId;
        return context;
    }

    private PartitionResult processPartition(List<UserCrop> partition, CropRuntimeContext context, OffsetDateTime now) {
        List<CropRuntimeChange> changes = new ArrayList<>();
        Set<Long> changedUserIds = new HashSet<>();

        for (UserCrop crop : partition) {
            Short runtimeStatus = calculateGrowStatus(crop, now);
            StageEvolutionResult stageEvolution = evolveStageAndBug(crop, context, runtimeStatus, now);

            boolean statusChanged = runtimeStatus != null && !runtimeStatus.equals(crop.getGrowStatus());
            boolean stageChanged = stageEvolution.changedStage;
            boolean bugChanged = stageEvolution.changedBug;

            if (!statusChanged && !stageChanged && !bugChanged) {
                continue;
            }

            CropRuntimeChange change = new CropRuntimeChange();
            change.cropId = crop.getId();
            change.runtimeStatus = runtimeStatus;
            change.nextStageIndex = stageEvolution.nextStageIndex;
            change.nextStageStartedAt = stageEvolution.nextStageStartedAt;
            change.nextBugCount = stageEvolution.nextBugCount;
            change.lastBugAt = stageEvolution.lastBugAt;
            change.changedStage = stageChanged;
            change.changedBug = bugChanged;
            change.bugAddedCount = stageEvolution.bugAddedCount;
            change.bugCountBefore = stageEvolution.bugCountBefore;
            change.enteredStages = stageEvolution.enteredStages;
            changes.add(change);

            changedUserIds.add(crop.getUserId());
        }

        PartitionResult result = new PartitionResult();
        result.changes = changes;
        result.changedUserIds = changedUserIds;
        return result;
    }

    private StageEvolutionResult evolveStageAndBug(UserCrop crop, CropRuntimeContext context, Short runtimeStatus, OffsetDateTime now) {
        StageEvolutionResult result = new StageEvolutionResult();
        result.nextStageIndex = crop.getCurrentStageIndex();
        result.nextStageStartedAt = crop.getStageStartedAt();
        result.nextBugCount = safeShort(crop.getBugCount());
        result.lastBugAt = crop.getLastBugAt();
        result.bugAddedCount = 0;
        result.enteredStages = List.of();

        List<SeedGrowthStage> stages = context.stageBySeedTypeId.get(crop.getSeedTypeId());
        if (stages == null || stages.isEmpty()) {
            return result;
        }

        short currentStageIndex = normalizeStageIndex(stages, crop.getCurrentStageIndex());
        int currentPos = findStagePos(stages, currentStageIndex);
        if (currentPos < 0) {
            currentPos = 0;
            currentStageIndex = stages.getFirst().getStageIndex();
        }

        OffsetDateTime stageStartAt = crop.getStageStartedAt();
        if (stageStartAt == null) {
            stageStartAt = crop.getPlantedAt() == null ? now : crop.getPlantedAt();
        }

        BigDecimal multiplier = resolveSoilMultiplier(crop.getPlotId(), context);
        short nextStageIndex = currentStageIndex;
        int pos = currentPos;
        OffsetDateTime cursor = stageStartAt;
        List<Short> enteredStages = new ArrayList<>();

        while (pos < stages.size() - 1) {
            int adjustedDuration = adjustDurationSeconds(stages.get(pos).getDurationSeconds(), multiplier);
            boolean reachedNextStage = adjustedDuration <= 0 || !now.isBefore(cursor.plusSeconds(adjustedDuration));
            if (!reachedNextStage) {
                break;
            }
            if (adjustedDuration > 0) {
                cursor = cursor.plusSeconds(adjustedDuration);
            }
            pos++;
            nextStageIndex = stages.get(pos).getStageIndex();
            enteredStages.add(nextStageIndex);
        }

        boolean forcedAdvance = false;
        if ((CropStatus.isRipe(runtimeStatus) || CropStatus.isWithered(runtimeStatus)) && pos < stages.size() - 1) {
            while (pos < stages.size() - 1) {
                pos++;
                nextStageIndex = stages.get(pos).getStageIndex();
                enteredStages.add(nextStageIndex);
            }
            forcedAdvance = true;
        }

        result.nextStageIndex = nextStageIndex;
        result.nextStageStartedAt = nextStageIndex == currentStageIndex
                ? stageStartAt
                : (forcedAdvance ? now : cursor);
        result.changedStage = nextStageIndex != currentStageIndex;
        result.enteredStages = new ArrayList<>(enteredStages);

        SeedType seedType = context.seedTypeById.get(crop.getSeedTypeId());
        short maxBugLimit = seedType == null ? (short) 0 : safeShort(seedType.getMaxBugLimit());
        short currentBugCount = safeShort(crop.getBugCount());
        short nextBugCount = currentBugCount;
        int bugAddedCount = 0;
        if (maxBugLimit > 0 && !enteredStages.isEmpty() && !CropStatus.isWithered(runtimeStatus)) {
            for (Short stageIndex : enteredStages) {
                if (nextBugCount >= maxBugLimit) {
                    break;
                }
                SeedGrowthStage stage = getStageByIndex(stages, stageIndex);
                if (stage == null) {
                    continue;
                }
                double probability = normalizeProbability(stage.getBugProbability());
                if (probability <= 0D) {
                    continue;
                }
                if (ThreadLocalRandom.current().nextDouble() < probability) {
                    nextBugCount++;
                    bugAddedCount++;
                }
            }
        }

        result.nextBugCount = nextBugCount;
        result.bugAddedCount = bugAddedCount;
        result.bugCountBefore = currentBugCount;
        result.changedBug = nextBugCount != currentBugCount;
        if (result.changedBug && nextBugCount > currentBugCount) {
            result.lastBugAt = now;
        }
        return result;
    }

    private ApplyResult applyChanges(List<UserCrop> allCrops, List<CropRuntimeChange> changes, OffsetDateTime now) {
        Map<Long, UserCrop> cropMap = new HashMap<>(allCrops.size());
        allCrops.forEach(crop -> cropMap.put(crop.getId(), crop));

        List<UserCrop> changedCrops = new ArrayList<>(changes.size());
        List<UserCropActionLog> bugSpawnLogs = new ArrayList<>();
        for (CropRuntimeChange change : changes) {
            UserCrop crop = cropMap.get(change.cropId);
            if (crop == null) {
                continue;
            }

            boolean touched = false;
            if (change.runtimeStatus != null && !change.runtimeStatus.equals(crop.getGrowStatus())) {
                crop.setGrowStatus(change.runtimeStatus);
                if (CropStatus.isRipe(change.runtimeStatus) && crop.getMaturedAt() == null) {
                    crop.setMaturedAt(now);
                }
                if (CropStatus.isWithered(change.runtimeStatus) && crop.getWitheredAt() == null) {
                    crop.setWitheredAt(now);
                }
                touched = true;
            }

            if (change.changedStage) {
                crop.setCurrentStageIndex(change.nextStageIndex);
                crop.setStageStartedAt(change.nextStageStartedAt == null ? now : change.nextStageStartedAt);
                touched = true;
            }

            if (change.changedBug) {
                crop.setBugCount(change.nextBugCount);
                crop.setLastBugAt(change.lastBugAt);
                touched = true;
            }

            if (touched) {
                gameplayCoreService.touchForUpdate(crop, crop.getUserId(), now);
                changedCrops.add(crop);
            }
            if (change.bugAddedCount > 0) {
                bugSpawnLogs.add(gameplayCoreService.buildCropActionLog(
                        crop.getUserId(),
                        crop.getPlotId(),
                        crop.getId(),
                        crop.getSeedTypeId(),
                        "BUG_SPAWN",
                        "SUCCESS",
                        now,
                        buildBugSpawnSnapshot(change)
                ));
            }
        }
        ApplyResult result = new ApplyResult();
        result.changedCrops = changedCrops;
        result.bugSpawnLogs = bugSpawnLogs;
        return result;
    }

    static Short calculateGrowStatus(UserCrop crop, OffsetDateTime now) {
        if (crop == null) {
            return null;
        }
        if (crop.getExpectedWitheredAt() != null && now.isAfter(crop.getExpectedWitheredAt())) {
            return CropStatus.WITHERED.getCode();
        }
        if (crop.getExpectedRipeAt() != null && !now.isBefore(crop.getExpectedRipeAt())) {
            return CropStatus.RIPE.getCode();
        }
        return CropStatus.growingCodeWhenNull(crop.getGrowStatus());
    }

    private List<List<UserCrop>> partition(List<UserCrop> crops, int chunkSize) {
        List<List<UserCrop>> parts = new ArrayList<>();
        if (crops == null || crops.isEmpty()) {
            return parts;
        }
        int size = crops.size();
        for (int from = 0; from < size; from += chunkSize) {
            int to = Math.min(from + chunkSize, size);
            parts.add(crops.subList(from, to));
        }
        return parts;
    }

    private short normalizeStageIndex(List<SeedGrowthStage> stages, Short currentStageIndex) {
        if (stages == null || stages.isEmpty()) {
            return 1;
        }
        short first = stages.getFirst().getStageIndex();
        short last = stages.getLast().getStageIndex();
        short value = currentStageIndex == null ? first : currentStageIndex;
        if (value < first) return first;
        if (value > last) return last;
        return value;
    }

    private int findStagePos(List<SeedGrowthStage> stages, short stageIndex) {
        for (int i = 0; i < stages.size(); i++) {
            if (safeShort(stages.get(i).getStageIndex()) == stageIndex) {
                return i;
            }
        }
        return -1;
    }

    private SeedGrowthStage getStageByIndex(List<SeedGrowthStage> stages, Short stageIndex) {
        if (stageIndex == null) {
            return null;
        }
        for (SeedGrowthStage stage : stages) {
            if (safeShort(stage.getStageIndex()) == stageIndex) {
                return stage;
            }
        }
        return null;
    }

    private BigDecimal resolveSoilMultiplier(Long plotId, CropRuntimeContext context) {
        if (plotId == null) {
            return BigDecimal.ONE;
        }
        UserPlot plot = context.plotById.get(plotId);
        if (plot == null) {
            return BigDecimal.ONE;
        }
        SoilType soilType = context.soilById.get(plot.getSoilTypeId());
        if (soilType == null || soilType.getGrowSpeedMultiplier() == null || soilType.getGrowSpeedMultiplier().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE;
        }
        return soilType.getGrowSpeedMultiplier();
    }

    private int adjustDurationSeconds(Integer rawDuration, BigDecimal multiplier) {
        int raw = safeInteger(rawDuration);
        if (raw <= 0) {
            return 0;
        }
        BigDecimal safeMultiplier = (multiplier == null || multiplier.compareTo(BigDecimal.ZERO) <= 0) ? BigDecimal.ONE : multiplier;
        BigDecimal adjusted = BigDecimal.valueOf(raw).divide(safeMultiplier, 0, RoundingMode.HALF_UP);
        return Math.max(0, adjusted.intValue());
    }

    private double normalizeProbability(BigDecimal probability) {
        if (probability == null) {
            return 0D;
        }
        double value = probability.doubleValue();
        if (value <= 0D) {
            return 0D;
        }
        return Math.min(value, 1D);
    }

    private String buildBugSpawnSnapshot(CropRuntimeChange change) {
        String enteredStagesJson = toStageArrayJson(change.enteredStages);
        return "{\"reason\":\"STAGE_TRANSITION\""
                + ",\"enteredStages\":" + enteredStagesJson
                + ",\"bugAddedCount\":" + change.bugAddedCount
                + ",\"bugCountBefore\":" + change.bugCountBefore
                + ",\"bugCountAfter\":" + safeShort(change.nextBugCount)
                + "}";
    }

    private String toStageArrayJson(List<Short> stages) {
        if (stages == null || stages.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < stages.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(safeShort(stages.get(i)));
        }
        sb.append(']');
        return sb.toString();
    }

    private int safeInteger(Integer value) {
        return gameplayCoreService.safeInteger(value);
    }

    private short safeShort(Short value) {
        return gameplayCoreService.safeShort(value);
    }

    private static class CropRuntimeContext {
        private Map<Long, UserPlot> plotById = new HashMap<>();
        private Map<Long, SoilType> soilById = new HashMap<>();
        private Map<Long, SeedType> seedTypeById = new HashMap<>();
        private Map<Long, List<SeedGrowthStage>> stageBySeedTypeId = new HashMap<>();
    }

    private static class CropRuntimeChange {
        private Long cropId;
        private Short runtimeStatus;
        private Short nextStageIndex;
        private OffsetDateTime nextStageStartedAt;
        private Short nextBugCount;
        private OffsetDateTime lastBugAt;
        private boolean changedStage;
        private boolean changedBug;
        private int bugAddedCount;
        private short bugCountBefore;
        private List<Short> enteredStages = List.of();
    }

    private static class StageEvolutionResult {
        private Short nextStageIndex;
        private OffsetDateTime nextStageStartedAt;
        private Short nextBugCount;
        private OffsetDateTime lastBugAt;
        private boolean changedStage;
        private boolean changedBug;
        private int bugAddedCount;
        private short bugCountBefore;
        private List<Short> enteredStages = List.of();
    }

    private static class PartitionResult {
        private List<CropRuntimeChange> changes;
        private Set<Long> changedUserIds;
    }

    private static class ApplyResult {
        private List<UserCrop> changedCrops = new ArrayList<>();
        private List<UserCropActionLog> bugSpawnLogs = new ArrayList<>();
    }

    private static class SchedulerThreadFactory implements ThreadFactory {
        private final AtomicInteger index = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("crop-status-scheduler-" + index.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
