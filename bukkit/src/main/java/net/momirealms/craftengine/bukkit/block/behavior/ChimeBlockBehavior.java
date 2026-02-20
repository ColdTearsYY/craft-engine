package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.sounds.SoundEventProxy;
import net.momirealms.craftengine.proxy.minecraft.sounds.SoundSourceProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelAccessorProxy;
import net.momirealms.craftengine.proxy.minecraft.world.phys.BlockHitResultProxy;

import java.util.Optional;
import java.util.concurrent.Callable;

public final class ChimeBlockBehavior extends BukkitBlockBehavior {
    public static final BlockBehaviorFactory<ChimeBlockBehavior> FACTORY = new Factory();
    public final SoundData hitSound;

    private ChimeBlockBehavior(CustomBlock customBlock, SoundData hitSound) {
        super(customBlock);
        this.hitSound = hitSound;
    }

    @Override
    public void onProjectileHit(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        if (this.hitSound == null) return;
        Object blockPos = BlockHitResultProxy.INSTANCE.getBlockPos(args[2]);
        Object sound = SoundEventProxy.INSTANCE.create(KeyUtils.toIdentifier(hitSound.id()), Optional.empty());
        if (VersionHelper.isOrAbove1_21_5()) {
            LevelAccessorProxy.INSTANCE.playSound$0(args[0], null, blockPos, sound, SoundSourceProxy.BLOCKS, hitSound.volume().get(), hitSound.pitch().get());
        } else {
            LevelAccessorProxy.INSTANCE.playSound$1(args[0], null, blockPos, sound, SoundSourceProxy.BLOCKS, hitSound.volume().get(), hitSound.pitch().get());
        }
    }

    private static class Factory implements BlockBehaviorFactory<ChimeBlockBehavior> {

        @Override
        public ChimeBlockBehavior create(CustomBlock block, ConfigSection section) {
            ConfigSection soundsSection = section.getSection("sounds");
            SoundData hitSound = null;
            if (soundsSection != null) {
                hitSound = soundsSection.getValue(v -> v.getAsSoundData(SoundData.SoundValue.FIXED_1, SoundData.SoundValue.RANGED_0_9_1), "chime", "projectile_hit", "projectile-hit");
            }
            return new ChimeBlockBehavior(block, hitSound);
        }
    }
}
