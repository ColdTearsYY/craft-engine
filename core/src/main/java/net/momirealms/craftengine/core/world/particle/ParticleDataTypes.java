package net.momirealms.craftengine.core.world.particle;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.context.number.ConstantNumberProvider;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.LazyReference;

import java.util.HashMap;
import java.util.Map;

public final class ParticleDataTypes {
    public static final Map<Key, java.util.function.Function<ConfigSection, ParticleData>> TYPES = new HashMap<>();

    static {
        registerParticleData(section -> {
                    final String blockState = section.getNonNullString("blockstate", "block_state", "block-state");
                    return new BlockStateData(LazyReference.lazyReference(() -> CraftEngine.instance().blockManager().createBlockState(blockState)));
                },
                ParticleTypes.BLOCK, ParticleTypes.FALLING_DUST, ParticleTypes.DUST_PILLAR, ParticleTypes.BLOCK_CRUMBLE, ParticleTypes.BLOCK_MARKER);
        registerParticleData(section -> new ColorData(
                        section.getNonNullValue(ConfigConstants.ARGUMENT_COLOR, "color").getAsColor()
                ),
                ParticleTypes.ENTITY_EFFECT, ParticleTypes.TINTED_LEAVES);
        registerParticleData(section -> new JavaTypeData(
                        section.getFloat("charge")
                ),
                ParticleTypes.SCULK_CHARGE);
        registerParticleData(section -> new JavaTypeData(
                        section.getInt("shriek")
                ),
                ParticleTypes.SHRIEK);
        registerParticleData(section -> new DustData(
                        section.getNonNullValue(ConfigConstants.ARGUMENT_COLOR, "color").getAsColor(),
                        section.getFloat(1f, "scale")
                ),
                ParticleTypes.DUST);
        registerParticleData(section -> new DustTransitionData(
                        section.getNonNullValue(ConfigConstants.ARGUMENT_COLOR, "from").getAsColor(),
                        section.getNonNullValue(ConfigConstants.ARGUMENT_COLOR, "to").getAsColor(),
                        section.getFloat(1f, "scale")
                ),
                ParticleTypes.DUST_COLOR_TRANSITION);
        registerParticleData(section -> {
                    final Key itemId = section.getNonNullIdentifier("item");
                    return new ItemStackData(LazyReference.lazyReference(() -> CraftEngine.instance().itemManager().createWrappedItem(itemId, null)));
                },
                ParticleTypes.ITEM);
        registerParticleData(section -> new VibrationData(
                        section.getValueOrDefault(ConfigValue::getAsNumber, ConfigConstants.CONSTANT_ZERO, "target_x", "target-x"),
                        section.getValueOrDefault(ConfigValue::getAsNumber, ConfigConstants.CONSTANT_ZERO, "target_y", "target-y"),
                        section.getValueOrDefault(ConfigValue::getAsNumber, ConfigConstants.CONSTANT_ZERO, "target_z", "target-z"),
                        section.getValueOrDefault(ConfigValue::getAsNumber, ConstantNumberProvider.constant(10), "arrival_time", "arrival-time")
                ),
                ParticleTypes.VIBRATION);
        registerParticleData(section -> new TrailData(
                        section.getValueOrDefault(ConfigValue::getAsNumber, ConfigConstants.CONSTANT_ZERO, "target_x", "target-x"),
                        section.getValueOrDefault(ConfigValue::getAsNumber, ConfigConstants.CONSTANT_ZERO, "target_y", "target-y"),
                        section.getValueOrDefault(ConfigValue::getAsNumber, ConfigConstants.CONSTANT_ZERO, "target_z", "target-z"),
                        section.getNonNullValue(ConfigConstants.ARGUMENT_COLOR, "color").getAsColor(),
                        section.getValueOrDefault(ConfigValue::getAsNumber, ConstantNumberProvider.constant(10), "duration")
                ),
                ParticleTypes.TRAIL);
    }

    public static void registerParticleData(java.util.function.Function<ConfigSection, ParticleData> function, Key... types) {
        for (Key type : types) {
            TYPES.put(type, function);
        }
    }
}
