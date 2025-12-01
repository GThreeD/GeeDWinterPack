package net.gthreed.geedwinterpack.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.gthreed.geedwinterpack.GeeDWinterPack;
import net.gthreed.geedwinterpack.block.snowpile.SnowPileBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static BlockEntityType<SnowPileBlockEntity> SNOW_PILE;

    public static void init() {
        SNOW_PILE = register(
                "snow_pile",
                SnowPileBlockEntity::new,
                ModBlocks.SNOW_PILE
        );
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
            Block... validBlocks
    ) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(GeeDWinterPack.MOD_ID, name);
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id,
                FabricBlockEntityTypeBuilder.<T>create(factory, validBlocks).build()
        );
    }
}
