package net.momirealms.craftengine.core.item.recipe.network.modern.display.slot;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

public final class TagSlotDisplay implements SlotDisplay {
    private final Key tag;

    public TagSlotDisplay(Key tag) {
        this.tag = tag;
    }

    public static TagSlotDisplay read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item> reader) {
        return new TagSlotDisplay(buf.readKey());
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item> writer) {
        buf.writeVarInt(4);
        buf.writeKey(this.tag);
    }

    @Override
    public String toString() {
        return "TagSlotDisplay{" +
                "tag=" + this.tag +
                '}';
    }
}
