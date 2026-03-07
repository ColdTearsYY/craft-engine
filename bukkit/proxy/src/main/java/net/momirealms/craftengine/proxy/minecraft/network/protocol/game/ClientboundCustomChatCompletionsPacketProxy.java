package net.momirealms.craftengine.proxy.minecraft.network.protocol.game;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ConstructorInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

import java.util.List;

@ReflectionProxy(name = "net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket")
public interface ClientboundCustomChatCompletionsPacketProxy {
    ClientboundCustomChatCompletionsPacketProxy INSTANCE = ASMProxyFactory.create(ClientboundCustomChatCompletionsPacketProxy.class);

    @ConstructorInvoker
    Object newInstance(@Type(clazz = ActionProxy.class) Object action, List<String> entries);

    @ReflectionProxy(name = "net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket$Action")
    interface ActionProxy {
        ActionProxy INSTANCE = ASMProxyFactory.create(ActionProxy.class);
        Enum<?>[] VALUES = INSTANCE.values();
        Enum<?> ADD = VALUES[0];
        Enum<?> REMOVE = VALUES[1];
        Enum<?> SET = VALUES[2];

        @MethodInvoker(name = "values", isStatic = true)
        Enum<?>[] values();
    }
}
