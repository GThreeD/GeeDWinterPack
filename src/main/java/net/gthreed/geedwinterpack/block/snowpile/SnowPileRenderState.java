package net.gthreed.geedwinterpack.block.snowpile;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class SnowPileRenderState extends BlockEntityRenderState {

    public final int[] cells = new int[SnowPileBlockEntity.GRID * SnowPileBlockEntity.GRID];
    public int lightCoords = 0;
    public int layers = 1;
}