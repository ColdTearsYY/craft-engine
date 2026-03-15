package net.momirealms.craftengine.proxy.minecraft.world.item.context;

import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.world.item.context.UseOnContext")
public interface UseOnContextProxy {
    UseOnContextProxy INSTANCE = ASMProxyFactory.create(UseOnContextProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.world.item.context.UseOnContext");

    @MethodInvoker(name = "getHitResult")
    Object getHitResult(Object target);
}
