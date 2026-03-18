package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.item.DataComponentTypes;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.parser.standard.StringParser;

public final class SetItemItemModelCommand extends BukkitCommandFeature<CommandSender> {

    public SetItemItemModelCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("value", StringParser.greedyStringParser())
                .handler(context -> {
                    BukkitServerPlayer serverPlayer = BukkitAdaptor.adapt(context.sender());
                    Item itemInHand = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND).copyWithCount(1);
                    if (itemInHand.isEmpty()) {
                        return;
                    }

                    String dataValue = context.get("value");
                    if (dataValue.indexOf(' ') != -1) {
                        handleFeedback(context, MessageConstants.COMMAND_ITEM_ITEM_MODEL_SET_INVALID, Component.text(dataValue));
                        return;
                    }
                    itemInHand.setComponent(DataComponentTypes.ITEM_MODEL, Key.of(dataValue).asString());
                    serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, itemInHand);
                    handleFeedback(context, MessageConstants.COMMAND_ITEM_ITEM_MODEL_SET_SUCCESS, Component.text(dataValue));
                });
    }

    @Override
    public String getFeatureID() {
        return "set_item_item_model";
    }
}
