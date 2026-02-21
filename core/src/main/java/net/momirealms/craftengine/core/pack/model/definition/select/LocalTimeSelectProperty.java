package net.momirealms.craftengine.core.pack.model.definition.select;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LocalTimeSelectProperty implements SelectProperty {
    public static final SelectPropertyFactory<LocalTimeSelectProperty> FACTORY = new Factory();
    public static final SelectPropertyReader<LocalTimeSelectProperty> READER = new Reader();
    private final String pattern;
    private final String locale;
    private final String timeZone;

    public LocalTimeSelectProperty(@NotNull String pattern,
                                   @Nullable String locale,
                                   @Nullable String timeZone) {
        this.pattern = pattern;
        this.locale = locale;
        this.timeZone = timeZone;
    }

    public String pattern() {
        return this.pattern;
    }

    public String locale() {
        return this.locale;
    }

    public String timeZone() {
        return this.timeZone;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "local_time");
        jsonObject.addProperty("pattern", this.pattern);
        if (this.locale != null) {
            jsonObject.addProperty("locale", this.locale);
        }
        if (this.timeZone != null) {
            jsonObject.addProperty("time_zone", this.timeZone);
        }
    }

    private static class Factory implements SelectPropertyFactory<LocalTimeSelectProperty> {
        @Override
        public LocalTimeSelectProperty create(ConfigSection section) {
            String pattern = section.getNonNullString("pattern");
            String locale = section.getString("locale");
            String timeZone = section.getString("time-zone", "time_zone");
            return new LocalTimeSelectProperty(pattern, locale, timeZone);
        }
    }

    private static class Reader implements SelectPropertyReader<LocalTimeSelectProperty> {
        @Override
        public LocalTimeSelectProperty read(JsonObject json) {
            String pattern = json.get("pattern").getAsString();
            String locale = json.has("locale") ? json.get("locale").getAsString() : null;
            String timeZone = json.has("time_zone") ? json.get("time_zone").getAsString() : null;
            return new LocalTimeSelectProperty(pattern, locale, timeZone);
        }
    }
}
