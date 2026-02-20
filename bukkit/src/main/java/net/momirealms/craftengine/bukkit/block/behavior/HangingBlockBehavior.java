package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.Tuple;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;

import java.util.List;
import java.util.Set;

public class HangingBlockBehavior extends BushBlockBehavior {
    public static final BlockBehaviorFactory<HangingBlockBehavior> FACTORY = new Factory();

    public HangingBlockBehavior(CustomBlock block, int delay, boolean blacklist, boolean stackable, List<Object> tagsCanSurviveOn, Set<Object> blocksCansSurviveOn, Set<String> customBlocksCansSurviveOn) {
        super(block, delay, blacklist, stackable, -1, tagsCanSurviveOn, blocksCansSurviveOn, customBlocksCansSurviveOn);
    }

    @Override
    protected boolean canSurvive(Object thisBlock, Object state, Object world, Object blockPos) throws ReflectiveOperationException {
        Object belowPos = LocationUtils.above(blockPos);
        Object belowState = BlockGetterProxy.INSTANCE.getBlockState(world, belowPos);
        return mayPlaceOn(belowState, world, belowPos);
    }

    private static class Factory implements BlockBehaviorFactory<HangingBlockBehavior> {

        @Override
        public HangingBlockBehavior create(CustomBlock block, ConfigSection section) {
            Tuple<List<Object>, Set<Object>, Set<String>> tuple = readTagsAndState(section, true);
            boolean stackable = ResourceConfigUtils.getAsBoolean(section.getOrDefault("stackable", false), "stackable");
            int delay = ResourceConfigUtils.getAsInt(section.getOrDefault("delay", 0), "delay");
            boolean blacklistMode = ResourceConfigUtils.getAsBoolean(section.getOrDefault("blacklist", false), "blacklist");
            return new HangingBlockBehavior(block, delay, blacklistMode, stackable, tuple.left(), tuple.mid(), tuple.right());
        }
    }
}
