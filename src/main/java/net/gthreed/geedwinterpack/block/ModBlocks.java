package net.gthreed.geedwinterpack.block;

import net.gthreed.geedwinterpack.block.snowpile.SnowPileBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.gthreed.geedwinterpack.GeeDWinterPack;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModBlocks {
    public static final ResourceLocation SNOW_PILE_ID =
            ResourceLocation.fromNamespaceAndPath(GeeDWinterPack.MOD_ID, "snow_pile");

    public static final Block SNOW_PILE = Registry.register(
            BuiltInRegistries.BLOCK,
            SNOW_PILE_ID,
            new SnowPileBlock(ResourceKey.create(Registries.BLOCK, SNOW_PILE_ID))
    );

    public static final Item SNOW_PILE_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            SNOW_PILE_ID,
            new BlockItem(
                    SNOW_PILE,
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, SNOW_PILE_ID))
            )
    );

    public static void init() {
    }
}


