package net.momirealms.craftengine.core.plugin.context.number;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.random.RandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record GaussianNumberProvider(double min, double max, double mean, double stdDev, int maxAttempts) implements NumberProvider {
    public static final NumberProviderFactory<GaussianNumberProvider> FACTORY = new Factory();

    @Override
    public float getFloat(RandomSource random) {
        return (float) getDouble(random);
    }

    @Override
    public double getDouble(RandomSource random) {
        int attempts = 0;
        while (attempts < this.maxAttempts) {
            double value = random.nextGaussian() * this.stdDev + this.mean;
            if (value >= this.min && value <= this.max) {
                return value;
            }
            attempts++;
        }
        return MiscUtils.clamp(this.mean, this.min, this.max);
    }

    private static class Factory implements NumberProviderFactory<GaussianNumberProvider> {

        @Override
        public GaussianNumberProvider create(ConfigSection section) {
            double min = section.getNonNullDouble("min");
            double max = section.getNonNullDouble("max");
            double mean = section.getDouble((min + max) / 2.0, "mean");
            double stdDev = section.getDouble((max - min) / 6.0, "std_dev", "std-dev");
            int maxAttempts = section.getInt(64, "max_attempts", "max-attempts");
            this.validateParameters(section.path(), min, max, stdDev, maxAttempts);
            return new GaussianNumberProvider(min, max, mean, stdDev, maxAttempts);
        }

        private void validateParameters(String path, double min, double max, double stdDev, int maxAttempts) {
            if (min >= max) {
                throw new KnownResourceException("number.less_than", path, "min", "max");
            }
            if (stdDev <= 0) {
                throw new KnownResourceException("number.greater_than", path, "std_dev", "0");
            }
            if (maxAttempts <= 0) {
                throw new KnownResourceException("number.greater_than", path, "max_attempts", "0");
            }
        }
    }
}