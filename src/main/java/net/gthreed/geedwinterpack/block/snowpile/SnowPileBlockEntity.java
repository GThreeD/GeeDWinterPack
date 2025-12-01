package net.gthreed.geedwinterpack.block.snowpile;

import net.gthreed.geedwinterpack.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Arrays;

public class SnowPileBlockEntity extends BlockEntity {
    public static final int GRID = 4;
    private final int[] cells = new int[GRID * GRID];


    public SnowPileBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SNOW_PILE, pos, state);
        Arrays.fill(this.cells, 1);
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
}
