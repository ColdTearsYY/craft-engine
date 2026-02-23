package net.momirealms.craftengine.core.world.particle;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.Key;

import java.util.Optional;

@SuppressWarnings("DuplicatedCode")
public final class ParticleConfig {
    public final ParticleType particleType;
    public final NumberProvider x;
    public final NumberProvider y;
    public final NumberProvider z;
    public final NumberProvider count;
    public final NumberProvider xOffset;
    public final NumberProvider yOffset;
    public final NumberProvider zOffset;
    public final NumberProvider speed;
    public final ParticleData particleData;

    public ParticleConfig(ParticleType particleType,
                          NumberProvider x,
                          NumberProvider y,
                          NumberProvider z,
                          NumberProvider count,
                          NumberProvider xOffset,
                          NumberProvider yOffset,
                          NumberProvider zOffset,
                          NumberProvider speed,
                          ParticleData particleData) {
        this.particleType = particleType;
        this.x = x;
        this.y = y;
        this.z = z;
        this.count = count;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.speed = speed;
        this.particleData = particleData;
    }

    public static ParticleConfig fromConfig$function(ConfigSection section) {
        Key particleType = section.getNonNullIdentifier("particle");
        NumberProvider x = NumberProviders.fromObject(section.getOrDefault("<arg:position.x>", "x"));
        NumberProvider y = NumberProviders.fromObject(section.getOrDefault("<arg:position.y>", "y"));
        NumberProvider z = NumberProviders.fromObject(section.getOrDefault("<arg:position.z>", "z"));
        NumberProvider count = NumberProviders.fromObject(section.getOrDefault(1, "count"));
        NumberProvider xOffset = NumberProviders.fromObject(section.getOrDefault(0, "offset-x"));
        NumberProvider yOffset = NumberProviders.fromObject(section.getOrDefault(0, "offset-y"));
        NumberProvider zOffset = NumberProviders.fromObject(section.getOrDefault(0, "offset-z"));
        NumberProvider speed = NumberProviders.fromObject(section.getOrDefault(0, "speed"));
        return new ParticleConfig(CraftEngine.instance().platform().getParticleType(particleType), x, y, z, count, xOffset, yOffset, zOffset, speed, Optional.ofNullable(ParticleDataTypes.TYPES.get(particleType)).map(it -> it.apply(section)).orElse(null));
    }

    public static ParticleConfig fromConfig$blockEntity(ConfigSection section) {
        Key particleType = section.getNonNullIdentifier("particle");
        NumberProvider x = NumberProviders.fromObject(section.getOrDefault(0, "x"));
        NumberProvider y = NumberProviders.fromObject(section.getOrDefault(0, "y"));
        NumberProvider z = NumberProviders.fromObject(section.getOrDefault(0, "z"));
        NumberProvider count = NumberProviders.fromObject(section.getOrDefault(1, "count"));
        NumberProvider xOffset = NumberProviders.fromObject(section.getOrDefault(0, "offset-x"));
        NumberProvider yOffset = NumberProviders.fromObject(section.getOrDefault(0, "offset-y"));
        NumberProvider zOffset = NumberProviders.fromObject(section.getOrDefault(0, "offset-z"));
        NumberProvider speed = NumberProviders.fromObject(section.getOrDefault(0, "speed"));
        return new ParticleConfig(CraftEngine.instance().platform().getParticleType(particleType), x, y, z, count, xOffset, yOffset, zOffset, speed, Optional.ofNullable(ParticleDataTypes.TYPES.get(particleType)).map(it -> it.apply(section)).orElse(null));
    }

    public ParticleType particleType() {
        return particleType;
    }

    public NumberProvider x() {
        return x;
    }

    public NumberProvider y() {
        return y;
    }

    public NumberProvider z() {
        return z;
    }

    public NumberProvider count() {
        return count;
    }

    public NumberProvider xOffset() {
        return xOffset;
    }

    public NumberProvider yOffset() {
        return yOffset;
    }

    public NumberProvider zOffset() {
        return zOffset;
    }

    public NumberProvider speed() {
        return speed;
    }

    public ParticleData particleData() {
        return particleData;
    }
}
