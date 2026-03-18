package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.NBT;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class FurniturePersistentData {
    public static final String ITEM = "item";
    public static final String VARIANT = "variant";
    @ApiStatus.Obsolete
    public static final String ANCHOR_TYPE = "anchor_type";

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private final CompoundTag data;
    private boolean unsaved;

    public FurniturePersistentData(CompoundTag data) {
        this.data = data == null ? new CompoundTag() : data;
    }

    public static FurniturePersistentData of(CompoundTag data) {
        return new FurniturePersistentData(data);
    }

    public static FurniturePersistentData ofVariant(String variant) {
        FurniturePersistentData accessor = new FurniturePersistentData(new CompoundTag());
        accessor.setVariant(variant);
        return accessor;
    }

    public CompoundTag copyTag() {
        try {
            this.readLock.lock();
            return this.data.copy();
        } finally {
            this.readLock.unlock();
        }
    }

    @ApiStatus.Internal
    public CompoundTag unsafeTag() {
        return this.data;
    }

    public void addCustomData(String key, Tag value) {
        try {
            this.writeLock.lock();
            this.data.put(key, value);
            this.unsaved = true;
        } finally {
            this.writeLock.unlock();
        }
    }

    @Nullable
    public Tag getCustomData(String key) {
        try {
            this.readLock.lock();
            return this.data.get(key);
        } finally {
            this.readLock.unlock();
        }
    }

    public void removeCustomData(String key) {
        try {
            this.writeLock.lock();
            this.data.remove(key);
            this.unsaved = true;
        } finally {
            this.writeLock.unlock();
        }
    }

    public Optional<Item> item() {
        byte[] data;
        try {
            this.readLock.lock();
            data = this.data.getByteArray(ITEM);
        } finally {
            this.readLock.unlock();
        }
        if (data == null) return Optional.empty();
        try {
            return Optional.of(CraftEngine.instance().itemManager().fromByteArray(data));
        } catch (Exception e) {
            Debugger.FURNITURE.warn(() -> "Failed to read furniture item data", e);
            return Optional.empty();
        }
    }

    public void setItem(Item item) {
        try {
            this.writeLock.lock();
            if (item == null) {
                this.data.remove(ITEM);
            } else {
                this.data.putByteArray(ITEM, item.toByteArray());
            }
            this.unsaved = true;
        } finally {
            this.writeLock.unlock();
        }
    }

    public Optional<String> variant() {
        try {
            this.readLock.lock();
            return Optional.ofNullable(this.data.getString(VARIANT));
        } finally {
            this.readLock.unlock();
        }
    }

    public void setVariant(String variant) {
        try {
            this.writeLock.lock();
            this.data.putString(VARIANT, variant);
            this.unsaved = true;
        } finally {
            this.writeLock.unlock();
        }
    }

    @ApiStatus.Obsolete
    public Optional<AnchorType> anchorType() {
        try {
            this.readLock.lock();
            if (this.data.containsKey(ANCHOR_TYPE)) return Optional.of(AnchorType.byId(this.data.getInt(ANCHOR_TYPE)));
            return Optional.empty();
        } finally {
            this.readLock.unlock();
        }
    }

    public static FurniturePersistentData fromBytes(final byte[] data) throws IOException {
        return new FurniturePersistentData(NBT.fromBytes(data));
    }

    public byte[] toBytes() throws IOException {
        try {
            this.readLock.lock();
            return NBT.toBytes(data);
        } finally {
            this.readLock.unlock();
        }
    }

    public boolean isUnsaved() {
        return this.unsaved;
    }

    public void markUnsaved() {
        this.unsaved = true;
    }

    public void clearUnsavedFlag() {
        this.unsaved = false;
    }
}
