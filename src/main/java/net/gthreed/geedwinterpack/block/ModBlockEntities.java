package net.gthreed.geedwinterpack.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.gthreed.geedwinterpack.GeeDWinterPack;
import net.gthreed.geedwinterpack.block.snowpile.SnowPileBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

public class ModBlockEntities {

    public static BlockEntityType<@NotNull SnowPileBlockEntity> SNOW_PILE;

    public static void init() {
        SNOW_PILE = register(
                SnowPileBlockEntity::new,
                ModBlocks.SNOW_PILE
        );
    }

    private static <T extends BlockEntity> BlockEntityType<@NotNull T> register(
            FabricBlockEntityTypeBuilder.Factory<? extends @NotNull T> factory,
            Block... validBlocks
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(GeeDWinterPack.MOD_ID, "snow_pile");
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id,
                FabricBlockEntityTypeBuilder.<T>create(factory, validBlocks).build()
        );
    }
}
