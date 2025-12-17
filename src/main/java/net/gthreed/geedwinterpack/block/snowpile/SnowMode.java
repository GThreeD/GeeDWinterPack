package net.gthreed.geedwinterpack.block.snowpile;

import net.gthreed.geedwinterpack.ModGameRules;
import net.minecraft.server.level.ServerLevel;

public final class SnowMode {
    private SnowMode() {
    }

    public static boolean useTileSnow(ServerLevel level) {
        return level.getGameRules().get(ModGameRules.USE_TILE_SNOW);
    }
}
