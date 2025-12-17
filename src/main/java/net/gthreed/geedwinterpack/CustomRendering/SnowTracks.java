package net.gthreed.geedwinterpack.CustomRendering;

import net.gthreed.geedwinterpack.ModGameRules;
import net.gthreed.geedwinterpack.block.ModBlocks;
import net.gthreed.geedwinterpack.block.snowpile.SnowMode;
import net.gthreed.geedwinterpack.block.snowpile.SnowPileBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SnowTracks {
    private static final Map<UUID, Vec2> LAST_STEP_POS = new HashMap<>();
    private static final Map<UUID, Long> LAST_PRINT_KEY = new HashMap<>();
    private static final double MIN_STEP_DIST = 0.12;
    private static final double MIN_HSPEED2 = 1e-5;


    public static void onEntityTick(LivingEntity ent) {
        Level level = ent.level();
        if (level instanceof ServerLevel sl &&
                !sl.getGameRules().get(ModGameRules.ENABLE_SNOW_TRACKS))
            return;


        UUID id = ent.getUUID();
        double ex = ent.getX(), ez = ent.getZ();
        Vec2 last = LAST_STEP_POS.get(id);
        if (last != null) {
            double dx = ex - last.x, dz = ez - last.y;
            if (dx * dx + dz * dz < MIN_STEP_DIST * MIN_STEP_DIST) return;
        }
        LAST_STEP_POS.put(id, new Vec2((float) ex, (float) ez));

        if (ent.isSpectator()) return;
        if (!ent.onGround()) return;

        boolean tile = (level instanceof ServerLevel sl) && SnowMode.useTileSnow(sl);

        if (!(ent instanceof net.minecraft.world.entity.player.Player)) {
            Vec3 dm = ent.getDeltaMovement();
            double hs2 = dm.x * dm.x + dm.z * dm.z;
            if (hs2 < MIN_HSPEED2) return;
        }


        AABB bb = ent.getBoundingBox();
        double y = bb.minY - 0.05;

        int minBx = Mth.floor(bb.minX);
        int maxBx = Mth.floor(bb.maxX - 1e-6);
        int minBz = Mth.floor(bb.minZ);
        int maxBz = Mth.floor(bb.maxZ - 1e-6);

        for (int bx = minBx; bx <= maxBx; bx++) {
            for (int bz = minBz; bz <= maxBz; bz++) {

                BlockPos pos = new BlockPos(bx, Mth.floor(y), bz);
                BlockState state = level.getBlockState(pos);


                if (tile) {

                    if (!state.is(ModBlocks.SNOW_PILE)) {
                        pos = pos.below();
                        state = level.getBlockState(pos);
                        if (!state.is(ModBlocks.SNOW_PILE)) continue;
                    }

                    BlockEntity be = level.getBlockEntity(pos);
                    if (!(be instanceof SnowPileBlockEntity pile)) continue;

                    // overlap in lokalen Block-Koordinaten (0..1)
                    double ox0 = Math.max(bb.minX, bx) - bx;
                    double oz0 = Math.max(bb.minZ, bz) - bz;
                    double ox1 = Math.min(bb.maxX, bx + 1) - bx;
                    double oz1 = Math.min(bb.maxZ, bz + 1) - bz;

                    int grid = SnowPileBlockEntity.GRID;

                    int cx0 = Mth.clamp((int) Math.floor(ox0 * grid), 0, grid - 1);
                    int cz0 = Mth.clamp((int) Math.floor(oz0 * grid), 0, grid - 1);
                    int cx1 = Mth.clamp((int) Math.floor((ox1 * grid) - 1e-6), 0, grid - 1);
                    int cz1 = Mth.clamp((int) Math.floor((oz1 * grid) - 1e-6), 0, grid - 1);

                    // optional: “nur beim neuen Schritt” → Key auf Rechteck
                    long key = pos.asLong()
                            ^ ((long) cx0 << 8) ^ ((long) cz0 << 12)
                            ^ ((long) cx1 << 16) ^ ((long) cz1 << 20);

                    Long lastKey = LAST_PRINT_KEY.put(ent.getUUID(), key);
                    if (lastKey != null && lastKey == key) continue;

                    boolean changed = false;
                    int p = pressureFor(ent);

                    for (int cz = cz0; cz <= cz1; cz++) {
                        for (int cx = cx0; cx <= cx1; cx++) {
                            changed |= pile.applyFootprint(cx, cz, p);
                        }
                    }

                    if (changed) {
                        pile.smoothOnce();
                    }
                } else {
                    // vanilla layers
                    if (state.is(Blocks.SNOW)) {
                        int layers = state.getValue(SnowLayerBlock.LAYERS);
                        if (layers > 1) level.setBlock(pos, state.setValue(SnowLayerBlock.LAYERS, layers - 1), 3);
                        else level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static int pressureFor(LivingEntity ent) {
        // klein = 1, größere Mobs = 2
        return ent.getBbWidth() >= 0.9f ? 2 : 1;
    }

}

