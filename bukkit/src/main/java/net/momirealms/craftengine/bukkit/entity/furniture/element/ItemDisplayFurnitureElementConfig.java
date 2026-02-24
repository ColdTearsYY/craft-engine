package net.momirealms.craftengine.bukkit.entity.furniture.element;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.entity.display.ItemDisplayContext;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureColorSource;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfig;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfigFactory;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.item.data.FireworkExplosion;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.PlayerContext;
import net.momirealms.craftengine.core.util.Color;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class ItemDisplayFurnitureElementConfig implements FurnitureElementConfig<ItemDisplayFurnitureElement> {
    public static final FurnitureElementConfigFactory<ItemDisplayFurnitureElement> FACTORY = new Factory();
    public final BiFunction<Player, FurnitureColorSource, List<Object>> metadata;
    public final Key itemId;
    public final Vector3f scale;
    public final Vector3f position;
    public final Vector3f translation;
    public final float xRot;
    public final float yRot;
    public final Quaternionf rotation;
    public final ItemDisplayContext displayContext;
    public final Billboard billboard;
    public final float shadowRadius;
    public final float shadowStrength;
    public final boolean applyDyedColor;
    public final Color glowColor;
    public final int blockLight;
    public final int skyLight;
    public final float viewRange;
    public final Predicate<PlayerContext> predicate;
    public final boolean hasCondition;

    private ItemDisplayFurnitureElementConfig(Key itemId,
                                             Vector3f scale,
                                             Vector3f position,
                                             Vector3f translation,
                                             float xRot,
                                             float yRot,
                                             Quaternionf rotation,
                                             ItemDisplayContext displayContext,
                                             Billboard billboard,
                                             float shadowRadius,
                                             float shadowStrength,
                                             boolean applyDyedColor,
                                             @Nullable Color glowColor,
                                             int blockLight,
                                             int skyLight,
                                             float viewRange,
                                             Predicate<PlayerContext> predicate,
                                             boolean hasCondition) {
        this.scale = scale;
        this.position = position;
        this.translation = translation;
        this.xRot = xRot;
        this.yRot = yRot;
        this.rotation = rotation;
        this.displayContext = displayContext;
        this.billboard = billboard;
        this.shadowRadius = shadowRadius;
        this.shadowStrength = shadowStrength;
        this.applyDyedColor = applyDyedColor;
        this.itemId = itemId;
        this.glowColor = glowColor;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
        this.viewRange = viewRange;
        this.predicate = predicate;
        this.hasCondition = hasCondition;
        BiFunction<Player, FurnitureColorSource, Item<?>> itemFunction = (player, colorSource) -> {
            Item<ItemStack> wrappedItem = BukkitItemManager.instance().createWrappedItem(itemId, player);
            if (applyDyedColor && colorSource != null && wrappedItem != null) {
                Optional.ofNullable(colorSource.dyedColor()).ifPresent(wrappedItem::dyedColor);
                Optional.ofNullable(colorSource.fireworkColors()).ifPresent(colors -> wrappedItem.fireworkExplosion(new FireworkExplosion(
                        FireworkExplosion.Shape.SMALL_BALL,
                        new IntArrayList(colors),
                        new IntArrayList(),
                        false,
                        false
                )));
            }
            return Optional.ofNullable(wrappedItem).orElseGet(() -> BukkitItemManager.instance().createWrappedItem(ItemKeys.BARRIER, null));
        };
        this.metadata = (player, source) -> {
            List<Object> dataValues = new ArrayList<>();
            if (glowColor != null) {
                ItemDisplayEntityData.SharedFlags.addEntityData((byte) 0x40, dataValues);
                ItemDisplayEntityData.GlowColorOverride.addEntityData(glowColor.color(), dataValues);
            }
            ItemDisplayEntityData.DisplayedItem.addEntityData(itemFunction.apply(player, source).getLiteralObject(), dataValues);
            ItemDisplayEntityData.Scale.addEntityDataIfNotDefaultValue(this.scale, dataValues);
            ItemDisplayEntityData.RotationLeft.addEntityDataIfNotDefaultValue(this.rotation, dataValues);
            ItemDisplayEntityData.BillboardConstraints.addEntityDataIfNotDefaultValue(this.billboard.id(), dataValues);
            ItemDisplayEntityData.Translation.addEntityDataIfNotDefaultValue(this.translation, dataValues);
            ItemDisplayEntityData.DisplayType.addEntityDataIfNotDefaultValue(this.displayContext.id(), dataValues);
            ItemDisplayEntityData.ShadowRadius.addEntityDataIfNotDefaultValue(this.shadowRadius, dataValues);
            ItemDisplayEntityData.ShadowStrength.addEntityDataIfNotDefaultValue(this.shadowStrength, dataValues);
            if (this.blockLight != -1 && this.skyLight != -1) {
                ItemDisplayEntityData.BrightnessOverride.addEntityData(this.blockLight << 4 | this.skyLight << 20, dataValues);
            }
            ItemDisplayEntityData.ViewRange.addEntityDataIfNotDefaultValue((float) (this.viewRange * player.displayEntityViewDistance()), dataValues);
            return dataValues;
        };
    }

    @Override
    public ItemDisplayFurnitureElement create(@NotNull Furniture furniture) {
        return new ItemDisplayFurnitureElement(furniture, this);
    }

    private static class Factory implements FurnitureElementConfigFactory<ItemDisplayFurnitureElement> {

        @Override
        public ItemDisplayFurnitureElementConfig create(ConfigSection section) {
            ConfigSection brightness = section.getSection("brightness");
            List<Condition<PlayerContext>> conditions = section.parseSectionList(CommonConditions::fromConfig, "conditions");
            return new ItemDisplayFurnitureElementConfig(
                    section.getNonNullIdentifier("item"),
                    section.getVector3f(ConfigConstants.NORMAL_SCALE, "scale"),
                    section.getVector3f(ConfigConstants.ZERO_VECTOR3, "position"),
                    section.getVector3f(ConfigConstants.ZERO_VECTOR3, "translation"),
                    section.getFloat(0f, "pitch"),
                    section.getFloat(0f, "yaw"),
                    section.getQuaternionf(ConfigConstants.ZERO_QUATERNION, "rotation"),
                    section.getEnum(ItemDisplayContext.NONE, ItemDisplayContext.class, "display_context", "display_transform", "display-context", "display-transform"),
                    section.getEnum(Billboard.FIXED, Billboard.class, "billboard"),
                    section.getFloat(0f, "shadow_radius", "shadow-radius"),
                    section.getFloat(1f, "shadow_strength", "shadow-strength"),
                    section.getBoolean(true, "apply_dyed_color", "apply-dyed-color"),
                    section.getValue(ConfigValue::getAsColor, "glow_color", "glow-color"),
                    brightness != null ? brightness.getInt(-1, "block_light", "block-light") : -1,
                    brightness != null ? brightness.getInt(-1, "sky_light", "sky-light") : -1,
                    section.getFloat(1f, "view_range", "view-range"),
                    MiscUtils.allOf(conditions),
                    !conditions.isEmpty()
            );
        }
    }
}
