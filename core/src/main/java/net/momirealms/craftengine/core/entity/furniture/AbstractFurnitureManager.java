package net.momirealms.craftengine.core.entity.furniture;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.momirealms.craftengine.core.entity.culling.CullingData;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviors;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfig;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfigs;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfigs;
import net.momirealms.craftengine.core.entity.furniture.tick.TickingFurniture;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.pack.PendingConfigSection;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.*;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStage;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStages;
import net.momirealms.craftengine.core.plugin.context.CommonFunctions;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerTask;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.TickersList;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.nio.file.Path;
import java.util.*;

public abstract class AbstractFurnitureManager implements FurnitureManager {
    protected final Map<Key, CustomFurniture> byId = new HashMap<>();
    private final CraftEngine plugin;
    private final FurnitureParser furnitureParser;
    // Cached command suggestions
    private final List<Suggestion> cachedSuggestions = new ArrayList<>();

    protected final Int2ObjectOpenHashMap<TickingFurniture> syncTickers = new Int2ObjectOpenHashMap<>(256, 0.5f);
    protected final Int2ObjectOpenHashMap<TickingFurniture> asyncTickers = new Int2ObjectOpenHashMap<>(256, 0.5f);
    protected final TickersList<TickingFurniture> syncTickingFurniture = new TickersList<>();
    protected final List<TickingFurniture> pendingSyncTickingFurniture = new ArrayList<>();
    protected final TickersList<TickingFurniture> asyncTickingFurniture = new TickersList<>();
    protected final List<TickingFurniture> pendingAsyncTickingFurniture = new ArrayList<>();
    private boolean isTickingSyncFurniture = false;
    private boolean isTickingAsyncFurniture = false;

    protected SchedulerTask syncTickTask;
    protected SchedulerTask asyncTickTask;

    public AbstractFurnitureManager(CraftEngine plugin) {
        this.plugin = plugin;
        this.furnitureParser = new FurnitureParser();
    }

    @Override
    public FurnitureParser parser() {
        return this.furnitureParser;
    }

    @Override
    public void delayedLoad() {
        this.initSuggestions();
    }

    @Override
    public void initSuggestions() {
        this.cachedSuggestions.clear();
        for (Key key : this.byId.keySet()) {
            this.cachedSuggestions.add(Suggestion.suggestion(key.toString()));
        }
    }

    @Override
    public Collection<Suggestion> cachedSuggestions() {
        return Collections.unmodifiableCollection(this.cachedSuggestions);
    }

    @Override
    public Optional<CustomFurniture> furnitureById(Key id) {
        return Optional.ofNullable(this.byId.get(id));
    }

    @Override
    public Map<Key, CustomFurniture> loadedFurniture() {
        return Collections.unmodifiableMap(this.byId);
    }

    private void syncTick() {
        this.isTickingSyncFurniture = true;
        if (!this.pendingSyncTickingFurniture.isEmpty()) {
            this.syncTickingFurniture.addAll(this.pendingSyncTickingFurniture);
            this.pendingSyncTickingFurniture.clear();
        }
        if (!this.syncTickingFurniture.isEmpty()) {
            Object[] entities = this.syncTickingFurniture.elements();
            for (int i = 0, size = this.syncTickingFurniture.size(); i < size; i++) {
                TickingFurniture entity = (TickingFurniture) entities[i];
                if (entity.isValid()) {
                    entity.tick();
                } else {
                    this.syncTickingFurniture.markAsRemoved(i);
                    this.syncTickers.remove(entity.entityId());
                }
            }
            this.syncTickingFurniture.removeMarkedEntries();
        }
        this.isTickingSyncFurniture = false;
    }

    private void asyncTick() {
        this.isTickingAsyncFurniture = true;
        if (!this.pendingAsyncTickingFurniture.isEmpty()) {
            this.asyncTickingFurniture.addAll(this.pendingAsyncTickingFurniture);
            this.pendingAsyncTickingFurniture.clear();
        }
        if (!this.asyncTickingFurniture.isEmpty()) {
            Object[] entities = this.asyncTickingFurniture.elements();
            for (int i = 0, size = this.asyncTickingFurniture.size(); i < size; i++) {
                TickingFurniture entity = (TickingFurniture) entities[i];
                if (entity.isValid()) {
                    entity.tick();
                } else {
                    this.asyncTickingFurniture.markAsRemoved(i);
                    this.asyncTickers.remove(entity.entityId());
                }
            }
            this.asyncTickingFurniture.removeMarkedEntries();
        }
        this.isTickingAsyncFurniture = false;
    }

    public synchronized void addSyncFurnitureTicker(TickingFurniture ticker) {
        if (this.isTickingSyncFurniture) {
            this.pendingSyncTickingFurniture.add(ticker);
        } else {
            this.syncTickingFurniture.add(ticker);
        }
    }

    public synchronized void addAsyncFurnitureTicker(TickingFurniture ticker) {
        if (this.isTickingAsyncFurniture) {
            this.pendingAsyncTickingFurniture.add(ticker);
        } else {
            this.asyncTickingFurniture.add(ticker);
        }
    }

    @Override
    public void delayedInit() {
        if (this.syncTickTask == null || this.syncTickTask.cancelled())
            this.syncTickTask = CraftEngine.instance().scheduler().sync().runRepeating(this::syncTick, 1, 1);
        if (this.asyncTickTask == null || this.asyncTickTask.cancelled())
            this.asyncTickTask = CraftEngine.instance().scheduler().sync().runAsyncRepeating(this::asyncTick, 1, 1);
    }

    @Override
    public void disable() {
        if (this.syncTickTask != null && !this.syncTickTask.cancelled())
            this.syncTickTask.cancel();
        if (this.asyncTickTask != null && !this.asyncTickTask.cancelled())
            this.asyncTickTask.cancel();
    }

    @Override
    public void unload() {
        this.byId.clear();
    }

    protected abstract FurnitureHitBoxConfig<?> defaultHitBox();

    public final class FurnitureParser extends IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[] { "furniture" };
        private final List<PendingConfigSection> pendingConfigSections = new ArrayList<>();

        public void addPendingConfigSection(PendingConfigSection section) {
            this.pendingConfigSections.add(section);
        }

        @SuppressWarnings("DuplicatedCode")
        @Override
        public void preProcess() {
            if (!this.pendingConfigSections.isEmpty()) {
                for (PendingConfigSection section : this.pendingConfigSections) {
                    ResourceConfigUtils.runCatching(
                            section.path(),
                            section.section().path(),
                            () -> parseSection(section.pack(), section.path(), section.id(), section.section()),
                            super.errorHandler
                    );
                }
                this.pendingConfigSections.clear();
            }
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public int count() {
            return AbstractFurnitureManager.this.byId.size();
        }

        @Override
        public LoadingStage loadingStage() {
            return LoadingStages.FURNITURE;
        }

        @Override
        public List<LoadingStage> dependencies() {
            return List.of(LoadingStages.ITEM);
        }

        @Override
        public void parseSection(Pack pack, Path path, Key id, ConfigSection section) {
            ConfigSection variantsSection = section.getNonNullSection("variant", "variants");
            Map<String, FurnitureVariant> variants = new LinkedHashMap<>();
            for (String variant : variantsSection.keySet()) {
                ConfigSection variantSection = variantsSection.getNonNullSection(variant);

                // 掉落物偏移
                Vector3f lootSpawnOffset =variantSection.getVector3f(ConfigConstants.ZERO_VECTOR3, "loot_spawn_offset", "loot-spawn-offset");

                // 外部模型
                String blueprint = variantSection.getString("blueprint", /* 旧版为具体插件，新版使用统一的 blueprint */ "better-model", "model-engine");
                Optional<ExternalModel> externalModel = Optional.ofNullable(blueprint).map(it -> AbstractFurnitureManager.this.plugin.compatibilityManager().createModel(it));

                // 元素与碰撞箱
                List<FurnitureElementConfig<?>> elements = variantSection.parseSectionList(FurnitureElementConfigs::fromConfig, "elements");
                List<FurnitureHitBoxConfig<?>> hitboxes = variantSection.parseSectionList(FurnitureHitBoxConfigs::fromConfig, "hitboxes");
                if (hitboxes.isEmpty() && externalModel.isEmpty()) {
                    hitboxes = List.of(defaultHitBox());
                }

                variants.put(variant, new FurnitureVariant(
                        variant,
                        parseCullingData(section.getValue("entity_culling", "entity-culling")),
                        elements.toArray(new FurnitureElementConfig[0]),
                        hitboxes.toArray(new FurnitureHitBoxConfig[0]),
                        externalModel,
                        lootSpawnOffset
                ));
            }

            CustomFurniture furniture = CustomFurniture.builder()
                    .id(id)
                    .settings(FurnitureSettings.applyModifiers(FurnitureSettings.of().itemId(id), section.getSection("settings")))
                    .variants(variants)
                    .events(CommonFunctions.parseEvents(section))
                    .lootTable(section.getValue(v -> LootTable.fromConfig(v.getAsSection()), "loot", "loots"))
                    .build();

            // TODO 复合行为
            ConfigSection behaviorSection = section.getSection("behavior", "behaviors");
            if (behaviorSection != null) {
                ((CustomFurnitureImpl) furniture).setBehavior(FurnitureBehaviors.fromConfig(furniture,behaviorSection));
            }
            AbstractFurnitureManager.this.byId.put(id, furniture);
        }

        private CullingData parseCullingData(@Nullable ConfigValue value) {
            if (value != null) {
                if (value.is(Boolean.class) && !value.getAsBoolean()) {
                    return null;
                } else if (value.is(Map.class)) {
                    ConfigSection section = value.getAsSection();
                    return new CullingData(
                            section.getAABB("aabb"),
                            section.getInt(Config.entityCullingViewDistance(), "view_distance", "view-distance"),
                            section.getDouble(0.25, "aabb_expansion", "aabb-expansion"),
                            section.getBoolean(true, "ray_tracing", "ray-tracing")
                    );
                }
            }
            return new CullingData(null, Config.entityCullingViewDistance(), 0.25, true);
        }
    }
}
