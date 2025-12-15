package net.gthreed.geedwinterpack.block.snowpile;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class SnowPileCellBlock extends Block {
    public static final IntegerProperty CELL = IntegerProperty.create("cell", 0, 15);

    public SnowPileCellBlock(ResourceKey<Block> key) {
        super(Properties.ofFullCopy(Blocks.SNOW)
                .strength(0.0F)
                .noOcclusion()
                .isViewBlocking((s, w, p) -> false)
                .isSuffocating((s, w, p) -> false)
                .setId(key));
        this.registerDefaultState(this.stateDefinition.any().setValue(CELL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CELL);
        super.createBlockStateDefinition(builder);
    }
}
