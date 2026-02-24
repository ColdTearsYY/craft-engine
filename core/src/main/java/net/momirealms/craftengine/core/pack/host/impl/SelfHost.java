package net.momirealms.craftengine.core.pack.host.impl;

import io.github.bucket4j.Bandwidth;
import net.momirealms.craftengine.core.pack.host.*;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SelfHost implements ResourcePackHost {
    public static final ResourcePackHostFactory<SelfHost> FACTORY = new Factory();
    private static final SelfHost INSTANCE = new SelfHost();

    public SelfHost() {
        SelfHostHttpServer.instance().readResourcePack(Config.fileToUpload());
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        ResourcePackDownloadData data = SelfHostHttpServer.instance().generateOneTimeUrl(player);
        if (data == null) return CompletableFuture.completedFuture(List.of());
        return CompletableFuture.completedFuture(List.of(data));
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        CraftEngine.instance().scheduler().executeAsync(() -> {
            try {
                SelfHostHttpServer.instance().readResourcePack(resourcePackPath);
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public boolean canUpload() {
        return true;
    }

    @Override
    public ResourcePackHostType<SelfHost> type() {
        return ResourcePackHosts.SELF;
    }

    private static class Factory implements ResourcePackHostFactory<SelfHost> {

        @Override
        public SelfHost create(ConfigSection section) {
            SelfHostHttpServer selfHostHttpServer = SelfHostHttpServer.instance();

            // url 拼接
            String ip = section.getNonEmptyString("ip");
            int port = section.getInt(8163, "port");
            if (port <= 0) {
                throw new KnownResourceException("number.greater_than", section.assemblePath("port"), "port", "0");
            } else if (port > 65535) {
                throw new KnownResourceException("number.less_than", section.assemblePath("port"), "port", "65536");
            }
            String url = section.getDefaultedString("", "url");
            if (!url.isEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                if (!url.endsWith("/")) url  += "/";
            }

            // 其他参数
            boolean oneTimeToken = section.getBoolean(true, "one_time_token", "one-time-token");
            String protocol = section.getDefaultedString("http", "protocol");
            boolean denyNonMinecraftRequest = section.getBoolean(true, "deny_non_minecraft_request", "deny-non-minecraft-request");
            boolean strictValidation = section.getBoolean(false, "strict_validation", "strict-validation");

            // 流量控制
            Bandwidth limit = null;
            ConfigSection rateLimitingSection = section.getSection("rate_limiting", "rate-limiting");
            long maxBandwidthUsage = 0L;
            long minDownloadSpeed = 50_000L;
            if (rateLimitingSection != null) {
                ConfigValue qpsValue = rateLimitingSection.getValue("qps_per_ip", "qps-per-ip");
                if (qpsValue != null) {
                    ConfigValue[] splitValues = qpsValue.getSplitValuesRestrict("/", 2);
                    int maxRequests = splitValues[0].getAsInt();
                    int resetInterval = splitValues[1].getAsInt();
                    limit = Bandwidth.builder()
                            .capacity(maxRequests)
                            .refillGreedy(maxRequests, Duration.ofSeconds(resetInterval))
                            .build();
                }
                maxBandwidthUsage = section.getLong(0, "max_bandwidth_per_second", "max-bandwidth-per-second");
                minDownloadSpeed = section.getLong(50_000, "min_download_speed_per_player", "min-download-speed-per-player");
            }

            // 更新单例
            selfHostHttpServer.updateProperties(ip, port, url, denyNonMinecraftRequest, protocol, limit, oneTimeToken, maxBandwidthUsage, minDownloadSpeed, strictValidation);
            return INSTANCE;
        }
    }
}
