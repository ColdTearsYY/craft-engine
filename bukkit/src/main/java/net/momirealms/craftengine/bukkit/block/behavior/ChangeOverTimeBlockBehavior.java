package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.UpdateFlags;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.random.RandomUtils;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.event.CraftEventFactoryProxy;
import net.momirealms.sparrow.nbt.CompoundTag;

import java.util.List;
import java.util.concurrent.Callable;

public final class ChangeOverTimeBlockBehavior extends BukkitBlockBehavior {
    public static final BlockBehaviorFactory<ChangeOverTimeBlockBehavior> FACTORY = new Factory();
    public final float changeSpeed;
    public final String nextBlock;
    public final LazyReference<BlockStateWrapper> lazyState;
    public final List<String> excludedProperties;

    private ChangeOverTimeBlockBehavior(CustomBlock customBlock,
                                        float changeSpeed,
                                        String nextBlock,
                                        List<String> excludedProperties) {
        super(customBlock);
        this.changeSpeed = changeSpeed;
        this.nextBlock = nextBlock;
        this.excludedProperties = excludedProperties;
        this.lazyState = LazyReference.lazyReference(() -> CraftEngine.instance().blockManager().createBlockState(this.nextBlock));
    }

    public BlockStateWrapper nextState() {
        return this.lazyState.get();
    }

    public CompoundTag filter(CompoundTag properties) {
        for (String property : this.excludedProperties) {
            properties.remove(property);
        }
        return properties;
    }

    @Override
    public void randomTick(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        if (RandomUtils.generateRandomFloat(0F, 1F) >= this.changeSpeed) return;
        Object blockState = args[0];
        BlockStateUtils.getOptionalCustomBlockState(blockState).ifPresent(state -> {
            BlockStateWrapper nextState = this.nextState();
            if (nextState == null) return;
            nextState = nextState.withProperties(filter(state.propertiesNbt()));
            CraftEventFactoryProxy.INSTANCE.handleBlockFormEvent(args[1], args[2], nextState.literalObject(), UpdateFlags.UPDATE_ALL);
        });
    }

    private static class Factory implements BlockBehaviorFactory<ChangeOverTimeBlockBehavior> {

        @Override
        public ChangeOverTimeBlockBehavior create(CustomBlock block, ConfigSection section) {
            return new ChangeOverTimeBlockBehavior(
                    block,
                    section.getFloat(0.057F, "change_speed", "change-speed"),
                    section.getNonNullString("next_block", "next-block"),
                    section.getStringList("excluded_properties", "excluded-properties")
            );
        }
    }
}
