package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.UpdateFlags;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.ExistingBlock;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.sparrow.nbt.CompoundTag;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class UpdateBlockPropertyFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final CompoundTag properties;
    private final NumberProvider x;
    private final NumberProvider y;
    private final NumberProvider z;
    private final NumberProvider updateFlags;

    private UpdateBlockPropertyFunction(List<Condition<CTX>> predicates, NumberProvider x, NumberProvider y, NumberProvider z, NumberProvider updateFlags, CompoundTag properties) {
        super(predicates);
        this.properties = properties;
        this.x = x;
        this.y = y;
        this.z = z;
        this.updateFlags = updateFlags;
    }

    @Override
    public void runInternal(CTX ctx) {
        Optional<WorldPosition> optionalWorldPosition = ctx.getOptionalParameter(DirectContextParameters.POSITION);
        if (optionalWorldPosition.isPresent()) {
            World world = optionalWorldPosition.get().world();
            int x = MiscUtils.floor(this.x.getDouble(ctx));
            int y = MiscUtils.floor(this.y.getDouble(ctx));
            int z = MiscUtils.floor(this.z.getDouble(ctx));
            ExistingBlock blockAt = world.getBlock(x, y, z);
            BlockStateWrapper wrapper = blockAt.blockState().withProperties(this.properties);
            world.setBlockState(x, y, z, wrapper, this.updateFlags.getInt(ctx));
        }
    }

    public static <CTX extends Context> FunctionFactory<CTX, UpdateBlockPropertyFunction<CTX>> factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX, UpdateBlockPropertyFunction<CTX>> {

        public Factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public UpdateBlockPropertyFunction<CTX> create(ConfigSection section) {
            ConfigSection propertiesSection = section.getNonNullSection("properties");
            CompoundTag properties = new CompoundTag();
            for (Map.Entry<String, Object> entry : propertiesSection.values().entrySet()) {
                properties.putString(entry.getKey(), String.valueOf(entry.getValue()));
            }
            return new UpdateBlockPropertyFunction<>(
                    getPredicates(section),
                    NumberProviders.fromObject(section.getOrDefault("<arg:position.x>", "x")),
                    NumberProviders.fromObject(section.getOrDefault("<arg:position.y>", "y")),
                    NumberProviders.fromObject(section.getOrDefault("<arg:position.z>", "z")),
                    Optional.ofNullable(section.get("update_flags", "update-flags")).map(NumberProviders::fromObject).orElse(NumberProviders.direct(UpdateFlags.UPDATE_ALL)),
                    properties
            );
        }
    }
}