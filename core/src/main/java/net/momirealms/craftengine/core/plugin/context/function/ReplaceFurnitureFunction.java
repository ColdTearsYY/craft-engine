package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.WorldPosition;

import java.util.List;
import java.util.Optional;

public final class ReplaceFurnitureFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final Key newFurnitureId;
    private final NumberProvider x;
    private final NumberProvider y;
    private final NumberProvider z;
    private final NumberProvider pitch;
    private final NumberProvider yaw;
    private final String variant;
    private final boolean dropLoot;
    private final boolean playSound;

    private ReplaceFurnitureFunction(
            List<Condition<CTX>> predicates, NumberProvider x, NumberProvider y, NumberProvider z, NumberProvider pitch, NumberProvider yaw, String variant, boolean dropLoot, boolean playSound, Key newFurnitureId
    ) {
        super(predicates);
        this.newFurnitureId = newFurnitureId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.variant = variant;
        this.dropLoot = dropLoot;
        this.playSound = playSound;
    }

    @Override
    public void runInternal(CTX ctx) {
        Optional<WorldPosition> optionalWorldPosition = ctx.getOptionalParameter(DirectContextParameters.POSITION);
        Optional<Furniture> optionalOldFurniture = ctx.getOptionalParameter(DirectContextParameters.FURNITURE);

        if (optionalWorldPosition.isPresent() && optionalOldFurniture.isPresent()) {
            Furniture oldFurniture = optionalOldFurniture.get();
            WorldPosition worldPosition = optionalWorldPosition.get();

            // Get the new position or use the current furniture position
            double xPos = this.x.getDouble(ctx);
            double yPos = this.y.getDouble(ctx);
            double zPos = this.z.getDouble(ctx);
            float pitchValue = this.pitch.getFloat(ctx);
            float yawValue = this.yaw.getFloat(ctx);
            WorldPosition newPosition = new WorldPosition(worldPosition.world(), xPos, yPos, zPos, pitchValue, yawValue);

            // Remove the old furniture
            RemoveFurnitureFunction.removeFurniture(ctx, oldFurniture, dropLoot, playSound);

            // Place the new furniture
            SpawnFurnitureFunction.spawnFurniture(this.newFurnitureId, newPosition, this.variant, this.playSound);
        }
    }

    public static <CTX extends Context> FunctionFactory<CTX, ReplaceFurnitureFunction<CTX>> factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX, ReplaceFurnitureFunction<CTX>> {

        public Factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public ReplaceFurnitureFunction<CTX> create(ConfigSection section) {
            return new ReplaceFurnitureFunction<>(
                    getPredicates(section),
                    NumberProviders.fromObject(section.getOrDefault("<arg:furniture.x>", "x")),
                    NumberProviders.fromObject(section.getOrDefault("<arg:furniture.y>", "y")),
                    NumberProviders.fromObject(section.getOrDefault("<arg:furniture.z>", "z")),
                    NumberProviders.fromObject(section.getOrDefault("<arg:furniture.pitch>", "pitch")),
                    NumberProviders.fromObject(section.getOrDefault("<arg:furniture.yaw>", "yaw")),
                    section.getNonNullString("variant", "anchor_type", "anchor-type"),
                    section.getBoolean(true, "drop_loot", "drop-loot"),
                    section.getBoolean(true, "play_sound", "play-sound"),
                    section.getNonNullIdentifier("furniture_id", "furniture-id", "furniture", "id")
            );
        }
    }
}