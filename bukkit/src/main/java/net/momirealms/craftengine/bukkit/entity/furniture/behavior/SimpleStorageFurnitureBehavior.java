package net.momirealms.craftengine.bukkit.entity.furniture.behavior;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.gui.BukkitInventory;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.entity.furniture.CustomFurniture;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehavior;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorFactory;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.CustomDataType;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.context.InteractEntityContext;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class SimpleStorageFurnitureBehavior extends FurnitureBehavior {
    public static final FurnitureBehaviorFactory<SimpleStorageFurnitureBehavior> FACTORY = new Factory();
    public final String containerTitle;
    public final int rows;

    private SimpleStorageFurnitureBehavior(CustomFurniture furniture,
                                           String containerTitle,
                                           int rows) {
        super(furniture);
        this.containerTitle = containerTitle;
        this.rows = rows;
    }

    @Override
    public InteractionResult useOnFurniture(InteractEntityContext context, Furniture furniture) {
        ItemStorage storage = furniture.getTempData(ItemStorage.TYPE);
        if (storage == null) return InteractionResult.SUCCESS_AND_CANCEL;
        storage.onOpen(context.getPlayer());
        return InteractionResult.SUCCESS_AND_CANCEL;
    }

    @Override
    public void onDestroy(Furniture furniture) {
        ItemStorage storage = furniture.getTempData(ItemStorage.TYPE);
        if (storage == null) return;
        storage.destroy();
        furniture.removeTempData(ItemStorage.TYPE);
    }

    @Override
    public void onLoad(Furniture furniture) {
        ItemStorage storage = furniture.getTempData(ItemStorage.TYPE);
        if (storage == null) {
            storage = new ItemStorage(furniture, this);
            furniture.putTempData(ItemStorage.TYPE, storage);
        }
        storage.load();
    }

    @Override
    public void onUnload(Furniture furniture) {
        ItemStorage itemStorage = furniture.getTempData(ItemStorage.TYPE);
        if (itemStorage == null) return;
        itemStorage.unload();
        furniture.removeTempData(ItemStorage.TYPE);
    }

    private static class Factory implements FurnitureBehaviorFactory<SimpleStorageFurnitureBehavior> {

        @Override
        public SimpleStorageFurnitureBehavior create(CustomFurniture furniture, ConfigSection section) {
            return new SimpleStorageFurnitureBehavior(
                    furniture,
                    section.getString("title", "<lang:container.chest>"),
                    section.getInt("rows", 1)
            );
        }
    }

    public static final class ItemStorage implements InventoryHolder {
        private static final CustomDataType<ItemStorage> TYPE = new CustomDataType<>();
        private static final String KEY = "craftengine:simple_storage_furniture";
        public final Furniture furniture;
        private final SimpleStorageFurnitureBehavior behavior;
        private final Inventory inventory;

        private ItemStorage(Furniture furniture, SimpleStorageFurnitureBehavior behavior) {
            this.furniture = furniture;
            this.behavior = behavior;
            this.inventory = FastNMS.INSTANCE.createSimpleStorageContainer(this, this.behavior.rows * 9, false, false);
        }

        public void load() {
            this.inventory.close();
            CompoundTag data = this.furniture.persistentData.getCustomData(KEY) instanceof CompoundTag tag ? tag : new CompoundTag();
            int dataVersion = data.getInt("data_version", Config.itemDataFixerUpperFallbackVersion());
            ListTag items = Optional.ofNullable(data.getList("items")).orElseGet(ListTag::new);
            this.inventory.setStorageContents(ItemStackUtils.parseBukkitItems(items, this.behavior.rows * 9, dataVersion));
        }

        public void unload() {
            this.inventory.close();
            CompoundTag data = this.furniture.persistentData.getCustomData(KEY) instanceof CompoundTag tag ? tag : new CompoundTag();
            data.put("items", ItemStackUtils.saveBukkitItemsAsListTag(this.inventory.getStorageContents()));
            data.putInt("data_version", VersionHelper.WORLD_VERSION);
            this.furniture.persistentData.addCustomData(KEY, data);
        }

        public void destroy() {
            this.inventory.close();
            this.furniture.persistentData.removeCustomData(KEY);
            for (ItemStack stack : this.inventory.getContents()) {
                if (stack != null) {
                    this.furniture.world().dropItemNaturally(this.furniture.position(), BukkitAdaptor.adapt(stack));
                }
            }
            this.inventory.clear();
        }

        public void onOpen(Player player) {
            org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) player.platformPlayer();
            bukkitPlayer.openInventory(this.inventory);
            new BukkitInventory(this.inventory).open(player, AdventureHelper.miniMessage().deserialize(this.behavior.containerTitle, PlayerOptionalContext.of(player).tagResolvers()));
        }

        public void onClose(Player player) {
        }

        @Override
        public @NotNull Inventory getInventory() {
            return this.inventory;
        }
    }
}
