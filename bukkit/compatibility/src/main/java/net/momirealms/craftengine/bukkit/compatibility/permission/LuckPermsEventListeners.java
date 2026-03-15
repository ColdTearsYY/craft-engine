package net.momirealms.craftengine.bukkit.compatibility.permission;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class LuckPermsEventListeners {
    private final JavaPlugin plugin;
    private final LuckPerms luckPerms;
    private final Consumer<Player> playerCallback;

    public LuckPermsEventListeners(JavaPlugin plugin, Consumer<Player> playerCallback) {
        this.plugin = plugin;
        this.playerCallback = playerCallback;
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
            this.registerEventListeners();
        } else {
            throw new IllegalStateException("Unable to hook LuckPerms");
        }
    }

    @SuppressWarnings("resource")
    private void registerEventListeners() {
        EventBus eventBus = this.luckPerms.getEventBus();
        eventBus.subscribe(this.plugin, UserDataRecalculateEvent.class, this::onUserPermissionChange);
        eventBus.subscribe(this.plugin, GroupDataRecalculateEvent.class, this::onGroupPermissionChange);
    }

    private void onUserPermissionChange(UserDataRecalculateEvent event) {
        UUID uniqueId = event.getUser().getUniqueId();
        CraftEngine.instance().scheduler().async().execute(() -> {
            org.bukkit.entity.Player player = Bukkit.getPlayer(uniqueId);
            if (player != null) {
                this.playerCallback.accept(BukkitAdaptor.adapt(player));
            }
        });
    }

    private void onGroupPermissionChange(GroupDataRecalculateEvent event) {
        CraftEngine.instance().scheduler().asyncLater(() -> {
            String groupName = event.getGroup().getName();
            Bukkit.getOnlinePlayers().forEach(player -> {
                UUID uuid = player.getUniqueId();
                User user = luckPerms.getUserManager().getUser(uuid);
                if (user == null) return;
                boolean inGroup = user.getInheritedGroups(user.getQueryOptions()).stream()
                        .anyMatch(g -> g.getName().equals(groupName));
                if (inGroup) {
                    this.playerCallback.accept(BukkitAdaptor.adapt(player));
                }
            });
        }, 1L, TimeUnit.SECONDS);
    }
}
