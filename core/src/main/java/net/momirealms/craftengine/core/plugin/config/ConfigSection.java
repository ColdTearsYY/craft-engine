package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.pack.Identifier;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.*;
import net.momirealms.craftengine.core.world.collision.AABB;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("DuplicatedCode")
public final class ConfigSection {
    private final Map<String, Object> value;
    private final String path;

    private ConfigSection(String path, Map<String, Object> value) {
        this.value = value;
        this.path = path;
    }

    public static ConfigSection ofRoot(Map<String, Object> value) {
        return new ConfigSection("", value);
    }

    public static ConfigSection of(String path, Map<String, Object> value) {
        return new ConfigSection(path, value);
    }

    @SuppressWarnings("unchecked")
    public static ConfigSection of(String path, Object value) {
        if (!(value instanceof Map)) {
            throw new KnownResourceException(ConfigConstants.PARSE_SECTION_FAILED, path, value.getClass().getSimpleName());
        }
        return new ConfigSection(path, (Map<String, Object>) value);
    }

    public static ConfigSection ofSamePath(ConfigSection section, Map<String, Object> value) {
        return new ConfigSection(section.path, value);
    }

    public String path() {
        return path;
    }

    public String assemblePath(String key) {
        if (this.path.isEmpty()) {
            return key;
        }
        return this.path + "." + key;
    }

    public String assembleExistingPath(String first, String... keys) {
        String next = null;
        if (this.value.containsKey(first)) {
            next = first;
        } else {
            for (String key : keys) {
                if (this.value.containsKey(key)) {
                    next = key;
                    break;
                }
            }
        }
        return next;
    }

    public String assemblePath(String key, int index) {
        if (this.path.isEmpty()) {
            return key + "[" + index + "]";
        }
        return this.path + "." + key + "[" + index + "]";
    }

    public boolean containsKey(String key) {
        return this.value.containsKey(key);
    }

    public boolean containsKey(String first, String... keys) {
        if (this.value.containsKey(first)) {
            return true;
        }
        for (String key : keys) {
            if (this.value.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> values() {
        return this.value;
    }

    public Set<String> keySet() {
        return this.value.keySet();
    }

    public <T> T getValue(Function<ConfigValue, T> convertor, String key) {
        ConfigValue value = getValue(key);
        if (value == null) {
            return null;
        }
        return convertor.apply(value);
    }

    public <T> T getValue(Function<ConfigValue, T> convertor, String first, String... keys) {
        ConfigValue value = getValue(first, keys);
        if (value == null) {
            return null;
        }
        return convertor.apply(value);
    }

    public <T> T getValueOrDefault(Function<ConfigValue, T> convertor, T def, String first, String... keys) {
        ConfigValue value = getValue(first, keys);
        if (value == null) {
            return def;
        }
        return convertor.apply(value);
    }

    public <T> T getValueOrDefault(Function<ConfigValue, T> convertor, T def, String key) {
        ConfigValue value = getValue(key);
        if (value == null) {
            return def;
        }
        return convertor.apply(value);
    }

    public ConfigValue getValue(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return null;
        }
        return new ConfigValue(assemblePath(key), value);
    }

    public ConfigValue getValue(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return new ConfigValue(assemblePath(first), firstValue);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return new ConfigValue(assemblePath(key), value);
            }
        }
        return null;
    }

    @NotNull
    public ConfigValue getNonNullValue(String argType, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, assemblePath(key), TranslationManager.instance().translate(argType));
        }
        return new ConfigValue(assemblePath(key), value);
    }

    @NotNull
    public ConfigValue getNonNullValue(String argType, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return new ConfigValue(assemblePath(first), firstValue);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return new ConfigValue(assemblePath(key), value);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, assemblePath(first), TranslationManager.instance().translate(argType));
    }

    // --- Basic Getters ---

    @Nullable
    public Object get(String key) {
        return this.value.get(key);
    }

    @Nullable
    public Object get(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return firstValue;
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Nullable
    public <T> T get(Function<Object, T> convertor, String key) {
        Object o = this.value.get(key);
        if (o == null) {
            return null;
        }
        return convertor.apply(o);
    }

    @Nullable
    public <T> T get(Function<Object, T> convertor, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return convertor.apply(firstValue);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return convertor.apply(value);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(T defaultValue, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(T defaultValue, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return (T) firstValue;
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return (T) value;
            }
        }
        return defaultValue;
    }

    public <T> T getOrDefault(Function<Object, T> convertor, T def, String key) {
        Object o = this.value.get(key);
        if (o == null) {
            return def;
        }
        return convertor.apply(o);
    }

    public <T> T getOrDefault(Function<Object, T> convertor, T def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return convertor.apply(firstValue);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return convertor.apply(value);
            }
        }
        return def;
    }

    @NotNull
    public <T> T getNonNull(Function<Object, T> convertor, String argType, String first) {
        Object firstValue = this.value.get(first);
        if (firstValue == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, TranslationManager.instance().translate(argType));
        }
        return convertor.apply(firstValue);
    }

    @NotNull
    public <T> T getNonNull(Function<Object, T> convertor, String argType, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return convertor.apply(firstValue);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return convertor.apply(value);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, TranslationManager.instance().translate(argType));
    }

    // --- String Getters ---

    @NotNull
    public String getNonEmptyString(String key) {
        return validateString(key, getNonNullString(key));
    }

    @NotNull
    public String getNonEmptyString(String first, String... keys) {
        return validateString(first, getNonNullString(first, keys));
    }

    private String validateString(String key, String value) {
        if (value.isEmpty()) {
            throw new KnownResourceException(ConfigConstants.PARSE_NONEMPTY_STRING_FAILED, assemblePath(key));
        }
        return value;
    }

    @NotNull
    public String getNonNullString(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_STRING));
        }
        return value.toString();
    }

    @NotNull
    public String getNonNullString(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return firstValue.toString();
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_STRING));
    }

    @Nullable
    public String getString(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    @Nullable
    public String getString(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return firstValue.toString();
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    public String getDefaultedString(String def, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return value.toString();
    }

    public String getDefaultedString(String def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return firstValue.toString();
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return def;
    }

    // --- Identifier Getters ---

    @NotNull
    public Key getNonNullIdentifier(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_IDENTIFIER));
        }
        return getAsIdentifier(value, key);
    }

    @NotNull
    public Key getNonNullIdentifier(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsIdentifier(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsIdentifier(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_IDENTIFIER));
    }

    public Key getIdentifier(Key def, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return getAsIdentifier(value, key);
    }

    public Key getIdentifier(Key def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsIdentifier(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsIdentifier(value, key);
            }
        }
        return def;
    }

    @Nullable
    public Key getIdentifier(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return null;
        }
        return getAsIdentifier(value, key);
    }

    @Nullable
    public Key getIdentifier(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsIdentifier(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsIdentifier(value, key);
            }
        }
        return null;
    }

    private Key getAsIdentifier(Object value, String key) {
        String stringFormat = value.toString();
        if (Identifier.isValid(stringFormat)) {
            return Key.of(stringFormat);
        } else {
            throw new KnownResourceException(ConfigConstants.PARSE_IDENTIFIER_FAILED, this.assemblePath(key), stringFormat);
        }
    }

    // --- Enum Getters ---

    @NotNull
    public <T extends Enum<T>> T getNonNullEnum(Class<T> clazz, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.ARGUMENT_ENUM, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_ENUM, EnumUtils.toString(clazz.getEnumConstants())));
        }
        return getAsEnum(value, clazz, key);
    }

    @NotNull
    public <T extends Enum<T>> T getNonNullEnum(Class<T> clazz, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsEnum(firstValue, clazz, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsEnum(value, clazz, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.ARGUMENT_ENUM, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_ENUM, EnumUtils.toString(clazz.getEnumConstants())));
    }

    @Nullable
    public <T extends Enum<T>> T getEnum(Class<T> clazz, String key) {
        return getEnum(null, clazz, key);
    }

    @Nullable
    public <T extends Enum<T>> T getEnum(Class<T> clazz, String first, String... keys) {
        return getEnum(null, clazz, first, keys);
    }

    public <T extends Enum<T>> T getEnum(T def, Class<T> clazz, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return getAsEnum(value, clazz, key);
    }

    public <T extends Enum<T>> T getEnum(T def, Class<T> clazz, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsEnum(firstValue, clazz, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsEnum(value, clazz, key);
            }
        }
        return def;
    }
    
    private <T extends Enum<T>> T getAsEnum(Object value, Class<T> clazz, String key) {
        String enumString = value.toString();
        try {
            return Enum.valueOf(clazz, enumString.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new KnownResourceException(ConfigConstants.PARSE_ENUM_FAILED, assemblePath(key), enumString, EnumUtils.toString(clazz.getEnumConstants()));
        }
    }

    // --- Boolean Getters ---

    public boolean getBoolean(String key) {
        return getBoolean(false, key);
    }

    public boolean getBoolean(String first, String... keys) {
        return getBoolean(false, first, keys);
    }

    public boolean getBoolean(boolean def, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return getAsBoolean(value, key);
    }

    public boolean getBoolean(boolean def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsBoolean(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsBoolean(value, key);
            }
        }
        return def;
    }

    public boolean getNonNullBoolean(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_BOOLEAN));
        }
        return getAsBoolean(value, key);
    }

    public boolean getNonNullBoolean(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsBoolean(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsBoolean(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_BOOLEAN));
    }

    private boolean getAsBoolean(Object obj, String key) {
        switch (obj) {
            case null -> { return false; }
            case Boolean b -> { return b; }
            case Number n -> {
                if (n.byteValue() == 0) return false;
                if (n.byteValue() > 0) return true;
                throw new KnownResourceException(ConfigConstants.PARSE_BOOLEAN_FAILED, this.assemblePath(key), String.valueOf(n));
            }
            case String s -> {
                if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on")) return true;
                if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("off")) return false;
                throw new KnownResourceException(ConfigConstants.PARSE_BOOLEAN_FAILED, this.assemblePath(key), s);
            }
            default -> throw new KnownResourceException(ConfigConstants.PARSE_BOOLEAN_FAILED, this.assemblePath(key), obj.toString());
        }
    }

    // --- Int Getters ---

    public int getInt(String key) {
        return getInt(0, key);
    }

    public int getInt(String first, String... keys) {
        return getInt(0, first, keys);
    }

    public int getInt(int def, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return getAsInt(value, key);
    }

    public int getInt(int def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsInt(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsInt(value, key);
            }
        }
        return def;
    }

    public int getNonNullInt(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_INT));
        }
        return getAsInt(value, key);
    }

    public int getNonNullInt(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsInt(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsInt(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_INT));
    }

    private int getAsInt(Object obj, String key) {
        switch (obj) {
            case null -> { return 0; }
            case Integer i -> { return i; }
            case Number n -> { return n.intValue(); }
            case String s -> {
                try {
                    return Integer.parseInt(s.replace("_", ""));
                } catch (NumberFormatException e) {
                    throw new KnownResourceException(ConfigConstants.PARSE_INT_FAILED, this.assemblePath(key), s);
                }
            }
            case Boolean b -> { return b ? 1 : 0; }
            default -> throw new KnownResourceException(ConfigConstants.PARSE_INT_FAILED, this.assemblePath(key), obj.toString());
        }
    }

    // --- Long Getters ---

    public long getLong(String key) {
        return getLong(0L, key);
    }

    public long getLong(String first, String... keys) {
        return getLong(0L, first, keys);
    }

    public long getLong(long def, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return getAsLong(value, key);
    }

    public long getLong(long def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsLong(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsLong(value, key);
            }
        }
        return def;
    }

    public long getNonNullLong(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_LONG));
        }
        return getAsLong(value, key);
    }

    public long getNonNullLong(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsLong(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsLong(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_LONG));
    }

    private long getAsLong(Object obj, String key) {
        switch (obj) {
            case null -> { return 0L; }
            case Long l -> { return l; }
            case Number n -> { return n.longValue(); }
            case String s -> {
                try {
                    return Long.parseLong(s.replace("_", ""));
                } catch (NumberFormatException e) {
                    throw new KnownResourceException(ConfigConstants.PARSE_LONG_FAILED, this.assemblePath(key), s);
                }
            }
            case Boolean b -> { return b ? 1L : 0L; }
            default -> throw new KnownResourceException(ConfigConstants.PARSE_LONG_FAILED, this.assemblePath(key), obj.toString());
        }
    }

    // --- Float Getters ---

    public float getFloat(String key) {
        return getFloat(0f, key);
    }

    public float getFloat(String first, String... keys) {
        return getFloat(0f, first, keys);
    }

    public float getFloat(float def, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return getAsFloat(value, key);
    }

    public float getFloat(float def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsFloat(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsFloat(value, key);
            }
        }
        return def;
    }

    public float getNonNullFloat(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_FLOAT));
        }
        return getAsFloat(value, key);
    }

    public float getNonNullFloat(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsFloat(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsFloat(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_FLOAT));
    }

    private float getAsFloat(Object obj, String key) {
        switch (obj) {
            case null -> { return 0.0f; }
            case Float f -> { return f; }
            case Number n -> { return n.floatValue(); }
            case String s -> {
                try {
                    return Float.parseFloat(s.replace("_", ""));
                } catch (NumberFormatException e) {
                    throw new KnownResourceException(ConfigConstants.PARSE_FLOAT_FAILED, this.assemblePath(key), s);
                }
            }
            case Boolean b -> { return b ? 1.0f : 0.0f; }
            default -> throw new KnownResourceException(ConfigConstants.PARSE_FLOAT_FAILED, this.assemblePath(key), obj.toString());
        }
    }

    // --- Double Getters ---

    public double getDouble(String key) {
        return getDouble(0.0, key);
    }

    public double getDouble(String first, String... keys) {
        return getDouble(0.0, first, keys);
    }

    public double getDouble(double def, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return getAsDouble(value, key);
    }

    public double getDouble(double def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsDouble(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsDouble(value, key);
            }
        }
        return def;
    }

    public double getNonNullDouble(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_DOUBLE));
        }
        return getAsDouble(value, key);
    }

    public double getNonNullDouble(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsDouble(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsDouble(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_DOUBLE));
    }

    private double getAsDouble(Object obj, String key) {
        switch (obj) {
            case null -> { return 0.0; }
            case Double d -> { return d; }
            case Number n -> { return n.doubleValue(); }
            case String s -> {
                try {
                    return Double.parseDouble(s.replace("_", ""));
                } catch (NumberFormatException e) {
                    throw new KnownResourceException(ConfigConstants.PARSE_DOUBLE_FAILED, this.assemblePath(key), s);
                }
            }
            case Boolean b -> { return b ? 1.0 : 0.0; }
            default -> throw new KnownResourceException(ConfigConstants.PARSE_DOUBLE_FAILED, this.assemblePath(key), obj.toString());
        }
    }

    // --- Section Getters ---

    @Nullable
    public ConfigSection getSection(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return null;
        }
        return getAsSection(value, key);
    }

    @Nullable
    public ConfigSection getSection(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsSection(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsSection(value, key);
            }
        }
        return null;
    }

    public ConfigSection getNonNullSection(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_SECTION));
        }
        return getAsSection(value, key);
    }

    public ConfigSection getNonNullSection(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsSection(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsSection(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_SECTION));
    }

    @SuppressWarnings("unchecked")
    private ConfigSection getAsSection(Object obj, String key) {
        if (obj instanceof Map<?, ?> map) {
            return of(assemblePath(key), (Map<String, Object>) map);
        }
        throw new KnownResourceException(ConfigConstants.PARSE_SECTION_FAILED, assemblePath(key), obj.getClass().getSimpleName());
    }

    // --- Vec3 Getters ---

    @Nullable
    public Vector3f getVector3f(String key) {
        return getVector3f(null, key);
    }

    @Nullable
    public Vector3f getVector3f(String first, String... keys) {
        return getVector3f(null, first, keys);
    }

    @Nullable
    public Vector3f getVector3f(Vector3f def, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return getAsVector3f(value, key);
    }

    @Nullable
    public Vector3f getVector3f(Vector3f def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsVector3f(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsVector3f(value, key);
            }
        }
        return def;
    }

    public Vector3f getNonNullVector3f(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_VEC3));
        }
        return getAsVector3f(value, key);
    }

    public Vector3f getNonNullVector3f(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsVector3f(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsVector3f(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_VEC3));
    }

    private Vector3f getAsVector3f(Object obj, String key) {
        try {
            switch (obj) {
                case Vector3f v -> { return v; }
                case Number n -> { return new Vector3f(n.floatValue()); }
                case List<?> list -> {
                    if (list.size() == 3) {
                        return new Vector3f(
                                Float.parseFloat(list.get(0).toString()),
                                Float.parseFloat(list.get(1).toString()),
                                Float.parseFloat(list.get(2).toString())
                        );
                    } else if (list.size() == 1) {
                        return new Vector3f(Float.parseFloat(list.getFirst().toString()));
                    }
                }
                case String s -> {
                    String[] split = s.replace("_", "").split(",");
                    if (split.length == 3) {
                        return new Vector3f(Float.parseFloat(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2]));
                    } else if (split.length == 1) {
                        return new Vector3f(Float.parseFloat(split[0]));
                    }
                }
                default -> {}
            }
        } catch (Exception ignored) {
        }
        throw new KnownResourceException(ConfigConstants.PARSE_VEC3_FAILED, assemblePath(key), obj.toString());
    }

    // --- Quaternion Getters ---

    @Nullable
    public Quaternionf getQuaternionf(String key) {
        return getQuaternionf((Quaternionf) null, key);
    }

    @Nullable
    public Quaternionf getQuaternionf(String first, String... keys) {
        return getQuaternionf(null, first, keys);
    }

    @Nullable
    public Quaternionf getQuaternionf(Quaternionf def, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return getAsQuaternionf(value, key);
    }

    @Nullable
    public Quaternionf getQuaternionf(Quaternionf def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsQuaternionf(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsQuaternionf(value, key);
            }
        }
        return def;
    }

    public Quaternionf getNonNullQuaternionf(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_QUATERNION));
        }
        return getAsQuaternionf(value, key);
    }

    public Quaternionf getNonNullQuaternionf(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsQuaternionf(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsQuaternionf(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_QUATERNION));
    }

    private Quaternionf getAsQuaternionf(Object obj, String key) {
        try {
            switch (obj) {
                case Quaternionf q -> { return q; }
                case Number n -> {
                    return QuaternionUtils.toQuaternionf(0, (float) -Math.toRadians(n.floatValue()), 0);
                }
                case List<?> list -> {
                    if (list.size() == 4) {
                        return new Quaternionf(
                                Float.parseFloat(list.get(0).toString()),
                                Float.parseFloat(list.get(1).toString()),
                                Float.parseFloat(list.get(2).toString()),
                                Float.parseFloat(list.get(3).toString())
                        );
                    } else if (list.size() == 1) {
                        float v = Float.parseFloat(list.getFirst().toString());
                        return QuaternionUtils.toQuaternionf(0, (float) -Math.toRadians(v), 0);
                    }
                }
                case String s -> {
                    String[] split = s.replace("_", "").split(",");
                    switch (split.length) {
                        case 4 -> {
                            return new Quaternionf(Float.parseFloat(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3]));
                        }
                        case 3 -> {
                            return QuaternionUtils.toQuaternionf((float) Math.toRadians(Float.parseFloat(split[2])), (float) Math.toRadians(Float.parseFloat(split[1])), (float) Math.toRadians(Float.parseFloat(split[0])));
                        }
                        case 2 -> {
                            return QuaternionUtils.toQuaternionf((float) Math.toRadians(Float.parseFloat(split[1])), (float) Math.toRadians(Float.parseFloat(split[0])), 0);
                        }
                        case 1 -> {
                            return QuaternionUtils.toQuaternionf(0, (float) -Math.toRadians(Float.parseFloat(split[0])), 0);
                        }
                    }
                }
                default -> {}
            }
        } catch (Exception ignored) {
        }
        throw new KnownResourceException(ConfigConstants.PARSE_QUATERNION_FAILED, assemblePath(key), obj.toString());
    }

    // --- AABB Getters ---

    @Nullable
    public AABB getAABB(String key) {
        return getAABB(null, key);
    }

    @Nullable
    public AABB getAABB(String first, String... keys) {
        return getAABB(null, first, keys);
    }

    @Nullable
    public AABB getAABB(AABB def, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return getAsAABB(value, key);
    }

    @Nullable
    public AABB getAABB(AABB def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsAABB(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsAABB(value, key);
            }
        }
        return def;
    }

    public AABB getNonNullAABB(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_AABB));
        }
        return getAsAABB(value, key);
    }

    public AABB getNonNullAABB(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsAABB(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsAABB(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_AABB));
    }

    private AABB getAsAABB(Object obj, String key) {
        try {
            switch (obj) {
                case AABB aabb -> { return aabb; }
                case Number n -> {
                    double half = n.doubleValue() / 2.0;
                    return new AABB(-half, -half, -half, half, half, half);
                }
                default -> {
                    double[] args;
                    if (obj instanceof List<?> list) {
                        args = list.stream().mapToDouble(o -> {
                            if (o instanceof Number n) return n.doubleValue();
                            return Double.parseDouble(o.toString().replace("_", ""));
                        }).toArray();
                    } else {
                        String[] split = obj.toString().replace("_", "").split(",");
                        args = new double[split.length];
                        for (int i = 0; i < split.length; i++) {
                            args[i] = Double.parseDouble(split[i].trim());
                        }
                    }

                    return switch (args.length) {
                        case 1 -> {
                            double h = args[0] / 2.0;
                            yield new AABB(-h, -h, -h, h, h, h);
                        }
                        case 2 -> {
                            double hX = args[0] / 2.0;
                            double hY = args[1] / 2.0;
                            yield new AABB(-hX, -hY, -hX, hX, hY, hX);
                        }
                        case 3 -> {
                            double hX = args[0] / 2.0;
                            double hY = args[1] / 2.0;
                            double hZ = args[2] / 2.0;
                            yield new AABB(-hX, -hY, -hZ, hX, hY, hZ);
                        }
                        case 6 -> new AABB(args[0], args[1], args[2], args[3], args[4], args[5]);
                        default -> throw new IllegalArgumentException();
                    };
                }
            }
        } catch (Exception ignored) {
        }
        throw new KnownResourceException(ConfigConstants.PARSE_AABB_FAILED, assemblePath(key), obj.toString());
    }

    // --- SNBT Getters ---

    @Nullable
    public Tag getSNBT(final String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return null;
        }
        return getAsSNBT(value, key);
    }

    @Nullable
    public Tag getSNBT(String first, final String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsSNBT(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsSNBT(value, key);
            }
        }
        return null;
    }

    public Tag getNonNullSNBT(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_SNBT));
        }
        return getAsSNBT(value, key);
    }

    public Tag getNonNullSNBT(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsSNBT(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsSNBT(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_SNBT));
    }

    private Tag getAsSNBT(Object obj, String key) {
        if (obj instanceof Tag tag) {
            return tag;
        }
        String snbt = obj.toString();
        try {
            return TagParser.parseTagFully(snbt);
        } catch (Exception e) {
            throw new KnownResourceException(ConfigConstants.PARSE_SNBT_FAILED, assemblePath(key), snbt, e.getMessage());
        }
    }

    // --- List Getters ---

    public List<Object> getNonEmptyList(String key) {
        Object value = this.value.get(key);
        if (value != null) {
            return getAsNonEmptyList(value, key);
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_LIST));
    }

    public List<Object> getNonEmptyList(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsNonEmptyList(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsNonEmptyList(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_LIST));
    }

    public List<Object> getList(String key) {
        return getList(List.of(), key);
    }

    public List<Object> getList(String first, String... keys) {
        return getList(List.of(), first, keys);
    }

    public List<Object> getList(List<Object> def, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return getAsList(value, key);
    }

    public List<Object> getList(List<Object> def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsList(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsList(value, key);
            }
        }
        return def;
    }

    public List<Object> getNonNullList(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_LIST));
        }
        return getAsList(value, key);
    }

    public List<Object> getNonNullList(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsList(firstValue, first);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsList(value, key);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_LIST));
    }

    public List<Float> getNonNullFloatList(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_LIST));
        }
        return getAsList(value, key, this::getAsFloat);
    }

    public List<Float> getNonNullFloatList(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsList(firstValue, first, this::getAsFloat);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsList(value, first, this::getAsFloat);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_LIST));
    }

    @SuppressWarnings("unchecked")
    private List<Object> getAsList(Object obj, String key) {
        if (obj instanceof List<?> list) {
            return (List<Object>) list;
        }
        throw new KnownResourceException(ConfigConstants.PARSE_LIST_FAILED, assemblePath(key), obj.getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private List<Object> getAsNonEmptyList(Object obj, String key) {
        if (obj instanceof List<?> list) {
            if (list.isEmpty()) {
                throw new KnownResourceException(ConfigConstants.PARSE_NONEMPTY_LIST_FAILED, assemblePath(key));
            }
            return (List<Object>) list;
        } else {
            return List.of(obj);
        }
    }

    private <T> List<T> getAsList(Object obj, String key, BiFunction<Object, String, T> mapper) {
        if (obj instanceof List<?> list) {
            List<T> result = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                result.add(mapper.apply(list.get(i), assemblePath(key, i)));
            }
            return result;
        }
        throw new KnownResourceException(ConfigConstants.PARSE_LIST_FAILED, assemblePath(key), obj.getClass().getSimpleName());
    }

    public List<String> getStringList(String key) {
        return getStringList(List.of(), key);
    }

    public List<String> getStringList(String first, String... keys) {
        return getStringList(List.of(), first, keys);
    }

    public List<String> getStringList(List<String> def, String key) {
        Object value = this.value.get(key);
        if (value == null) {
            return def;
        }
        return getAsStringList(value);
    }

    public List<String> getStringList(List<String> def, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsStringList(firstValue);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsStringList(value);
            }
        }
        return def;
    }

    public List<String> getNonNullStringList(String key) {
        Object value = this.value.get(key);
        if (value == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, key, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_LIST));
        }
        return getAsStringList(value);
    }

    public List<String> getNonNullStringList(String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return getAsStringList(firstValue);
        }
        for (String key : keys) {
            Object value = this.value.get(key);
            if (value != null) {
                return getAsStringList(value);
            }
        }
        throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_LIST));
    }

    private List<String> getAsStringList(Object value) {
        switch (value) {
            case null -> {
                return List.of();
            }
            case List<?> list -> {
                if (list.isEmpty()) return List.of();
                return list.stream()
                        .map(Object::toString)
                        .toList();
            }
            case String s -> {
                return List.of(s);
            }
            default -> {
                return List.of(value.toString());
            }
        }
    }

    // --- Misc ---

    public <T> List<T> parseNonEmptyList(Function<ConfigValue, T> parser, String key) {
        List<Object> list = getNonEmptyList(key);
        List<T> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ConfigValue configValue = new ConfigValue(this.assemblePath(key, i), list.get(i));
            result.add(parser.apply(configValue));
        }
        return result;
    }

    public <T> List<T> parseNonEmptyList(Function<ConfigValue, T> parser, String first, String keys) {
        ConfigValue listValue = getValue(first, keys);
        if (listValue == null) {
            throw new KnownResourceException(ConfigConstants.MISSING_ARGUMENT, this.path, first, TranslationManager.instance().translate(ConfigConstants.ARGUMENT_LIST));
        }
        List<Object> list = listValue.getAsNonEmptyList();
        List<T> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ConfigValue configValue = new ConfigValue(listValue.assemblePath(i), list.get(i));
            result.add(parser.apply(configValue));
        }
        return result;
    }

    public <T> List<T> parseList(Function<ConfigValue, T> parser, String key) {
        List<Object> list = getList(key);
        if (list.isEmpty()) {
            return List.of();
        }
        List<T> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ConfigValue configValue = new ConfigValue(this.assemblePath(key, i), list.get(i));
            result.add(parser.apply(configValue));
        }
        return result;
    }

    public <T> List<T> parseList(Function<ConfigValue, T> parser, String first, String... keys) {
        ConfigValue listValue = getValue(first, keys);
        if (listValue == null) {
            return List.of();
        }
        List<Object> list = listValue.getAsNonEmptyList();
        if (!list.isEmpty()) {
            List<T> result = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                ConfigValue configValue = new ConfigValue(listValue.assemblePath(i), list.get(i));
                result.add(parser.apply(configValue));
            }
            return result;
        }
        return List.of();
    }

    public <T> List<T> parseSectionList(Function<ConfigSection, T> parser, String key) {
        Object target = this.value.get(key);
        if (target != null) {
            return parseSectionList(parser, key, target);
        }
        return Collections.emptyList();
    }

    public <T> List<T> parseSectionList(Function<ConfigSection, T> parser, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            return parseSectionList(parser, first, firstValue);
        }
        for (String key : keys) {
            Object target = this.value.get(key);
            if (target != null) {
                return parseSectionList(parser, key, target);
            }
        }
        return Collections.emptyList();
    }

    private <T> List<T> parseSectionList(Function<ConfigSection, T> parser, String key, Object target) {
        if (target instanceof List<?> list) {
            for (int i = 0; i < list.size() ; i++) {
                Object configInList = list.get(i);
                if (configInList == null) {
                    throw new KnownResourceException(ConfigConstants.PARSE_SECTION_FAILED, this.assemblePath(key, i), "null");
                }
                if (!(configInList instanceof Map<?,?>)) {
                    throw new KnownResourceException(ConfigConstants.PARSE_SECTION_FAILED, this.assemblePath(key, i), configInList.getClass().getSimpleName());
                }
            }
            switch (list.size()) {
                case 0 -> {
                    return Collections.emptyList();
                }
                case 1 -> {
                    return List.of(parser.apply(ConfigSection.of(this.assemblePath(key, 0), list.getFirst())));
                }
                case 2 -> {
                    return List.of(
                            parser.apply(ConfigSection.of(this.assemblePath(key, 0), list.getFirst())),
                            parser.apply(ConfigSection.of(this.assemblePath(key, 1), list.getLast()))
                    );
                }
                default -> {
                    List<T> result = new ArrayList<>(list.size());
                    for (int i = 0; i < list.size() ; i++) {
                        Object configInList = list.get(i);
                        result.add(parser.apply(ConfigSection.of(this.assemblePath(key, i), configInList)));
                    }
                    return result;
                }
            }
        } else if (target instanceof Map<?, ?> map) {
            return List.of(parser.apply(ConfigSection.of(this.assemblePath(key), MiscUtils.castToMap(map))));
        } else {
            throw new KnownResourceException(ConfigConstants.PARSE_SECTION_FAILED, this.assemblePath(key), target.getClass().getSimpleName());
        }
    }

    public void forEachSection(Consumer<ConfigSection> consumer, String key) {
        Object value = this.value.get(key);
        if (value != null) {
            forEachSection(consumer, key, value);
        }
    }

    public void forEachSection(Consumer<ConfigSection> consumer, String first, String... keys) {
        Object firstValue = this.value.get(first);
        if (firstValue != null) {
            forEachSection(consumer, first, firstValue);
        } else {
            for (String key : keys) {
                Object value = this.value.get(key);
                if (value != null) {
                    forEachSection(consumer, key, value);
                }
            }
        }
    }

    private void forEachSection(Consumer<ConfigSection> consumer, String key, Object target) {
        if (target instanceof List<?> list) {
            for (int i = 0; i < list.size() ; i++) {
                Object configInList = list.get(i);
                if (configInList == null) {
                    continue;
                }
                if (!(configInList instanceof Map<?,?>)) {
                    throw new KnownResourceException(ConfigConstants.PARSE_SECTION_FAILED, this.assemblePath(key, i), configInList.getClass().getSimpleName());
                }
                consumer.accept(ConfigSection.of(this.assemblePath(key, i), configInList));
            }
        } else if (target instanceof Map<?, ?> map) {
            consumer.accept(ConfigSection.of(this.assemblePath(key), map));
        } else {
            throw new KnownResourceException(ConfigConstants.PARSE_SECTION_FAILED, this.assemblePath(key), target.getClass().getSimpleName());
        }
    }
}