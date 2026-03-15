package net.momirealms.craftengine.core.item.recipe.network.legacy;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiFunction;
import java.util.function.Function;

@ApiStatus.Obsolete
public interface LegacyRecipe {

    default void applyClientboundData(Function<Item, Item> function) {}

    void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item> writer);

    record Type<I>(BiFunction<FriendlyByteBuf, FriendlyByteBuf.Reader<I>, LegacyRecipe> reader) {

        public LegacyRecipe read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<I> reader) {
            return this.reader.apply(buf, reader);
        }
    }
}
