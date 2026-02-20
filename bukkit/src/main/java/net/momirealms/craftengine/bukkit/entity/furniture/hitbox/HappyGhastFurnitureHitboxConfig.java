package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.bukkit.entity.data.HappyGhastData;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.hitbox.AbstractFurnitureHitBoxConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfigFactory;
import net.momirealms.craftengine.core.entity.seat.SeatConfig;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
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
    private final double scale;
    private final boolean hardCollision;
    private final List<Object> cachedValues = new ArrayList<>(3);

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
        return scale;
    }

    public boolean hardCollision() {
        return hardCollision;
    }

    public List<Object> cachedValues() {
        return cachedValues;
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
            Vector3f position = ResourceConfigUtils.getAsVector3f(section.getOrDefault("position", 0), "position");
            boolean canUseItemOn = ResourceConfigUtils.getAsBoolean(section.getOrDefault("can-use-item-on", true), "can-use-item-on");
            boolean blocksBuilding = ResourceConfigUtils.getAsBoolean(section.getOrDefault("blocks-building", true), "blocks-building");
            boolean canBeHitByProjectile = ResourceConfigUtils.getAsBoolean(section.getOrDefault("can-be-hit-by-projectile", true), "can-be-hit-by-projectile");
            double scale = ResourceConfigUtils.getAsDouble(section.getOrDefault("scale", 1), "scale");
            boolean hardCollision = ResourceConfigUtils.getAsBoolean(section.getOrDefault("hard-collision", true), "hard-collision");
            return new HappyGhastFurnitureHitboxConfig(
                    SeatConfig.fromObj(section.get("seats")),
                    position, canUseItemOn, blocksBuilding, canBeHitByProjectile,
                    scale, hardCollision
            );
        }
    }
}
