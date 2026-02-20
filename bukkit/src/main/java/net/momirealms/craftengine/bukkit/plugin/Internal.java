package net.momirealms.craftengine.bukkit.plugin;

import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ConstructorInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
class Internal {

    @ReflectionProxy(clazz = BukkitWorldManager.class)
    interface BukkitWorldManagerProxy {
        BukkitWorldManagerProxy INSTANCE = ASMProxyFactory.create(BukkitWorldManagerProxy.class);

        @ConstructorInvoker
        BukkitWorldManager newInstance(BukkitCraftEngine plugin);
    }
}
