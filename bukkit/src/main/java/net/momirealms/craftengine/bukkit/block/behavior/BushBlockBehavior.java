package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.BlockTags;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.Tuple;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.BlockBehaviourProxy;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BushBlockBehavior extends AbstractCanSurviveBlockBehavior {
    public static final BlockBehaviorFactory<BushBlockBehavior> FACTORY = new Factory();
    public final List<Object> tagsCanSurviveOn;
    public final List<Object> blocksCansSurviveOn;
    public final List<Object> blockStatesCansSurviveOn;
    public final boolean blacklistMode;
    public final boolean stackable;
    public final int maxHeight;

    protected BushBlockBehavior(CustomBlock block,
                                int delay,
                                boolean blacklist,
                                boolean stackable,
                                int maxHeight,
                                List<Object> tagsCanSurviveOn,
                                List<Object> blocksCansSurviveOn,
                                List<Object> blockStatesCansSurviveOn) {
        super(block, delay);
        this.blacklistMode = blacklist;
        this.stackable = stackable;
        this.maxHeight = maxHeight;
        this.tagsCanSurviveOn = List.copyOf(tagsCanSurviveOn);
        this.blocksCansSurviveOn = List.copyOf(blocksCansSurviveOn);
        this.blockStatesCansSurviveOn = List.copyOf(blockStatesCansSurviveOn);
    }

    private static class Factory implements BlockBehaviorFactory<BushBlockBehavior> {
        private static final String[] MAX_HEIGHT = new String[] {"max_height", "max-height"};

        @Override
        public BushBlockBehavior create(CustomBlock block, ConfigSection section) {
            Tuple<List<Object>, List<Object>, List<Object>> tuple = readTagsAndBlockAndState(
                    section.getValue(new String[]{"bottom_block_tags", "bottom-block-tags"}),
                    section.getValue(new String[]{"bottom_blocks", "bottom-blocks"}),
                    section.getValue(new String[]{"bottom_block_states", "bottom-block-states"})
            );
            return new BushBlockBehavior(
                    block,
                    section.getInt("delay", 0),
                    section.getBoolean("blacklist"),
                    section.getBoolean("stackable"),
                    section.getInt(MAX_HEIGHT),
                    tuple.left(),
                    tuple.mid(),
                    tuple.right()
            );
        }
    }

    public static Tuple<List<Object>, List<Object>, List<Object>> readTagsAndBlockAndState(@Nullable ConfigValue tagsValue, @Nullable ConfigValue blocksValue, @Nullable ConfigValue blockstatesValue) {
        List<Object> tags = tagsValue == null ? List.of() : tagsValue.getAsList(it -> BlockTags.getOrCreate(it.getAsIdentifier()));
        List<Object> blocks = blocksValue == null ? List.of() : blocksValue.getAsList(it -> BlockStateUtils.getBlockOwner(it.getAsBlockState().literalObject()));
        List<Object> blockstates = blockstatesValue == null ? List.of() : blockstatesValue.getAsList(it -> it.getAsBlockState().literalObject());
        return Tuple.of(tags, blocks, blockstates);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    protected boolean canSurvive(Object thisBlock, Object state, Object world, Object blockPos) throws Exception {
        Object belowPos = LocationUtils.below(blockPos);
        Object belowState = BlockGetterProxy.INSTANCE.getBlockState(world, belowPos);
        return mayPlaceOn(belowState, world, belowPos);
    }

    boolean mayPlaceOn(Object belowState, Object world, Object belowPos) {
        if (!this.blockStatesCansSurviveOn.isEmpty() && this.blockStatesCansSurviveOn.contains(belowState)) {
            return !this.blacklistMode;
        }
        for (Object tag : this.tagsCanSurviveOn) {
            if (BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$1(belowState, tag)) {
                return !this.blacklistMode;
            }
        }
        for (Object block : this.blocksCansSurviveOn) {
            if (BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$0(belowState, block)) {
                return !this.blacklistMode;
            }
        }
        ImmutableBlockState blockState = BlockStateUtils.getOptionalCustomBlockState(belowState).orElse(null);
        if (blockState != null && blockState.owner().value() == super.customBlock) {
            if (!this.stackable || this.maxHeight == 1) return false;
            if (this.maxHeight > 1) {
                return mayStackOn(world, belowPos);
            }
            return true;
        }
        return this.blacklistMode;
    }

    private boolean mayStackOn(Object world, Object belowPos) {
        int count = 1;
        Object cursorPos = LocationUtils.below(belowPos);

        while (count < this.maxHeight) {
            Object belowState = BlockGetterProxy.INSTANCE.getBlockState(world, cursorPos);
            Optional<ImmutableBlockState> belowCustomState = BlockStateUtils.getOptionalCustomBlockState(belowState);
            if (belowCustomState.isPresent() && belowCustomState.get().owner().value() == super.customBlock) {
                count++;
                cursorPos = LocationUtils.below(cursorPos);
            } else {
                break;
            }
        }
        return count < this.maxHeight;
    }
}
