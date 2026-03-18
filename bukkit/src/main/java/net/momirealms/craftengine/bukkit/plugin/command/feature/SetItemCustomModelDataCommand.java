package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.item.DataComponentTypes;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.parser.standard.FloatParser;

import java.util.List;
import java.util.Map;

public final class SetItemCustomModelDataCommand extends BukkitCommandFeature<CommandSender> {

    public SetItemCustomModelDataCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("value", FloatParser.floatParser(1.0F))
                .handler(context -> {
                    BukkitServerPlayer serverPlayer = BukkitAdaptor.adapt(context.sender());
                    Item itemInHand = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND).copyWithCount(1);
                    if (itemInHand.isEmpty()) {
                        return;
                    }

                    float dataValue = context.get("value");
                    itemInHand.setComponent(DataComponentTypes.CUSTOM_MODEL_DATA, Map.of("floats", List.of(dataValue)));
                    serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, itemInHand);
                    handleFeedback(context, MessageConstants.COMMAND_ITEM_CUSTOM_MODEL_DATA_SET, Component.text(dataValue));
                });
    }

    @Override
    public String getFeatureID() {
        return "set_item_custom_model_data";
    }
}
