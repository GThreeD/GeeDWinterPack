package net.gthreed.geedwinterpack.block.snowpile;

import net.gthreed.geedwinterpack.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

public class SnowPileBlockEntity extends BlockEntity {
    public static final int GRID = 4;
    private final int[] cells = new int[GRID * GRID];


    public SnowPileBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SNOW_PILE, pos, state);
        int layers = state.getValue(SnowPileBlock.LAYERS);
        Arrays.fill(this.cells, layers);
    }

    public int getCellHeight(int x, int z) {
        return getCell(x, z);
    }

    public void setCellHeight(int x, int z, int height) {
        int idx = index(x, z);
        if (idx < 0 || idx >= cells.length) return;

        int layers = getBlockState().getValue(SnowPileBlock.LAYERS);
        int newVal = Mth.clamp(height, 0, layers);
        if (cells[idx] == newVal) return;

        cells[idx] = newVal;
        onCellsChanged();
    }

    public void decrementCell(int x, int z, int amount) {
        setCellHeight(x, z, getCellHeight(x, z) - amount);
    }

    public void incrementCell(int x, int z, int amount) {
        setCellHeight(x, z, getCellHeight(x, z) + amount);
    }

    public boolean allEmpty() {
        for (int v : cells) if (v > 0) return false;
        return true;
    }

    public boolean allFull() {
        int layers = getBlockState().getValue(SnowPileBlock.LAYERS);
        for (int v : cells) if (v < layers) return false;
        return true;
    }

    public void fillToLayers() {
        int layers = getBlockState().getValue(SnowPileBlock.LAYERS);
        Arrays.fill(this.cells, layers);
        onCellsChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putIntArray("cells", this.cells);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.getIntArray("cells").ifPresent(arr -> {
            if (arr.length == this.cells.length) {
                System.arraycopy(arr, 0, this.cells, 0, this.cells.length);
            } else {
                Arrays.fill(this.cells, 0);
            }
        });
    }

    public int[] getCells() {
        return cells;
    }
    public static int index(int x, int z) {
        return z * GRID + x;
    }

    public void setCell(int index, int value) {
        if (index < 0 || index >= cells.length) return;
        cells[index] = value;
        onCellsChanged();
    }

    public int getCell(int x, int z) {
        int idx = index(x, z);
        if (idx < 0 || idx >= cells.length) return 0;
        return cells[idx];
    }

    public boolean hasNoSnow(int x, int z) {
        return getCell(x, z) <= 0;
    }

    public void setSnow(int x, int z, boolean value) {
        int idx = index(x, z);
        if (idx < 0 || idx >= cells.length) return;

        int newVal = value ? 1 : 0;
        if (cells[idx] == newVal) return;

        cells[idx] = newVal;
        onCellsChanged();
    }

    public void fillAll() {
        Arrays.fill(this.cells, 1);
        onCellsChanged();
    }

    public void clearAll() {
        Arrays.fill(this.cells, 0);
        onCellsChanged();
    }

    private void onCellsChanged() {
        if (level != null && !level.isClientSide()) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
        // nutzt saveAdditional(ValueOutput), also kommt "cells" automatisch rein
        return this.saveWithoutMetadata(lookup);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}

