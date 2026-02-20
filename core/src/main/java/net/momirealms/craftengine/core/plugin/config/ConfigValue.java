package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.pack.Identifier;
import net.momirealms.craftengine.core.plugin.context.number.*;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record ConfigValue(String path, @NotNull Object value) {

    public Object value() {
        return this.value;
    }

    public boolean is(Class<?> type) {
        return type.isAssignableFrom(this.value.getClass());
    }

    public String assemblePath(String path) {
        return this.path + "." + path;
    }

    public String assemblePath(String path, int index) {
        return this.path + "." + path + "[" + index + "]";
    }

    public String assemblePath(int index) {
        return this.path + "[" + index + "]";
    }

    public String getAsString() {
        return this.value.toString();
    }

    public SoundData.SoundValue getAsSoundValue() {
        if (this.value instanceof Number number) {
            return SoundData.SoundValue.fixed(number.floatValue());
        } else {
            String volumeString = getAsString();
            if (volumeString.contains("~")) {
                String[] split = volumeString.split("~", 2);
                float min;
                try {
                    min = Float.parseFloat(split[0]);
                } catch (NumberFormatException e) {
                    throw new KnownResourceException(ConfigConstants.PARSE_FLOAT_FAILED, this.path, split[0]);
                }
                float max;
                try {
                    max = Float.parseFloat(split[1]);
                } catch (NumberFormatException e) {
                    throw new KnownResourceException(ConfigConstants.PARSE_FLOAT_FAILED, this.path, split[1]);
                }
                return SoundData.SoundValue.ranged(min, max);
            } else {
                try {
                    return SoundData.SoundValue.fixed(Float.parseFloat(volumeString));
                } catch (NumberFormatException e) {
                    throw new KnownResourceException(ConfigConstants.PARSE_FLOAT_FAILED, this.path, volumeString);
                }
            }
        }
    }

    public SoundData getAsSoundData(SoundData.SoundValue volume, SoundData.SoundValue pitch) {
        if (this.value instanceof Map<?,?>) {
            ConfigSection section = getAsSection();
            Key soundId = section.getIdentifier("id");
            volume = section.getValueOrDefault(ConfigValue::getAsSoundValue, volume, "volume");
            pitch = section.getValueOrDefault(ConfigValue::getAsSoundValue, pitch, "pitch");
            return new SoundData(soundId, volume, pitch);
        } else {
            return new SoundData(getAsIdentifier(), volume, pitch);
        }
    }

    public NumberProvider getAsNumber() {
        switch (this.value) {
            case Number number -> {
                return ConstantNumberProvider.constant(number.doubleValue());
            }
            case Boolean bool -> {
                return ConstantNumberProvider.constant(bool ? 1 : 0);
            }
            case Map<?, ?> ignored -> {
                return NumberProviders.fromConfig(getAsSection());
            }
            default -> {
                String string = getAsString();
                if (string.contains("~")) {
                    String[] split = string.split("~", 2);
                    double min;
                    try {
                        min = Double.parseDouble(split[0]);
                    } catch (NumberFormatException e) {
                        throw new KnownResourceException(ConfigConstants.PARSE_DOUBLE_FAILED, this.path, split[0]);
                    }
                    double max;
                    try {
                        max = Double.parseDouble(split[1]);
                    } catch (NumberFormatException e) {
                        throw new KnownResourceException(ConfigConstants.PARSE_DOUBLE_FAILED, this.path, split[1]);
                    }
                    return new UniformNumberProvider(ConstantNumberProvider.constant(min), ConstantNumberProvider.constant(max));
                } else if (string.contains("<") && string.contains(">")) {
                    return ExpressionNumberProvider.expression(string);
                } else {
                    try {
                        return ConstantNumberProvider.constant(Double.parseDouble(string));
                    } catch (NumberFormatException e) {
                        throw new KnownResourceException(ConfigConstants.PARSE_DOUBLE_FAILED, this.path, string);
                    }
                }
            }
        }
    }

    public Key getAsIdentifier() {
        String stringFormat = this.value.toString();
        if (Identifier.isValid(stringFormat)) {
            return Key.of(stringFormat);
        } else {
            throw new KnownResourceException(ConfigConstants.PARSE_IDENTIFIER_FAILED, this.path, stringFormat);
        }
    }

    public Key getAsKey() {
        return Key.of(this.value.toString());
    }

    public boolean getAsBoolean() {
        switch (this.value) {
            case Boolean b -> { return b; }
            case Number n -> {
                if (n.byteValue() == 0) return false;
                if (n.byteValue() > 0) return true;
                throw new KnownResourceException(ConfigConstants.PARSE_BOOLEAN_FAILED, this.path, String.valueOf(n));
            }
            case String s -> {
                if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on")) return true;
                if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("off")) return false;
                throw new KnownResourceException(ConfigConstants.PARSE_BOOLEAN_FAILED, this.path, s);
            }
            default -> throw new KnownResourceException(ConfigConstants.PARSE_BOOLEAN_FAILED, this.path, this.value.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public ConfigSection getAsSection() {
        if (this.value instanceof Map<?, ?> map) {
            return ConfigSection.of(this.path, (Map<String, Object>) map);
        }
        throw new KnownResourceException(ConfigConstants.PARSE_SECTION_FAILED, this.path, this.value.getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    public List<Object> getAsList() {
        if (this.value instanceof List<?> list) {
            return (List<Object>) list;
        }
        throw new KnownResourceException(ConfigConstants.PARSE_LIST_FAILED, this.path, this.value.getClass().getSimpleName());
    }
}