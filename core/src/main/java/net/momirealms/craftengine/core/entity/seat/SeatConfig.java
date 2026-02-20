package net.momirealms.craftengine.core.entity.seat;

import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.joml.Vector3f;

import java.util.List;

public record SeatConfig(Vector3f position, float yRot, boolean limitPlayerRotation) {

    public static SeatConfig fromString(String path, String arg) {
        String[] split = arg.split(" ");
        if (split.length == 1) {
            String[] vecSplit = split[0].split(",", 3);
            try {
                return new SeatConfig(
                        new Vector3f(Float.parseFloat(vecSplit[0]), Float.parseFloat(vecSplit[1]), Float.parseFloat(vecSplit[2])),
                        0, false
                );
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new KnownResourceException(ConfigConstants.PARSE_VEC3_FAILED, path, split[0]);
            }
        } else {
            float yRot;
            try {
                yRot = Float.parseFloat(split[1]);
            } catch (NumberFormatException e) {
                throw new KnownResourceException(ConfigConstants.PARSE_FLOAT_FAILED, path, split[1]);
            }
            String[] vecSplit = split[0].split(",", 3);
            try {
                return new SeatConfig(
                        new Vector3f(Float.parseFloat(vecSplit[0]), Float.parseFloat(vecSplit[1]), Float.parseFloat(vecSplit[2])),
                        yRot, true
                );
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new KnownResourceException(ConfigConstants.PARSE_VEC3_FAILED, path, split[0]);
            }
        }
    }

    public static SeatConfig[] fromObj(Object config) {
        if (config instanceof List<?>) {
            List<String> seats = MiscUtils.getAsStringList(config);
            return seats.stream()
                    .map(arg -> {
                        String[] split = arg.split(" ");
                        if (split.length == 1) return new SeatConfig(ResourceConfigUtils.getAsVector3f(split[0], "seats"), 0, false);
                        return new SeatConfig(ResourceConfigUtils.getAsVector3f(split[0], "seats"), Float.parseFloat(split[1]), true);
                    })
                    .toArray(SeatConfig[]::new);
        } else if (config != null) {
            String arg = config.toString();
            String[] split = arg.split(" ");
            if (split.length == 1) return new SeatConfig[] {new SeatConfig(ResourceConfigUtils.getAsVector3f(split[0], "seats"), 0, false)};
            return new SeatConfig[] {new SeatConfig(ResourceConfigUtils.getAsVector3f(split[0], "seats"), Float.parseFloat(split[1]), true)};
        } else {
            return new SeatConfig[0];
        }
    }
}
