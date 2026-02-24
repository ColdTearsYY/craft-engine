package net.momirealms.craftengine.bukkit.block.entity.renderer.element;

import com.google.common.base.Objects;
import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.entity.furniture.element.ItemDisplayFurnitureElementConfig;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigFactory;
import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.entity.display.ItemDisplayContext;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.Color;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class ItemDisplayBlockEntityElementConfig implements BlockEntityElementConfig<ItemDisplayBlockEntityElement> {
    public static final Factory FACTORY = new Factory();
    public final Function<Player, List<Object>> lazyMetadataPacket;
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
    public final Color glowColor;
    public final int blockLight;
    public final int skyLight;
    public final float viewRange;

    public ItemDisplayBlockEntityElementConfig(Key itemId,
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
                                               @Nullable Color glowColor,
                                               int blockLight,
                                               int skyLight,
                                               float viewRange) {
        this.itemId = itemId;
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
        this.glowColor = glowColor;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
        this.viewRange = viewRange;
        this.lazyMetadataPacket = player -> {
            List<Object> dataValues = new ArrayList<>();
            if (glowColor != null) {
                ItemDisplayEntityData.SharedFlags.addEntityData((byte) 0x40, dataValues);
                ItemDisplayEntityData.GlowColorOverride.addEntityData(glowColor.color(), dataValues);
            } else {
                ItemDisplayEntityData.SharedFlags.addEntityData((byte) 0x0, dataValues);
                ItemDisplayEntityData.GlowColorOverride.addEntityData(-1, dataValues);
            }
            Item<ItemStack> wrappedItem = BukkitItemManager.instance().createWrappedItem(itemId, player);
            if (wrappedItem == null) {
                wrappedItem = java.util.Objects.requireNonNull(BukkitItemManager.instance().createWrappedItem(ItemKeys.BARRIER, player));
            }
            ItemDisplayEntityData.DisplayedItem.addEntityData(wrappedItem.getLiteralObject(), dataValues);
            ItemDisplayEntityData.Scale.addEntityData(this.scale, dataValues);
            ItemDisplayEntityData.RotationLeft.addEntityData(this.rotation, dataValues);
            ItemDisplayEntityData.BillboardConstraints.addEntityData(this.billboard.id(), dataValues);
            ItemDisplayEntityData.Translation.addEntityData(this.translation, dataValues);
            ItemDisplayEntityData.DisplayType.addEntityData(this.displayContext.id(), dataValues);
            ItemDisplayEntityData.ShadowRadius.addEntityData(this.shadowRadius, dataValues);
            ItemDisplayEntityData.ShadowStrength.addEntityData(this.shadowStrength, dataValues);
            if (this.blockLight != -1 && this.skyLight != -1) {
                ItemDisplayEntityData.BrightnessOverride.addEntityData(this.blockLight << 4 | this.skyLight << 20, dataValues);
            } else {
                ItemDisplayEntityData.BrightnessOverride.addEntityData(-1, dataValues);
            }
            ItemDisplayEntityData.ViewRange.addEntityData((float) (this.viewRange * player.displayEntityViewDistance()), dataValues);
            return dataValues;
        };
    }

    @Override
    public ItemDisplayBlockEntityElement create(World world, BlockPos pos) {
        return new ItemDisplayBlockEntityElement(this, pos);
    }

    @Override
    public ItemDisplayBlockEntityElement create(World world, BlockPos pos, ItemDisplayBlockEntityElement previous) {
        return new ItemDisplayBlockEntityElement(this, pos, previous.entityId,
                previous.config.yRot != this.yRot ||
                        previous.config.xRot != this.xRot ||
                        !previous.config.position.equals(this.position)
        );
    }

    @Override
    public ItemDisplayBlockEntityElement createExact(World world, BlockPos pos, ItemDisplayBlockEntityElement previous) {
        if (!previous.config.isSamePosition(this)) {
            return null;
        }
        return new ItemDisplayBlockEntityElement(this, pos, previous.entityId, false);
    }

    @Override
    public Class<ItemDisplayBlockEntityElement> elementClass() {
        return ItemDisplayBlockEntityElement.class;
    }

    public Color glowColor() {
        return glowColor;
    }

    public Key itemId() {
        return itemId;
    }

    public Vector3f scale() {
        return this.scale;
    }

    public Vector3f translation() {
        return this.translation;
    }

    public Vector3f position() {
        return this.position;
    }

    public float yRot() {
        return this.yRot;
    }

    public float xRot() {
        return this.xRot;
    }

    public Billboard billboard() {
        return billboard;
    }

    public ItemDisplayContext displayContext() {
        return displayContext;
    }

    public Quaternionf rotation() {
        return rotation;
    }

    public float shadowRadius() {
        return shadowRadius;
    }

    public float shadowStrength() {
        return shadowStrength;
    }

    public List<Object> metadataValues(Player player) {
        return this.lazyMetadataPacket.apply(player);
    }

    public boolean isSamePosition(ItemDisplayBlockEntityElementConfig that) {
        return Float.compare(xRot, that.xRot) == 0 &&
                Float.compare(yRot, that.yRot) == 0 &&
                Objects.equal(position, that.position) &&
                Objects.equal(translation, that.translation) &&
                Objects.equal(rotation, that.rotation);
    }

    public static class Factory implements BlockEntityElementConfigFactory<ItemDisplayBlockEntityElement> {

        @Override
        public ItemDisplayBlockEntityElementConfig create(ConfigSection section) {
            ConfigSection brightness = section.getSection("brightness");
            return new ItemDisplayBlockEntityElementConfig(
                    section.getNonNullIdentifier("item"),
                    section.getVector3f(ConfigConstants.NORMAL_SCALE, "scale"),
                    section.getVector3f(ConfigConstants.CENTER_VECTOR3, "position"),
                    section.getVector3f(ConfigConstants.ZERO_VECTOR3, "translation"),
                    section.getFloat(0f, "pitch"),
                    section.getFloat(0f, "yaw"),
                    section.getQuaternionf(ConfigConstants.ZERO_QUATERNION, "rotation"),
                    section.getEnum(ItemDisplayContext.NONE, ItemDisplayContext.class, "display_context", "display_transform", "display-context", "display-transform"),
                    section.getEnum(Billboard.FIXED, Billboard.class, "billboard"),
                    section.getFloat("shadow_radius", "shadow-radius"),
                    section.getFloat(1f, "shadow_strength", "shadow-strength"),
                    section.getValue(ConfigValue::getAsColor, "glow_color", "glow-color"),
                    brightness != null ? brightness.getInt(-1, "block_light", "block-light") : -1,
                    brightness != null ? brightness.getInt(-1, "sky_light", "sky-light") : -1,
                    section.getFloat(1f, "view_range", "view-range")
            );
        }
    }
}
