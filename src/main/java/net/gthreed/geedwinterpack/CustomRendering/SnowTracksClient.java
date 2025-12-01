package net.gthreed.geedwinterpack.CustomRendering;

import net.gthreed.geedwinterpack.block.ModBlocks;
import net.gthreed.geedwinterpack.block.snowpile.SnowPileBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SnowTracksClient {

    public static void onPlayerTick(LocalPlayer player) {
        if (player == null) return;

        if (player.isSpectator() || player.getAbilities().flying) return;
        if (!player.onGround()) return;

        if (!(player.level() instanceof ClientLevel level)) return;

        Vec3 feet = player.position().subtract(0, 0.1, 0);
        BlockPos pos = BlockPos.containing(feet);

        BlockState state = level.getBlockState(pos);

        if (!state.is(ModBlocks.SNOW_PILE)) {
            pos = pos.below();
            state = level.getBlockState(pos);
            if (!state.is(ModBlocks.SNOW_PILE)) {
                return;
            }
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SnowPileBlockEntity pile)) return;

        double localX = feet.x - pos.getX();
        double localZ = feet.z - pos.getZ();

        int grid = SnowPileBlockEntity.GRID;
        int cellX = Mth.clamp((int) (localX * grid), 0, grid - 1);
        int cellZ = Mth.clamp((int) (localZ * grid), 0, grid - 1);

        Vec3 motion = player.getDeltaMovement();
        double vx = motion.x;
        double vz = motion.z;

        int neighborX = cellX;
        int neighborZ = cellZ;

        if (Math.abs(vx) > Math.abs(vz)) {
            neighborX = cellX + (vx > 0 ? 1 : -1);
        } else if (Math.abs(vz) > 0.001) {
            neighborZ = cellZ + (vz > 0 ? 1 : -1);
        }

        neighborX = Mth.clamp(neighborX, 0, grid - 1);
        neighborZ = Mth.clamp(neighborZ, 0, grid - 1);

        clearCell(pile, cellX, cellZ);
        clearCell(pile, neighborX, neighborZ);
    }

    private static void clearCell(SnowPileBlockEntity pile, int cellX, int cellZ) {
        if (pile.hasNoSnow(cellX, cellZ)) return;
        pile.setSnow(cellX, cellZ, false);
    }
}