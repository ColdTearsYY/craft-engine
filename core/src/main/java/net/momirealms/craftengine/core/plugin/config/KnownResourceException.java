package net.momirealms.craftengine.core.plugin.config;

import java.nio.file.Path;

public final class KnownResourceException extends ResourceException {
    private final String translationKey;
    private final String node;
    private final String[] arguments;
    private Path filePath;

    public KnownResourceException(String translationKey, String node, String... arguments) {
        this.node = node;
        this.arguments = arguments;
        this.translationKey = translationKey;
    }

    public KnownResourceException(Path filePath, String translationKey, String node, String... arguments) {
        this.node = node;
        this.filePath = filePath;
        this.arguments = arguments;
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return this.translationKey;
    }

    @Override
    public String node() {
        return this.node;
    }

    public String[] arguments() {
        return this.arguments;
    }

    @Override
    public Path filePath() {
        return this.filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }
}
