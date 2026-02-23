package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.entity.BukkitBlockEntityTypes;
import net.momirealms.craftengine.bukkit.block.entity.WallTorchParticleBlockEntity;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.entity.tick.BlockEntityTicker;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.particle.ParticleConfig;

public final class WallTorchParticleBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final BlockBehaviorFactory<WallTorchParticleBlockBehavior> FACTORY = new Factory();
    public final ParticleConfig[] particles;
    public final int tickInterval;
    public final Property<HorizontalDirection> facingProperty;

    private WallTorchParticleBlockBehavior(CustomBlock customBlock,
                                           ParticleConfig[] particles,
                                           int tickInterval,
                                           Property<HorizontalDirection> facingProperty) {
        super(customBlock);
        this.particles = particles;
        this.tickInterval = tickInterval;
        this.facingProperty = facingProperty;
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(ImmutableBlockState state) {
        return EntityBlockBehavior.blockEntityTypeHelper(BukkitBlockEntityTypes.WALL_TORCH_PARTICLE);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, ImmutableBlockState state) {
        return new WallTorchParticleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> createAsyncBlockEntityTicker(CEWorld level, ImmutableBlockState state, BlockEntityType<T> blockEntityType) {
        if (this.particles.length == 0) return null;
        return EntityBlockBehavior.createTickerHelper(WallTorchParticleBlockEntity::tick);
    }

    private static class Factory implements BlockBehaviorFactory<WallTorchParticleBlockBehavior> {

        @Override
        public WallTorchParticleBlockBehavior create(CustomBlock block, ConfigSection section) {
            return new WallTorchParticleBlockBehavior(
                    block,
                    section.parseSectionList(ParticleConfig::fromConfig$blockEntity, "particles", "particle").toArray(new ParticleConfig[0]),
                    section.getInt(10, "tick_interval", "tick-interval"),
                    BlockBehaviorFactory.getProperty(section.path(), block, "facing", HorizontalDirection.class)
            );
        }
    }
}
