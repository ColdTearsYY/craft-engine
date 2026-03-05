package net.momirealms.craftengine.core.pack.host;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;

import java.net.InetSocketAddress;
import java.net.ProxySelector;

public interface ResourcePackHostFactory<T extends ResourcePackHost> {

    T create(ConfigSection section);

    default String getNonNullEnvironmentVariable(ConfigSection section, String name) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            throw new KnownResourceException("host.missing_environment_variable", section.path(), name);
        }
        return value;
    }

    default ProxySelector getProxySelector(ConfigSection section) {
        if (section == null) {
            return ProxySelector.of(null);
        }
        String proxyHost = section.getNonEmptyString("host");
        int proxyPort = section.getNonNullInt("port");
        if (proxyPort <= 0) {
            throw new KnownResourceException("number.greater_than", section.assemblePath("port"), "port", "0");
        } else if (proxyPort > 65535) {
            throw new KnownResourceException("number.less_than", section.assemblePath("port"), "port", "65536");
        }
        return ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort));
    }
}
