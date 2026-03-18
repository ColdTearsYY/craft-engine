package net.momirealms.craftengine.bukkit.plugin.injector;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.block.BlockSettings;
import net.momirealms.craftengine.core.block.DelegatingBlockState;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.proxy.minecraft.server.level.ServerPlayerProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.player.PlayerProxy;
import net.momirealms.craftengine.proxy.minecraft.world.item.ItemStackProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.BlockProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.BlockStateProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.StateDefinitionProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.properties.BlockStatePropertiesProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.storage.loot.LootParamsProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.storage.loot.parameters.LootContextParamsProxy;
import net.momirealms.craftengine.proxy.minecraft.world.phys.Vec3Proxy;
import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.constructor.SConstructor3;
import net.momirealms.sparrow.reflection.constructor.matcher.ConstructorMatcher;

import java.util.List;
import java.util.concurrent.Callable;

public final class BlockStateGenerator {
    private static SConstructor3 constructor$CraftEngineBlockState;
    public static Object instance$StateDefinition$Factory;

    public static void init() throws ReflectiveOperationException {
        ByteBuddy byteBuddy = new ByteBuddy(ClassFileVersion.JAVA_V17);
        String packageWithName = BlockStateGenerator.class.getName();
        String generatedStateClassName = packageWithName.substring(0, packageWithName.lastIndexOf('.')) + ".CraftEngineBlockState";
        DynamicType.Builder<?> stateBuilder = byteBuddy
                .subclass(BlockStateProxy.CLASS, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name(generatedStateClassName)
                .defineField("immutableBlockState", ImmutableBlockState.class, Visibility.PUBLIC)
                .defineField("blockOwner", BlockProxy.CLASS, Visibility.PUBLIC)
                .implement(DelegatingBlockState.class)
                .method(ElementMatchers.named("blockState"))
                .intercept(FieldAccessor.ofField("immutableBlockState"))
                .method(ElementMatchers.named("setBlockState"))
                .intercept(FieldAccessor.ofField("immutableBlockState"))
                .method(ElementMatchers.named("blockOwner"))
                .intercept(FieldAccessor.ofField("blockOwner"))
                .method(ElementMatchers.named("setBlockOwner"))
                .intercept(FieldAccessor.ofField("blockOwner"))
                .method(ElementMatchers.is(BlockReflections.method$BlockStateBase$getDrops))
                .intercept(MethodDelegation.to(GetDropsInterceptor.INSTANCE))
                .method(ElementMatchers.is(BlockReflections.method$StateHolder$hasProperty))
                .intercept(MethodDelegation.to(HasPropertyInterceptor.INSTANCE))
                .method(ElementMatchers.is(BlockReflections.method$StateHolder$getValue))
                .intercept(MethodDelegation.to(GetPropertyValueInterceptor.INSTANCE))
                .method(ElementMatchers.is(BlockReflections.method$StateHolder$setValue))
                .intercept(MethodDelegation.to(SetPropertyValueInterceptor.INSTANCE))
                .method(ElementMatchers.is(BlockReflections.method$BlockStateBase$is))
                .intercept(MethodDelegation.to(IsBlockInterceptor.INSTANCE))
                .method(ElementMatchers.is(BlockReflections.method$BlockStateBase$getBlock))
                .intercept(MethodDelegation.to(GetBlockInterceptor.INSTANCE))
                .method(ElementMatchers.is(BlockReflections.method$BlockStateBase$getBlockHolder))
                .intercept(MethodDelegation.to(GetBlockHolderInterceptor.INSTANCE));
        SparrowClass<?> clazz$CraftEngineBlock = SparrowClass.of(stateBuilder.make().load(BlockStateGenerator.class.getClassLoader()).getLoaded());

        constructor$CraftEngineBlockState = clazz$CraftEngineBlock.getSparrowConstructor(ConstructorMatcher.takeArguments(
                BlockProxy.CLASS,
                VersionHelper.isOrAbove1_20_5() ? Reference2ObjectArrayMap.class : ImmutableMap.class,
                MapCodec.class
        )).asm$3();

        String generatedFactoryClassName = packageWithName.substring(0, packageWithName.lastIndexOf('.')) + ".CraftEngineStateFactory";
        DynamicType.Builder<?> factoryBuilder = byteBuddy
                .subclass(Object.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name(generatedFactoryClassName)
                .implement(StateDefinitionProxy.FactoryProxy.CLASS)
                .method(ElementMatchers.named("create"))
                .intercept(MethodDelegation.to(CreateStateInterceptor.INSTANCE));

        Class<?> clazz$Factory = factoryBuilder.make().load(BlockStateGenerator.class.getClassLoader()).getLoaded();
        instance$StateDefinition$Factory = ReflectionUtils.getTheOnlyConstructor(clazz$Factory).newInstance();
    }

    public static class GetDropsInterceptor {
        public static final GetDropsInterceptor INSTANCE = new GetDropsInterceptor();

        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            ImmutableBlockState state = ((DelegatingBlockState) thisObj).blockState();
            if (state == null) return List.of();
            Object builder = args[0];
            Object vec3 = LootParamsProxy.BuilderProxy.INSTANCE.getOptionalParameter(builder, LootContextParamsProxy.ORIGIN);
            if (vec3 == null) return List.of();

            Object tool = LootParamsProxy.BuilderProxy.INSTANCE.getOptionalParameter(builder, LootContextParamsProxy.TOOL);
            Item item = BukkitItemManager.instance().wrap(tool == null ? null : ItemStackUtils.getBukkitStack(tool));
            Object optionalPlayer = LootParamsProxy.BuilderProxy.INSTANCE.getOptionalParameter(builder, LootContextParamsProxy.THIS_ENTITY);
            if (!PlayerProxy.CLASS.isInstance(optionalPlayer)) {
                optionalPlayer = null;
            }

            // do not drop if it's not the correct tool
            BlockSettings settings = state.settings();
            if (optionalPlayer != null && settings.requireCorrectTool()) {
                if (item.isEmpty()) return List.of();
                if (!settings.isCorrectTool(item.id()) &&
                        (!settings.respectToolComponent() || !ItemStackProxy.INSTANCE.isCorrectToolForDrops(tool, state.customBlockState().literalObject()))) {
                    return List.of();
                }
            }

            Object serverLevel = LootParamsProxy.BuilderProxy.INSTANCE.getLevel(builder);
            World world = BukkitAdaptor.adapt(LevelProxy.INSTANCE.getWorld(serverLevel));
            ContextHolder.Builder lootBuilder = new ContextHolder.Builder()
                    .withParameter(DirectContextParameters.POSITION, new WorldPosition(world, Vec3Proxy.INSTANCE.getX(vec3), Vec3Proxy.INSTANCE.getY(vec3), Vec3Proxy.INSTANCE.getZ(vec3)));
            if (!item.isEmpty()) {
                lootBuilder.withParameter(DirectContextParameters.ITEM_IN_HAND, item);
            }
            BukkitServerPlayer player = optionalPlayer != null ? BukkitAdaptor.adapt(ServerPlayerProxy.INSTANCE.getBukkitEntity(optionalPlayer)) : null;
            if (player != null) {
                lootBuilder.withParameter(DirectContextParameters.PLAYER, player);
            }
            Float radius = LootParamsProxy.BuilderProxy.INSTANCE.getOptionalParameter(builder, LootContextParamsProxy.EXPLOSION_RADIUS);
            if (radius != null) {
                lootBuilder.withParameter(DirectContextParameters.EXPLOSION_RADIUS, radius);
            }
            return state.getDrops(lootBuilder, world, player).stream().map(Item::getMinecraftItem).toList();
        }
    }

    public static class HasPropertyInterceptor {
        public static final HasPropertyInterceptor INSTANCE = new HasPropertyInterceptor();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public boolean intercept(@This Object thisObj, @AllArguments Object[] args) {
            Object property = args[0];
            if (property != BlockStatePropertiesProxy.WATERLOGGED) return false;
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            ImmutableBlockState state = customState.blockState();
            if (state == null) return false;
            Property<Boolean> waterloggedProperty = (Property<Boolean>) state.owner().value().getProperty("waterlogged");
            return waterloggedProperty != null;
        }
    }

    // TODO 将 property 获取代理到同名 property 上，并检查类型是否兼容
    public static class GetPropertyValueInterceptor {
        public static final GetPropertyValueInterceptor INSTANCE = new GetPropertyValueInterceptor();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            Object property = args[0];
            if (property != BlockStatePropertiesProxy.WATERLOGGED) return null;
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            ImmutableBlockState state = customState.blockState();
            if (state == null) return null;
            Property<Boolean> waterloggedProperty = (Property<Boolean>) state.owner().value().getProperty("waterlogged");
            if (waterloggedProperty == null) return null;
            return state.get(waterloggedProperty);
        }
    }

    public static class SetPropertyValueInterceptor {
        public static final SetPropertyValueInterceptor INSTANCE = new SetPropertyValueInterceptor();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            Object property = args[0];
            if (property != BlockStatePropertiesProxy.WATERLOGGED) return thisObj;
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            ImmutableBlockState state = customState.blockState();
            if (state == null) return thisObj;
            Property<Boolean> waterloggedProperty = (Property<Boolean>) state.owner().value().getProperty("waterlogged");
            if (waterloggedProperty == null) return thisObj;
            return state.with(waterloggedProperty, (boolean) args[1]).customBlockState().literalObject();
        }
    }

    public static class IsBlockInterceptor {
        public static final IsBlockInterceptor INSTANCE = new IsBlockInterceptor();

        @RuntimeType
        public boolean intercept(@This Object thisObj, @AllArguments Object[] args) {
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            Object thisBlock = customState.blockOwner();
            if (thisBlock == null) return false;
            if (BlockProxy.INSTANCE.getDefaultBlockState(args[0]) instanceof DelegatingBlockState holder) {
                Object holderBlock = holder.blockOwner();
                if (holderBlock == null) return false;
                return thisBlock == holderBlock;
            }
            return false;
        }
    }

    public static class GetBlockInterceptor {
        public static final GetBlockInterceptor INSTANCE = new GetBlockInterceptor();

        @RuntimeType
        public Object intercept(@This Object thisObj, @SuperCall Callable<Object> superMethod) throws Exception {
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            Object block = customState.blockOwner();
            return block != null ? block : superMethod.call();
        }
    }

    public static class GetBlockHolderInterceptor {
        public static final GetBlockHolderInterceptor INSTANCE = new GetBlockHolderInterceptor();

        @RuntimeType
        public Object intercept(@This Object thisObj, @SuperCall Callable<Object> superMethod) throws Exception {
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            Object block = customState.blockOwner();
            return block != null ? BlockProxy.INSTANCE.getBuiltInRegistryHolder(block) : superMethod.call();
        }
    }

    public static class CreateStateInterceptor {
        public static final CreateStateInterceptor INSTANCE = new CreateStateInterceptor();

        @RuntimeType
        public Object intercept(@AllArguments Object[] args) {
            return constructor$CraftEngineBlockState.newInstance(args[0], args[1], args[2]);
        }
    }
}
