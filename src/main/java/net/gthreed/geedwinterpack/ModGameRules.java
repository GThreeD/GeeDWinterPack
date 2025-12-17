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

    private ModGameRules() {
    }

    public static void init() {
    }
}
