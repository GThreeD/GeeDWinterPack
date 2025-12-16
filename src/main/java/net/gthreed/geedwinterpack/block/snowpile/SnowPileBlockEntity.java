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

    private static final int STAMPS_PER_PIXEL = 4;
    private final int[] cells = new int[GRID * GRID];
    private final int[] compaction = new int[GRID * GRID];



    public SnowPileBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SNOW_PILE, pos, state);
        int layers = state.getValue(SnowPileBlock.LAYERS);
        Arrays.fill(this.cells, layers);
        Arrays.fill(this.compaction, 0);
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
        int idx = index(x, z);
        if (idx >= 0 && idx < compaction.length) compaction[idx] = 0;
        setCellHeight(x, z, getCellHeight(x, z) - amount);
    }

    public void incrementCell(int x, int z, int amount) {
        int idx = index(x, z);
        if (idx >= 0 && idx < compaction.length) compaction[idx] = 0;
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
        Arrays.fill(this.compaction, 0);
        onCellsChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putIntArray("cells", this.cells);
        out.putIntArray("comp", this.compaction);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);

        in.getIntArray("cells").ifPresent(arr -> {
            if (arr.length == this.cells.length) {
                int layers = getBlockState().getValue(SnowPileBlock.LAYERS);
                for (int i = 0; i < arr.length; i++) {
                    this.cells[i] = Mth.clamp(arr[i], 0, layers);
                }
            } else {
                Arrays.fill(this.cells, 0);
            }
        });

        in.getIntArray("comp").ifPresent(arr -> {
            if (arr.length == this.compaction.length) {
                System.arraycopy(arr, 0, this.compaction, 0, this.compaction.length);
            } else {
                Arrays.fill(this.compaction, 0);
            }
        });
    }

    private void markDirtyNoSync() {
        if (level != null && !level.isClientSide()) setChanged();
    }

    public int[] getCells() {
        return cells;
    }
    public static int index(int x, int z) {
        return z * GRID + x;
    }

    public int getCell(int x, int z) {
        int idx = index(x, z);
        if (idx < 0 || idx >= cells.length) return 0;
        return cells[idx];
    }

    public void clearAll() {
        Arrays.fill(this.cells, 0);
        Arrays.fill(this.compaction, 0);
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


    public void smoothOnce() {
        boolean changed = false;

        for (int z = 0; z < GRID; z++) {
            for (int x = 0; x < GRID; x++) {
                int idx = index(x, z);
                int h = cells[idx];

                // 4-neighborhood
                int[][] n = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (int[] d : n) {
                    int nx = x + d[0], nz = z + d[1];
                    if (nx < 0 || nz < 0 || nx >= GRID || nz >= GRID) continue;

                    int nidx = index(nx, nz);
                    int nh = cells[nidx];

                    if (h - nh >= 2) { // nur wenn wirklich “Hügel”
                        cells[idx]--;
                        cells[nidx]++;
                        changed = true;
                        break; // pro Zelle nur 1 Transfer
                    }
                }
            }
        }

        if (changed) onCellsChanged();
    }

    public boolean applyFootprint(int x, int z, int pressure) {
        int idx = index(x, z);
        if (idx < 0 || idx >= cells.length) return false;
        if (cells[idx] <= 0) return false;

        pressure = Math.max(1, pressure);

        int c = compaction[idx] + pressure;
        int drop = c / STAMPS_PER_PIXEL;          // wie viele Pixel wirklich runter
        compaction[idx] = c % STAMPS_PER_PIXEL;   // Rest bleibt als "Verdichtung"

        if (drop <= 0) {
            markDirtyNoSync(); // damit Verdichtung persistiert
            return false;
        }

        int layers = getBlockState().getValue(SnowPileBlock.LAYERS);
        int newVal = Mth.clamp(cells[idx] - drop, 0, layers);
        if (newVal == cells[idx]) return false;

        cells[idx] = newVal;
        if (newVal == 0) compaction[idx] = 0;

        onCellsChanged(); // sync + render update
        return true;
    }

}

