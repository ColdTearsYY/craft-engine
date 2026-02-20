package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class FurnitureSounds {
    public static final SoundData EMPTY_SOUND = new SoundData(Key.of("minecraft:intentionally_empty"), SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_1);
    public static final FurnitureSounds EMPTY = new FurnitureSounds(EMPTY_SOUND, EMPTY_SOUND, EMPTY_SOUND);

    private final SoundData breakSound;
    private final SoundData placeSound;
    private final SoundData hitSound;

    public FurnitureSounds(SoundData breakSound, SoundData placeSound, SoundData hitSound) {
        this.breakSound = breakSound;
        this.placeSound = placeSound;
        this.hitSound = hitSound;
    }

    public static FurnitureSounds fromConfig(ConfigSection section) {
        if (section == null) return EMPTY;
        return new FurnitureSounds(
                section.getValueOrDefault(v -> v.getAsSoundData(SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_0_8), EMPTY_SOUND, "break"),
                section.getValueOrDefault(v -> v.getAsSoundData(SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_0_8), EMPTY_SOUND, "place"),
                section.getValueOrDefault(v -> v.getAsSoundData(SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_0_5), EMPTY_SOUND, "hit")
        );
    }

    public SoundData breakSound() {
        return this.breakSound;
    }

    public SoundData placeSound() {
        return this.placeSound;
    }

    public SoundData hitSound() {
        return this.hitSound;
    }
}
