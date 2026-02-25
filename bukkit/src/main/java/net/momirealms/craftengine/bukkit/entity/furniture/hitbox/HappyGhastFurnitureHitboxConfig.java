package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.bukkit.entity.data.HappyGhastData;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.hitbox.AbstractFurnitureHitBoxConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfigFactory;
import net.momirealms.craftengine.core.entity.seat.SeatConfig;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.collision.AABB;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class HappyGhastFurnitureHitboxConfig extends AbstractFurnitureHitBoxConfig<HappyGhastFurnitureHitbox> {
    public static final Factory FACTORY = new Factory();
    public final double scale;
    public final boolean hardCollision;
    public final List<Object> cachedValues = new ArrayList<>(3);

    private HappyGhastFurnitureHitboxConfig(SeatConfig[] seats,
                                           Vector3f position,
                                           boolean canUseItemOn,
                                           boolean blocksBuilding,
                                           boolean canBeHitByProjectile,
                                           double scale,
                                           boolean hardCollision) {
        super(seats, position, canUseItemOn, blocksBuilding, canBeHitByProjectile);
        this.scale = scale;
        this.hardCollision = hardCollision;
        HappyGhastData.StaysStill.addEntityDataIfNotDefaultValue(hardCollision, this.cachedValues);
        HappyGhastData.MobFlags.addEntityDataIfNotDefaultValue((byte) 0x01, this.cachedValues); // NO AI
        HappyGhastData.SharedFlags.addEntityDataIfNotDefaultValue((byte) 0x20, this.cachedValues); // Invisible
    }

    public double scale() {
        return this.scale;
    }

    public boolean hardCollision() {
        return this.hardCollision;
    }

    public List<Object> cachedValues() {
        return this.cachedValues;
    }

    @Override
    public HappyGhastFurnitureHitbox create(Furniture furniture) {
        return new HappyGhastFurnitureHitbox(furniture, this);
    }

    @Override
    public void prepareBoundingBox(WorldPosition targetPos, Consumer<AABB> aabbConsumer, boolean ignoreBlocksBuilding) {
        if (this.blocksBuilding || ignoreBlocksBuilding) {
            Vec3d relativePosition = Furniture.getRelativePosition(targetPos, this.position);
            aabbConsumer.accept(AABB.makeBoundingBox(relativePosition, 4 * this.scale, 4 * this.scale));
        }
    }

    public static class Factory implements FurnitureHitBoxConfigFactory<HappyGhastFurnitureHitbox> {

        @Override
        public FurnitureHitBoxConfig<HappyGhastFurnitureHitbox> create(ConfigSection section) {
            return new HappyGhastFurnitureHitboxConfig(
                    section.getNonNullValue(ConfigConstants.ARGUMENT_LIST, "seats").getAsSeats(),
                    section.getValueOrDefault(ConfigValue::getAsVector3f, ConfigConstants.ZERO_VECTOR3, "position"),
                    section.getBoolean(true, "can_use_item_on", "can-use-item-on"),
                    section.getBoolean(true, "blocks_building", "blocks-building"),
                    section.getBoolean(true, "can_be_hit_by_projectile", "can-be-hit-by-projectile"),
                    section.getDouble(1d, "scale"),
                    section.getBoolean(true, "hard_collision", "hard-collision")
            );
        }
    }
}
