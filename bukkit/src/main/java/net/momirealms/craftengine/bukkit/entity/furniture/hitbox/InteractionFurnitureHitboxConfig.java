package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.bukkit.entity.data.BaseEntityData;
import net.momirealms.craftengine.bukkit.entity.data.InteractionEntityData;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.hitbox.AbstractFurnitureHitBoxConfig;
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

public final class InteractionFurnitureHitboxConfig extends AbstractFurnitureHitBoxConfig<InteractionFurnitureHitbox> {
    public static final Factory FACTORY = new Factory();
    public static final InteractionFurnitureHitboxConfig DEFAULT = new InteractionFurnitureHitboxConfig();
    public final Vector3f size;
    public final boolean responsive;
    public final boolean invisible;
    public final List<Object> cachedValues = new ArrayList<>(4);

    private InteractionFurnitureHitboxConfig(SeatConfig[] seats,
                                            Vector3f position,
                                            boolean canUseItemOn,
                                            boolean blocksBuilding,
                                            boolean canBeHitByProjectile,
                                            boolean invisible,
                                            Vector3f size,
                                            boolean interactive) {
        super(seats, position, canUseItemOn, blocksBuilding, canBeHitByProjectile);
        this.size = size;
        this.responsive = interactive;
        this.invisible = invisible;
        InteractionEntityData.Height.addEntityDataIfNotDefaultValue(size.y, this.cachedValues);
        InteractionEntityData.Width.addEntityDataIfNotDefaultValue(size.x, this.cachedValues);
        InteractionEntityData.Responsive.addEntityDataIfNotDefaultValue(interactive, this.cachedValues);
        if (invisible) {
            BaseEntityData.SharedFlags.addEntityDataIfNotDefaultValue((byte) 0x20, this.cachedValues);
        }
    }

    private InteractionFurnitureHitboxConfig() {
        super(new SeatConfig[0], new Vector3f(), false, false, false);
        this.size = new Vector3f(1);
        this.responsive = true;
        this.invisible = false;
    }

    public Vector3f size() {
        return this.size;
    }

    public boolean responsive() {
        return this.responsive;
    }

    public boolean invisible() {
        return this.invisible;
    }

    public List<Object> cachedValues() {
        return this.cachedValues;
    }

    @Override
    public void prepareBoundingBox(WorldPosition targetPos, Consumer<AABB> aabbConsumer, boolean ignoreBlocksBuilding) {
        if (this.blocksBuilding || ignoreBlocksBuilding) {
            Vec3d relativePosition = Furniture.getRelativePosition(targetPos, this.position);
            aabbConsumer.accept(AABB.makeBoundingBox(relativePosition, this.size.x, this.size.y));
        }
    }

    @Override
    public InteractionFurnitureHitbox create(Furniture furniture) {
        return new InteractionFurnitureHitbox(furniture, this);
    }

    public static class Factory implements FurnitureHitBoxConfigFactory<InteractionFurnitureHitbox> {

        @Override
        public InteractionFurnitureHitboxConfig create(ConfigSection section) {
            float width;
            float height;
            ConfigValue optionalScale = section.getValue("scale");
            if (optionalScale != null) {
                ConfigValue[] split = optionalScale.getSplitValuesRestrict(",", 2);
                width = split[0].getAsFloat();
                height = split[1].getAsFloat();
            } else {
                width = section.getFloat(1f, "width");
                height = section.getFloat(1f, "height");
            }
            return new InteractionFurnitureHitboxConfig(
                    section.getNonNullValue(ConfigConstants.ARGUMENT_LIST, "seats").getAsSeats(),
                    section.getValueOrDefault(ConfigValue::getAsVector3f, ConfigConstants.ZERO_VECTOR3, "position"),
                    section.getBoolean(true, "can_use_item_on", "can-use-item-on"),
                    section.getBoolean(true, "blocks_building", "blocks-building"),
                    section.getBoolean(true, "can_be_hit_by_projectile", "can-be-hit-by-projectile"),
                    section.getBoolean("invisible"),
                    new Vector3f(width, height, width),
                    section.getBoolean(true, "interactive")
            );
        }
    }
}
