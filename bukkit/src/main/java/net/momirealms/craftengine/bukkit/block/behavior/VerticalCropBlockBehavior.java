package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateFlags;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.IntegerProperty;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.util.random.RandomUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.event.CraftEventFactoryProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelWriterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.BlocksProxy;

import java.util.Optional;
import java.util.concurrent.Callable;

public final class VerticalCropBlockBehavior extends BukkitBlockBehavior {
    public static final BlockBehaviorFactory<VerticalCropBlockBehavior> FACTORY = new Factory();
    public final int maxHeight;
    public final IntegerProperty ageProperty;
    public final float growSpeed;
    public final boolean direction;

    private VerticalCropBlockBehavior(CustomBlock customBlock,
                                      IntegerProperty ageProperty,
                                      int maxHeight,
                                      float growSpeed,
                                      boolean direction) {
        super(customBlock);
        this.maxHeight = maxHeight;
        this.ageProperty = ageProperty;
        this.growSpeed = growSpeed;
        this.direction = direction;
    }

    @Override
    public void randomTick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object blockState = args[0];
        Object level = args[1];
        Object blockPos = args[2];
        Optional<ImmutableBlockState> optionalCurrentState = BlockStateUtils.getOptionalCustomBlockState(blockState);
        if (optionalCurrentState.isEmpty()) {
            return;
        }
        ImmutableBlockState currentState = optionalCurrentState.get();
        // above block is empty
        if (BlockGetterProxy.INSTANCE.getBlockState(level, (this.direction ? LocationUtils.above(blockPos) : LocationUtils.below(blockPos))) == BlocksProxy.AIR$defaultState) {
            int currentHeight = 1;
            BlockPos currentPos = LocationUtils.fromBlockPos(blockPos);
            for (; ; ) {
                Object nextPos = LocationUtils.toBlockPos(currentPos.x(), this.direction ? currentPos.y() - currentHeight : currentPos.y() + currentHeight, currentPos.z());
                Object nextState = BlockGetterProxy.INSTANCE.getBlockState(level, nextPos);
                Optional<ImmutableBlockState> optionalBelowCustomState = BlockStateUtils.getOptionalCustomBlockState(nextState);
                if (optionalBelowCustomState.isPresent() && optionalBelowCustomState.get().owner().value() == super.customBlock) {
                    currentHeight++;
                } else {
                    break;
                }
            }
            if (currentHeight < this.maxHeight) {
                int age = currentState.get(ageProperty);
                if (age >= this.ageProperty.max || RandomUtils.generateRandomFloat(0, 1) < this.growSpeed) {
                    Object nextPos = this.direction ? LocationUtils.above(blockPos) : LocationUtils.below(blockPos);
                    boolean success;
                    if (VersionHelper.isOrAbove1_21_5()) {
                        success = CraftEventFactoryProxy.INSTANCE.handleBlockGrowEvent(level, nextPos, super.customBlock.defaultState().customBlockState().literalObject(), UpdateFlags.UPDATE_ALL);
                    } else {
                        success = CraftEventFactoryProxy.INSTANCE.handleBlockGrowEvent(level, nextPos, super.customBlock.defaultState().customBlockState().literalObject());
                    }
                    if (success) {
                        LevelWriterProxy.INSTANCE.setBlock(level, blockPos, currentState.with(this.ageProperty, this.ageProperty.min).customBlockState().literalObject(), UpdateFlags.UPDATE_NONE);
                    }
                } else if (RandomUtils.generateRandomFloat(0, 1) < this.growSpeed) {
                    LevelWriterProxy.INSTANCE.setBlock(level, blockPos, currentState.with(this.ageProperty, age + 1).customBlockState().literalObject(), UpdateFlags.UPDATE_NONE);
                }
            }
        }
    }

    private static class Factory implements BlockBehaviorFactory<VerticalCropBlockBehavior> {

        @Override
        public VerticalCropBlockBehavior create(CustomBlock block, ConfigSection section) {
            return new VerticalCropBlockBehavior(
                    block,
                    (IntegerProperty) BlockBehaviorFactory.getProperty(section.path(), block, "age", Integer.class),
                    section.getInt(3, "max_height", "max-height"),
                    section.getFloat(1f, "grow_speed", "grow-speed"),
                    section.getDefaultedString("up","direction").equalsIgnoreCase("up")
            );
        }
    }
}
