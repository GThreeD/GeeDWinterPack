package net.gthreed.geedwinterpack.CustomRendering;

import net.gthreed.geedwinterpack.block.ModBlocks;
import net.gthreed.geedwinterpack.block.snowpile.SnowPileBlock;
import net.gthreed.geedwinterpack.block.snowpile.SnowPileBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SnowTracks {
    private static final Map<UUID, Long> LAST_FOOTPRINT = new HashMap<>();

    public static void onPlayerTick(ServerPlayer player) {
        if (player.isSpectator() || player.getAbilities().flying) return;
        if (!player.onGround()) return;

        ServerLevel level = player.level();

        Vec3 feet = player.position().subtract(0, 0.1, 0);
        BlockPos pos = BlockPos.containing(feet);

        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.SNOW_PILE)) {
            pos = pos.below();
            state = level.getBlockState(pos);
            if (!state.is(ModBlocks.SNOW_PILE)) return;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SnowPileBlockEntity pile)) return;

        double localX = feet.x - pos.getX();
        double localZ = feet.z - pos.getZ();

        int grid = SnowPileBlockEntity.GRID;
        int cellX = Mth.clamp((int) (localX * grid), 0, grid - 1);
        int cellZ = Mth.clamp((int) (localZ * grid), 0, grid - 1);

        long key = pos.asLong() ^ ((long) cellX << 4) ^ (long) cellZ;
        Long last = LAST_FOOTPRINT.put(player.getUUID(), key);
        if (last != null && last == key) return; // <<< nur wenn neue Tile

        // jetzt "schrittweise" abtragen:
        pile.decrementCell(cellX, cellZ, 1);

        // optional Nachbar-Tile leicht mitnehmen:
        Vec3 motion = player.getDeltaMovement();
        int nx = cellX, nz = cellZ;
        if (Math.abs(motion.x) > Math.abs(motion.z)) nx += (motion.x > 0 ? 1 : -1);
        else if (Math.abs(motion.z) > 0.001) nz += (motion.z > 0 ? 1 : -1);
        nx = Mth.clamp(nx, 0, grid - 1);
        nz = Mth.clamp(nz, 0, grid - 1);
        pile.decrementCell(nx, nz, 1);

        if (pile.allEmpty()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static boolean allEmpty(SnowPileBlockEntity pile) {
        for (int v : pile.getCells()) {
            if (v > 0) return false;
        }
        return true;
    }

    private static void clearCell(ServerLevel level, BlockPos pos, int cellX, int cellZ) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SnowPileBlockEntity pile)) return;

        if (pile.hasNoSnow(cellX, cellZ)) return;
        pile.setSnow(cellX, cellZ, false);

        if (!allEmpty(pile)) return;

        BlockState state = level.getBlockState(pos);
        int layers = state.getValue(SnowPileBlock.LAYERS);

        if (layers > 1) {
            level.setBlock(pos, state.setValue(SnowPileBlock.LAYERS, layers - 1), 3);

            BlockEntity be2 = level.getBlockEntity(pos);
            if (be2 instanceof SnowPileBlockEntity pile2) {
                pile2.fillAll();
            }
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
