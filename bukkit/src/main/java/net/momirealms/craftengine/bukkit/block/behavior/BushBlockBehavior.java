package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.BlockTags;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.Tuple;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.BlockBehaviourProxy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BushBlockBehavior extends AbstractCanSurviveBlockBehavior {
    public static final BlockBehaviorFactory<BushBlockBehavior> FACTORY = new Factory();
    public final List<Object> tagsCanSurviveOn;
    public final Set<Object> blockStatesCanSurviveOn;
    public final Set<String> customBlocksCansSurviveOn;
    public final boolean blacklistMode;
    public final boolean stackable;
    public final int maxHeight;

    protected BushBlockBehavior(CustomBlock block,
                                int delay,
                                boolean blacklist,
                                boolean stackable,
                                int maxHeight,
                                List<Object> tagsCanSurviveOn,
                                Set<Object> blockStatesCanSurviveOn,
                                Set<String> customBlocksCansSurviveOn) {
        super(block, delay);
        this.blacklistMode = blacklist;
        this.stackable = stackable;
        this.maxHeight = maxHeight;
        this.tagsCanSurviveOn = List.copyOf(tagsCanSurviveOn);
        this.blockStatesCanSurviveOn = Set.copyOf(blockStatesCanSurviveOn);
        this.customBlocksCansSurviveOn = Set.copyOf(customBlocksCansSurviveOn);
    }

    private static class Factory implements BlockBehaviorFactory<BushBlockBehavior> {

        @Override
        public BushBlockBehavior create(CustomBlock block, ConfigSection section) {
            Tuple<List<Object>, Set<Object>, Set<String>> tuple = readTagsAndState(section, false);
            return new BushBlockBehavior(
                    block,
                    section.getInt(0, "delay"),
                    section.getBoolean("blacklist"),
                    section.getBoolean("stackable"),
                    section.getInt(0, "max_height", "max-height"),
                    tuple.left(),
                    tuple.mid(),
                    tuple.right()
            );
        }
    }

    // todo 重构一下
    public static Tuple<List<Object>, Set<Object>, Set<String>> readTagsAndState(ConfigSection section, boolean aboveOrBelow) {
        List<Object> mcTags = section.parseList(v -> BlockTags.getOrCreate(v.getAsIdentifier()),
                (aboveOrBelow ? "above" : "bottom") + "_block_tags", (aboveOrBelow ? "above" : "bottom") + "-block-tags");
        Set<Object> mcBlocks = new HashSet<>();
        Set<String> customBlocks = new HashSet<>();
        for (String blockState : section.getStringList((aboveOrBelow ? "above" : "bottom") + "_block_tags", (aboveOrBelow ? "above" : "bottom") + "-block-tags")) {
            int index = blockState.indexOf('[');
            Key blockType = index != -1 ? Key.of(blockState.substring(0, index)) : Key.of(blockState);
            Material material = Registry.MATERIAL.get(new NamespacedKey(blockType.namespace(), blockType.value()));
            if (material != null) {
                if (index == -1) {
                    // vanilla
                    mcBlocks.addAll(BlockStateUtils.getPossibleBlockStates(blockType));
                } else {
                    mcBlocks.add(BlockStateUtils.blockDataToBlockState(Bukkit.createBlockData(blockState)));
                }
            } else {
                // custom maybe
                customBlocks.add(blockState);
            }
        }
        return new Tuple<>(mcTags, mcBlocks, customBlocks);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    protected boolean canSurvive(Object thisBlock, Object state, Object world, Object blockPos) throws Exception {
        Object belowPos = LocationUtils.below(blockPos);
        Object belowState = BlockGetterProxy.INSTANCE.getBlockState(world, belowPos);
        return mayPlaceOn(belowState, world, belowPos);
    }

    boolean mayPlaceOn(Object belowState, Object world, Object belowPos) {
        for (Object tag : this.tagsCanSurviveOn) {
            if (BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$1(belowState, tag)) {
                return !this.blacklistMode;
            }
        }
        Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(belowState);
        if (optionalCustomState.isEmpty()) {
            if (!this.blockStatesCanSurviveOn.isEmpty() && this.blockStatesCanSurviveOn.contains(belowState)) {
                return !this.blacklistMode;
            }
        } else {
            ImmutableBlockState belowCustomState = optionalCustomState.get();
            if (belowCustomState.owner().value() == super.customBlock) {
                if (!this.stackable || this.maxHeight == 1) return false;
                if (this.maxHeight > 1) {
                    return mayStackOn(world, belowPos);
                }
                return true;
            }
            if (this.customBlocksCansSurviveOn.contains(belowCustomState.owner().value().id().toString())) {
                return !this.blacklistMode;
            }
            if (this.customBlocksCansSurviveOn.contains(belowCustomState.toString())) {
                return !this.blacklistMode;
            }
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
