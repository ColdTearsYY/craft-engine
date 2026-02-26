package net.momirealms.craftengine.core.item.setting;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Tristate;

public record Repairable(Tristate craftingTable, Tristate anvilRepair, Tristate anvilCombine) {

    public static final Repairable UNDEFINED = new Repairable(Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED);
    public static final Repairable TRUE = new Repairable(Tristate.TRUE, Tristate.TRUE, Tristate.TRUE);
    public static final Repairable FALSE = new Repairable(Tristate.FALSE, Tristate.FALSE, Tristate.FALSE);

    public static Repairable fromConfig(ConfigSection section) {
        Tristate craftingTable = section.getValueOrDefault(it -> Tristate.of(it.getAsBoolean()), Tristate.UNDEFINED, "crafting_table", "crafting-table");
        Tristate anvilRepair = section.getValueOrDefault(it -> Tristate.of(it.getAsBoolean()), Tristate.UNDEFINED, "anvil_repair", "anvil-repair");
        Tristate anvilCombine = section.getValueOrDefault(it -> Tristate.of(it.getAsBoolean()), Tristate.UNDEFINED, "anvil_combine", "anvil-combine");
        return new Repairable(craftingTable, anvilRepair, anvilCombine);
    }
}
