package net.momirealms.craftengine.bukkit.entity.furniture.element;

import net.momirealms.craftengine.bukkit.entity.data.TextDisplayEntityData;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.entity.display.TextDisplayAlignment;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfig;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfigFactory;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.NetworkTextReplaceContext;
import net.momirealms.craftengine.core.plugin.context.PlayerContext;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.Color;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class TextDisplayFurnitureElementConfig implements FurnitureElementConfig<TextDisplayFurnitureElement> {
    public static final FurnitureElementConfigFactory<TextDisplayFurnitureElement> FACTORY = new Factory();
    public final Function<Player, List<Object>> metadata;
    public final String text;
    public final Vector3f scale;
    public final Vector3f position;
    public final Vector3f translation;
    public final float xRot;
    public final float yRot;
    public final Quaternionf rotation;
    public final Billboard billboard;
    public final float shadowRadius;
    public final float shadowStrength;
    public final Color glowColor;
    public final int blockLight;
    public final int skyLight;
    public final float viewRange;
    public final int lineWidth;
    public final int backgroundColor;
    public final byte opacity;
    public final boolean hasShadow;
    public final boolean isSeeThrough;
    public final boolean useDefaultBackgroundColor;
    public final TextDisplayAlignment alignment;
    public final Predicate<PlayerContext> predicate;
    public final boolean hasCondition;

    private TextDisplayFurnitureElementConfig(String text,
                                             Vector3f scale,
                                             Vector3f position,
                                             Vector3f translation,
                                             float xRot,
                                             float yRot,
                                             Quaternionf rotation,
                                              Billboard billboard,
                                             float shadowRadius,
                                             float shadowStrength,
                                             @Nullable Color glowColor,
                                             int blockLight,
                                             int skyLight,
                                             float viewRange,
                                             int lineWidth,
                                             int backgroundColor,
                                             byte opacity,
                                             boolean hasShadow,
                                             boolean isSeeThrough,
                                             boolean useDefaultBackgroundColor,
                                             TextDisplayAlignment alignment,
                                             Predicate<PlayerContext> predicate,
                                             boolean hasCondition) {
        this.text = text;
        this.scale = scale;
        this.position = position;
        this.translation = translation;
        this.xRot = xRot;
        this.yRot = yRot;
        this.rotation = rotation;
        this.billboard = billboard;
        this.shadowRadius = shadowRadius;
        this.shadowStrength = shadowStrength;
        this.glowColor = glowColor;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
        this.viewRange = viewRange;
        this.lineWidth = lineWidth;
        this.backgroundColor = backgroundColor;
        this.opacity = opacity;
        this.hasShadow = hasShadow;
        this.useDefaultBackgroundColor = useDefaultBackgroundColor;
        this.alignment = alignment;
        this.isSeeThrough = isSeeThrough;
        this.hasCondition = hasCondition;
        this.predicate = predicate;
        this.metadata = (player) -> {
            List<Object> dataValues = new ArrayList<>();
            if (glowColor != null) {
                TextDisplayEntityData.SharedFlags.addEntityData((byte) 0x40, dataValues);
                TextDisplayEntityData.GlowColorOverride.addEntityData(glowColor.color(), dataValues);
            }
            TextDisplayEntityData.Scale.addEntityDataIfNotDefaultValue(this.scale, dataValues);
            TextDisplayEntityData.RotationLeft.addEntityDataIfNotDefaultValue(this.rotation, dataValues);
            TextDisplayEntityData.BillboardConstraints.addEntityDataIfNotDefaultValue(this.billboard.id(), dataValues);
            TextDisplayEntityData.Translation.addEntityDataIfNotDefaultValue(this.translation, dataValues);
            TextDisplayEntityData.ShadowRadius.addEntityDataIfNotDefaultValue(this.shadowRadius, dataValues);
            TextDisplayEntityData.ShadowStrength.addEntityDataIfNotDefaultValue(this.shadowStrength, dataValues);
            TextDisplayEntityData.Text.addEntityData(ComponentUtils.adventureToMinecraft(AdventureHelper.miniMessage().deserialize(this.text, NetworkTextReplaceContext.of(player).tagResolvers())), dataValues);
            TextDisplayEntityData.LineWidth.addEntityDataIfNotDefaultValue(this.lineWidth, dataValues);
            TextDisplayEntityData.BackgroundColor.addEntityDataIfNotDefaultValue(this.backgroundColor, dataValues);
            TextDisplayEntityData.TextOpacity.addEntityDataIfNotDefaultValue(this.opacity, dataValues);
            TextDisplayEntityData.TextDisplayMasks.addEntityDataIfNotDefaultValue(TextDisplayEntityData.encodeMask(this.hasShadow, this.isSeeThrough, this.useDefaultBackgroundColor, this.alignment), dataValues);
            if (this.blockLight != -1 && this.skyLight != -1) {
                TextDisplayEntityData.BrightnessOverride.addEntityData(this.blockLight << 4 | this.skyLight << 20, dataValues);
            }
            TextDisplayEntityData.ViewRange.addEntityDataIfNotDefaultValue((float) (this.viewRange * player.displayEntityViewDistance()), dataValues);
            return dataValues;
        };
    }

    @Override
    public TextDisplayFurnitureElement create(@NotNull Furniture furniture) {
        return new TextDisplayFurnitureElement(furniture, this);
    }

    private static class Factory implements FurnitureElementConfigFactory<TextDisplayFurnitureElement> {

        @Override
        public TextDisplayFurnitureElementConfig create(ConfigSection section) {
            ConfigSection brightness = section.getSection("brightness");
            List<Condition<PlayerContext>> conditions = section.parseSectionList(CommonConditions::fromConfig, "conditions");
            return new TextDisplayFurnitureElementConfig(
                    section.getNonNullString("text"),
                    section.getVector3f(ConfigConstants.NORMAL_SCALE, "scale"),
                    section.getVector3f(ConfigConstants.ZERO_VECTOR3, "position"),
                    section.getVector3f(ConfigConstants.ZERO_VECTOR3, "translation"),
                    section.getFloat(0f, "pitch"),
                    section.getFloat(0f, "yaw"),
                    section.getQuaternionf(ConfigConstants.ZERO_QUATERNION, "rotation"),
                    section.getEnum(Billboard.FIXED, Billboard.class, "billboard"),
                    section.getFloat(0f, "shadow_radius", "shadow-radius"),
                    section.getFloat(1f, "shadow_strength", "shadow-strength"),
                    section.getValue(ConfigValue::getAsColor, "glow_color", "glow-color"),
                    brightness != null ? brightness.getInt(-1, "block_light", "block-light") : -1,
                    brightness != null ? brightness.getInt(-1, "sky_light", "sky-light") : -1,
                    section.getFloat(1f, "view_range", "view-range"),
                    section.getInt(200, "line_width", "line-width"),
                    section.getOrDefault(o -> Color.fromStrings(o.toString().split(",")).color(), 0x40000000, "background-color"),
                    (byte) section.getInt(-1, "opacity","text_opacity", "text-opacity"),
                    section.getBoolean("shadow", "has_shadow", "has-shadow"),
                    section.getBoolean("is_see_through", "is-see-through"),
                    section.getBoolean("use_default_background_color", "use-default-background-color"),
                    section.getEnum(TextDisplayAlignment.CENTER, TextDisplayAlignment.class, "alignment"),
                    MiscUtils.allOf(conditions),
                    !conditions.isEmpty()
            );
        }
    }
}
