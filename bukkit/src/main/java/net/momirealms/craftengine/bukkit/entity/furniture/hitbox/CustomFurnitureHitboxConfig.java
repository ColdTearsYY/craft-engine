package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.bukkit.entity.data.BaseEntityData;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.bukkit.util.RegistryUtils;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.hitbox.AbstractFurnitureHitBoxConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfigFactory;
import net.momirealms.craftengine.core.entity.seat.SeatConfig;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.collision.AABB;
import net.momirealms.craftengine.proxy.minecraft.core.registries.BuiltInRegistriesProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityDimensionsProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityTypeProxy;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CustomFurnitureHitboxConfig extends AbstractFurnitureHitBoxConfig<CustomFurnitureHitbox> {
    public static final Factory FACTORY = new Factory();
    public final float scale;
    public final Object entityType;
    public final List<Object> cachedValues = new ArrayList<>();
    public final float width;
    public final float height;

    private CustomFurnitureHitboxConfig(SeatConfig[] seats,
                                       Vector3f position,
                                       boolean canUseItemOn,
                                       boolean blocksBuilding,
                                       boolean canBeHitByProjectile,
                                       float width,
                                       float height,
                                       boolean fixed,
                                       float scale,
                                       Object type) {
        super(seats, position, canUseItemOn, blocksBuilding, canBeHitByProjectile);
        this.scale = scale;
        this.entityType = type;
        this.width = fixed ? width : width * scale;
        this.height = fixed ? height : height * scale;
        BaseEntityData.NoGravity.addEntityDataIfNotDefaultValue(true, this.cachedValues);
        BaseEntityData.Silent.addEntityDataIfNotDefaultValue(true, this.cachedValues);
        BaseEntityData.SharedFlags.addEntityDataIfNotDefaultValue((byte) 0x20, this.cachedValues);
    }

    public float scale() {
        return this.scale;
    }

    public Object entityType() {
        return this.entityType;
    }

    public List<Object> cachedValues() {
        return this.cachedValues;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    @Override
    public void prepareBoundingBox(WorldPosition targetPos, Consumer<AABB> aabbConsumer, boolean ignoreBlocksBuilding) {
        if (this.blocksBuilding || ignoreBlocksBuilding) {
            Vec3d relativePosition = Furniture.getRelativePosition(targetPos, this.position);
            aabbConsumer.accept(AABB.makeBoundingBox(relativePosition, this.width, this.height));
        }
    }

    @Override
    public CustomFurnitureHitbox create(Furniture furniture) {
        return new CustomFurnitureHitbox(furniture, this);
    }

    public static class Factory implements FurnitureHitBoxConfigFactory<CustomFurnitureHitbox> {

        @Override
        public CustomFurnitureHitboxConfig create(ConfigSection section) {
            ConfigValue typeValue = section.getNonNullValue(ConfigConstants.ARGUMENT_IDENTIFIER, "entity_type", "entity-type");
            Object nmsEntityType = RegistryUtils.getRegistryValue(BuiltInRegistriesProxy.ENTITY_TYPE, KeyUtils.toIdentifier(typeValue.getAsIdentifier()));
            if (nmsEntityType == null) {
                throw new KnownResourceException("resource.furniture.hitbox.custom.invalid_entity_type", typeValue.path(), typeValue.getAsString());
            }
            Object dimensions = EntityTypeProxy.INSTANCE.getDimensions(nmsEntityType);
            float width = EntityDimensionsProxy.INSTANCE.getWidth(dimensions);
            float height = EntityDimensionsProxy.INSTANCE.getHeight(dimensions);
            boolean fixed = EntityDimensionsProxy.INSTANCE.isFixed(dimensions);
            return new CustomFurnitureHitboxConfig(
                    section.getNonNullValue(ConfigConstants.ARGUMENT_LIST, "seats").getAsSeats(),
                    section.getValueOrDefault(ConfigValue::getAsVector3f, ConfigConstants.ZERO_VECTOR3, "position"),
                    section.getBoolean(true, "can_use_item_on", "can-use-item-on"),
                    section.getBoolean(true, "blocks_building", "blocks-building"),
                    section.getBoolean(true, "can_be_hit_by_projectile", "can-be-hit-by-projectile"),
                    width,
                    height,
                    fixed,
                    section.getFloat(1f, "scale"),
                    nmsEntityType
            );
        }
    }
}
