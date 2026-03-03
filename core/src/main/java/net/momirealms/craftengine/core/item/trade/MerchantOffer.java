package net.momirealms.craftengine.core.item.trade;

import net.momirealms.craftengine.core.item.Item;

import java.util.Optional;
import java.util.function.Function;

public final class MerchantOffer<I> {
    private Item<I> cost1;
    private Optional<Item<I>> cost2;
    private Item<I> result;
    private final int uses;
    private final int maxUses;
    private final int specialPrice;
    private final int xp;
    private final float priceMultiplier;
    private final int demand;
    private final boolean outOfStock;

    public MerchantOffer(Item<I> cost1,
                         Optional<Item<I>> cost2,
                         Item<I> result,
                         boolean outOfStock,
                         int uses,
                         int maxUses,
                         int xp,
                         int specialPrice,
                         float priceMultiplier,
                         int demand) {
        this.cost1 = cost1;
        this.cost2 = cost2;
        this.result = result;
        this.outOfStock = outOfStock;
        this.uses = uses;
        this.maxUses = maxUses;
        this.specialPrice = specialPrice;
        this.xp = xp;
        this.priceMultiplier = priceMultiplier;
        this.demand = demand;
    }

    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.cost1 = function.apply(this.cost1);
        this.cost2 = this.cost2.map(function);
        this.result = function.apply(this.result);
    }

    public Item<I> cost1() {
        return this.cost1;
    }

    public Optional<Item<I>> cost2() {
        return this.cost2;
    }

    public Item<I> result() {
        return this.result;
    }

    public int uses() {
        return this.uses;
    }

    public int maxUses() {
        return this.maxUses;
    }

    public int specialPrice() {
        return this.specialPrice;
    }

    public int xp() {
        return this.xp;
    }

    public float priceMultiplier() {
        return this.priceMultiplier;
    }

    public int demand() {
        return this.demand;
    }

    public boolean outOfStock() {
        return this.outOfStock;
    }
}
