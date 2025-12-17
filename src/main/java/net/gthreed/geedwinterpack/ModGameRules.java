package net.gthreed.geedwinterpack;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public final class ModGameRules {
    public static final GameRule<Boolean> USE_TILE_SNOW =
            GameRuleBuilder.forBoolean(false)
                    .category(GameRuleCategory.MISC)
                    .buildAndRegister(Identifier.fromNamespaceAndPath(GeeDWinterPack.MOD_ID, "use_tile_snow"));

    public static final GameRule<Integer> MAX_SNOW_HEIGHT = GameRuleBuilder
            .forInteger(3)
            .category(GameRuleCategory.MISC)
            .buildAndRegister(Identifier.fromNamespaceAndPath(GeeDWinterPack.MOD_ID, "max_snow_height"));

    public static final GameRule<Boolean> ENABLE_SNOW_TRACKS =
            GameRuleBuilder.forBoolean(false)
                    .category(GameRuleCategory.MISC)
                    .buildAndRegister(Identifier.fromNamespaceAndPath(GeeDWinterPack.MOD_ID, "enable_snow_tracks"));

    public static void init() {
    }
}
