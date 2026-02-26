package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.entity.projectile.ProjectileMeta;
import net.momirealms.craftengine.core.item.equipment.ComponentBasedEquipment;
import net.momirealms.craftengine.core.item.equipment.Equipment;
import net.momirealms.craftengine.core.item.equipment.Equipments;
import net.momirealms.craftengine.core.item.recipe.remainder.CraftRemainder;
import net.momirealms.craftengine.core.item.setting.*;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.*;

import java.util.*;

public final class ItemSettingsModifiers {
    public static final ItemSettingsModifierType<ItemSettingsModifier> REPAIRABLE = register(Key.ce("repairable"), value -> {
        if (value.is(Map.class)) {
            Repairable repairable = Repairable.fromConfig(value.getAsSection());
            return settings -> settings.repairable(repairable);
        } else {
            return settings -> settings.repairable(value.getAsBoolean() ? Repairable.TRUE : Repairable.FALSE);
        }
    });
    public static final ItemSettingsModifierType<ItemSettingsModifier> ENCHANTABLE = register(Key.ce("enchantable"), (value -> settings -> {
        boolean canEnchant = value.getAsBoolean();
        settings.canEnchant(canEnchant);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> KEEP_ON_DEATH_CHANCE = register(Key.ce("keep_on_death_chance"), (value -> settings -> {
        float chance = MiscUtils.clamp(value.getAsFloat(), 0, 1);
        settings.keepOnDeathChance(chance);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> DESTROY_ON_DEATH_CHANCE = register(Key.ce("destroy_on_death_chance"), (value -> settings -> {
        float chance = MiscUtils.clamp(value.getAsFloat(), 0, 1);
        settings.destroyOnDeathChance(chance);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> RENAMEABLE = register(Key.ce("renameable"), (value -> settings -> {
        boolean renameable = value.getAsBoolean();
        settings.renameable(renameable);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> DROP_DISPLAY = register(Key.ce("drop_display"), (value -> {
        if (value.is(String.class)) {
            return settings -> settings.dropDisplay(value.getAsString());
        } else {
            return settings -> settings.dropDisplay(value.getAsBoolean() ? "" : null);
        }
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> GLOW_COLOR = register(Key.ce("glow_color"), (value -> settings -> {
        LegacyChatFormatter formatter = value.getAsEnum(LegacyChatFormatter.class);
        settings.glowColor(formatter);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> ANVIL_REPAIR_ITEM = register(Key.ce("anvil_repair_item"), (value -> {
        List<AnvilRepairItem> anvilRepairItemList = value.parseAsList(it -> {
            ConfigSection section = it.getAsSection();
            return new AnvilRepairItem(
                    section.getStringList("target"),
                    section.getInt("amount"),
                    section.getDouble("percent")
            );
        });
        return settings -> settings.repairItems(anvilRepairItemList);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> FUEL_TIME = register(Key.ce("fuel_time"), (value -> settings -> {
        int fuelTime = value.getAsInt();
        settings.fuelTime(fuelTime);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> CONSUME_REPLACEMENT = register(Key.ce("consume_replacement"), (value -> settings -> {
        Key itemId = value.getAsIdentifier();
        settings.consumeReplacement(itemId);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> CRAFT_REMAINING_ITEM = register(Key.ce("craft_remaining_item"), (value -> settings -> {
        CraftRemainder remainder = value.getAsCraftRemainder();
        settings.craftRemainder(remainder);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> CRAFT_REMAINDER = register(Key.ce("craft_remainder"), (value -> settings -> {
        CraftRemainder remainder = value.getAsCraftRemainder();
        settings.craftRemainder(remainder);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> TAGS = register(Key.ce("tags"), (value -> settings -> settings.tags(new HashSet<>(value.parseAsList(it -> {
        String asString = it.getAsString();
        if (asString.charAt(0) == '#') {
            return Key.of(asString.substring(1));
        } else {
            return Key.of(asString);
        }
    })))));
    public static final ItemSettingsModifierType<ItemSettingsModifier> EQUIPPABLE = register(Key.ce("equippable"), (value -> {
        EquipmentData data = value.getAsEquipmentData();
        if (data.assetId() == null) {
            throw new IllegalArgumentException("Please move 'equippable' option to 'data' section."); // todo 修改为可翻译
        }
        ComponentBasedEquipment componentBasedEquipment = Equipments.COMPONENT.factory().create(data.assetId(), value.getAsSection());
        ((AbstractItemManager<?>) CraftEngine.instance().itemManager()).addOrMergeEquipment(componentBasedEquipment);
        ItemEquipment itemEquipment = new ItemEquipment(Tristate.FALSE, data, componentBasedEquipment);
        return settings -> settings.equipment(itemEquipment);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> EQUIPMENT = register(Key.ce("equipment"), (value -> {
        ConfigSection section = value.getAsSection();
        Tristate clientBoundModel = section.getValueOrDefault(it -> Tristate.of(it.getAsBoolean()), Tristate.UNDEFINED, "client_bound_model", "client-bound-model");
        Key assetId = section.getNonNullIdentifier("asset_id", "asset-id");
        Optional<Equipment> optionalEquipment = CraftEngine.instance().itemManager().getEquipment(assetId);
        if (optionalEquipment.isEmpty()) {
            throw new LocalizedResourceConfigException("warning.config.item.settings.equipment.invalid_asset_id");
        }
        if (VersionHelper.isOrAbove1_21_2() && section.containsKey("slot")) {
            if (optionalEquipment.get() instanceof ComponentBasedEquipment) {
                EquipmentData data = value.getAsEquipmentData();
                return settings -> settings.equipment(new ItemEquipment(clientBoundModel, data, optionalEquipment.get()));
            } else { // todo 优化写法
                // trim based
                Map<String, Object> copiedArgs = new HashMap<>(section.values());
                copiedArgs.put("asset_id", Config.sacrificedVanillaArmorType());
                ConfigValue configValue = new ConfigValue(section.path(), copiedArgs);
                EquipmentData data = configValue.getAsEquipmentData();
                return settings -> settings.equipment(new ItemEquipment(clientBoundModel, data, optionalEquipment.get()));
            }
        } else {
            return settings -> settings.equipment(new ItemEquipment(clientBoundModel, null, optionalEquipment.get()));
        }
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> CAN_PLACE = register(Key.ce("can_place"), (value -> {
        boolean bool = value.getAsBoolean();
        return settings -> settings.disableVanillaBehavior(!bool);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> TRIGGER_ADVANCEMENT = register(Key.ce("trigger_advancement"), (value -> {
        boolean bool = value.getAsBoolean();
        return settings -> settings.triggerAdvancement(bool);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> DISABLE_VANILLA_BEHAVIOR = register(Key.ce("disable_vanilla_behavior"), (value -> {
        boolean bool = value.getAsBoolean();
        return settings -> settings.disableVanillaBehavior(bool);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> PROJECTILE = register(Key.ce("projectile"), (value -> settings -> {
        ProjectileMeta meta = value.getAsProjectileMeta();
        settings.projectileMeta(meta);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> HELMET = register(Key.ce("helmet"), (value -> {
        ConfigValue configValue = value.getAsSection().getValue("equip-sound");
        if (configValue != null) {
            return settings -> settings.helmet(new Helmet(configValue.getAsSoundData(SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_1)));
        } else {
            return settings -> settings.helmet(new Helmet(SoundData.of(Key.of("minecraft:intentionally_empty"), SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_1)));
        }
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> COMPOST_PROBABILITY = register(Key.ce("compost_probability"), (value -> {
        float chance = value.getAsFloat();
        return settings -> settings.compostProbability(chance);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> DYEABLE = register(Key.ce("dyeable"), (value -> {
        boolean bool = value.getAsBoolean();
        return settings -> settings.dyeable(Tristate.of(bool));
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> RESPECT_REPAIRABLE_COMPONENT = register(Key.ce("respect_repairable_component"), (value -> {
        boolean bool = value.getAsBoolean();
        return settings -> settings.respectRepairableComponent(bool);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> DYE_COLOR = register(Key.ce("dye_color"), (value -> settings -> {
        Color color = value.getAsColor();
        settings.dyeColor(color);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> FIREWORK_COLOR = register(Key.ce("firework_color"), (value -> settings -> {
        Color color = value.getAsColor();
        settings.fireworkColor(color);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> FOOD = register(Key.ce("food"), (value -> {
        FoodData foodData = value.getAsFoodData();
        return settings -> settings.foodData(foodData);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> INVULNERABLE = register(Key.ce("invulnerable"), (value -> {
        List<DamageSource> list = value.parseAsList(it -> it.getAsEnum(DamageSource.class));
        return settings -> settings.invulnerable(list);
    }));
    public static final ItemSettingsModifierType<ItemSettingsModifier> INGREDIENT_SUBSTITUTE = register(Key.ce("ingredient_substitute"), (value -> settings -> {
        List<Key> list = value.parseAsList(ConfigValue::getAsIdentifier);
        settings.ingredientSubstitutes(list);
    }));

    private ItemSettingsModifiers() {}

    public static <M extends ItemSettingsModifier> ItemSettingsModifierType<M> register(Key id, ItemSettingsModifierFactory<M> factory) {
        ItemSettingsModifierType<M> type = new ItemSettingsModifierType<>(id, factory);
        ((WritableRegistry<ItemSettingsModifierType<? extends ItemSettingsModifier>>) BuiltInRegistries.ITEM_SETTINGS_TYPE)
                .register(ResourceKey.create(Registries.ITEM_SETTINGS_TYPE.location(), id), type);
        return type;
    }
}
