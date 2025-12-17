package net.gthreed.geedwinterpack.CustomRendering;

import net.gthreed.geedwinterpack.ModGameRules;
import net.gthreed.geedwinterpack.block.ModBlocks;
import net.gthreed.geedwinterpack.block.snowpile.SnowMode;
import net.gthreed.geedwinterpack.block.snowpile.SnowPileBlock;
import net.gthreed.geedwinterpack.block.snowpile.SnowPileBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SnowAccumulation {
    private static int MAX_LAYERS = ModGameRules.MAX_SNOW_HEIGHT.defaultValue();
    private static int NEAR_TOP_TILES_FOR_LAYER_UP = ModGameRules.MAX_SNOW_HEIGHT.defaultValue() - 1;
    private static final double COVERAGE = 0.65; // wie viel Fläche überhaupt Schnee bekommt
    private static final int RND_PASSES = 2;      // smoothing passes

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(Level.OVERWORLD)) return;
        int maxH = level.getGameRules().get(ModGameRules.MAX_SNOW_HEIGHT);

        MAX_LAYERS = Mth.clamp(maxH, 1, 64);
        NEAR_TOP_TILES_FOR_LAYER_UP = MAX_LAYERS - 1;

        if (level.isClientSide()) return;

        for (var player : level.players()) {
            RandomSource random = level.random;
            Vec3 pos = player.position();

            for (int i = 0; i < 20; i++) {
                double radius = 10;
                double px = pos.x + (random.nextDouble() - 0.5) * radius * 2;
                double pz = pos.z + (random.nextDouble() - 0.5) * radius * 2;

                int x = Mth.floor(px);
                int z = Mth.floor(pz);

                BlockPos placePos = pickSnowPlacePos(level, x, z);
                if (placePos == null) continue;

                maybePlaceSnow(level, placePos, placePos.below(), random);
            }
        }
    }

    private static BlockPos pickSnowPlacePos(ServerLevel level, int x, int z) {
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);
        BlockPos pos = new BlockPos(x, y, z);

        // Wenn Heightmap "über" einer replaceable Pflanze landet: 1 runter und die Pflanze ersetzen
        if (level.getBlockState(pos).isAir()) {
            BlockPos down = pos.below();
            BlockState downState = level.getBlockState(down);
            if (!downState.isAir() && downState.canBeReplaced()) {
                pos = down;
            }
        }

        if (!level.canSeeSky(pos)) return null;

        // Nie in/auf Flüssigkeiten platzieren
        if (!level.getFluidState(pos).isEmpty()) return null;
        if (!level.getFluidState(pos.below()).isEmpty()) return null;

        // Muss dort überleben können (verhindert u.a. Wasser/Blätter/etc.)
        BlockState snow1 = ModBlocks.SNOW_PILE.defaultBlockState().setValue(SnowPileBlock.LAYERS, 1);
        if (!snow1.canSurvive(level, pos)) return null;

        BlockState at = level.getBlockState(pos);
        if (!(at.isAir() || at.canBeReplaced())) return null;

        return pos;
    }

    private static void maybePlaceSnow(ServerLevel level, BlockPos pos, BlockPos belowPos, RandomSource random) {
        boolean tile = SnowMode.useTileSnow(level);

        if (tile) {
            maybePlaceOrGrowTileSnow(level, pos, belowPos, random);
        } else {
            maybePlaceOrGrowVanillaSnow(level, pos, belowPos, random);
        }
    }

    private static void maybePlaceOrGrowTileSnow(ServerLevel level, BlockPos pos, BlockPos belowPos, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        BlockState below = level.getBlockState(belowPos);

        if (state.is(ModBlocks.SNOW_PILE)) {
            growExistingPile(level, pos, state, random);
            return;
        }

        if (below.is(ModBlocks.SNOW_PILE)) {
            growExistingPile(level, belowPos, below, random);
            return;
        }

        if (state.isAir() || state.canBeReplaced()) {
            if (random.nextFloat() < 0.25f) {
                level.setBlock(
                        pos,
                        ModBlocks.SNOW_PILE.defaultBlockState().setValue(SnowPileBlock.LAYERS, 1),
                        3
                );

                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof SnowPileBlockEntity pile) {
                    pile.clearAll();
                    int x = random.nextInt(SnowPileBlockEntity.GRID);
                    int z = random.nextInt(SnowPileBlockEntity.GRID);
                    pile.setCellHeight(x, z, 1);
                }
            }
        }
    }

    private static void maybePlaceOrGrowVanillaSnow(ServerLevel level, BlockPos pos, BlockPos belowPos, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        BlockState below = level.getBlockState(belowPos);

        // schon Schnee da -> wachsen / schmelzen
        if (state.is(Blocks.SNOW)) {
            if (isHeatNearby(level, pos, 3, 2)) {
                meltVanilla(level, pos, state);
                return;
            }

            int layers = state.getValue(SnowLayerBlock.LAYERS);
            if (random.nextFloat() < 0.015f && layers < Mth.clamp(MAX_LAYERS, 1, 8)) {
                level.setBlock(pos, state.setValue(SnowLayerBlock.LAYERS, layers + 1), 3);
            }
            return;
        }

        // falls oben Air ist, aber unten Schnee liegt -> unten wachsen
        if (state.isAir() && below.is(Blocks.SNOW)) {
            if (isHeatNearby(level, belowPos, 3, 2)) {
                meltVanilla(level, belowPos, below);
                return;
            }

            int layers = below.getValue(SnowLayerBlock.LAYERS);
            if (random.nextFloat() < 0.015f && layers < Mth.clamp(MAX_LAYERS, 1, 8)) {
                level.setBlock(belowPos, below.setValue(SnowLayerBlock.LAYERS, layers + 1), 3);
            }
            return;
        }

        // neu platzieren (wie bisher, nur eben Blocks.SNOW statt SnowPile)
        if (state.isAir() || state.canBeReplaced()) {
            if (random.nextFloat() < 0.025f) {
                level.setBlock(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1), 3);
            }
        }
    }

    private static void meltVanilla(ServerLevel level, BlockPos pos, BlockState state) {
        int layers = state.getValue(SnowLayerBlock.LAYERS);
        if (layers > 1) level.setBlock(pos, state.setValue(SnowLayerBlock.LAYERS, layers - 1), 3);
        else level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    private static boolean isHeatBlock(BlockState s) {
        return s.is(Blocks.TORCH) || s.is(Blocks.WALL_TORCH)
                || s.is(Blocks.CAMPFIRE) || s.is(Blocks.SOUL_CAMPFIRE)
                || s.is(Blocks.FIRE) || s.is(Blocks.LAVA)
                || s.is(Blocks.MAGMA_BLOCK);
    }

    private static boolean isHeatNearby(ServerLevel level, BlockPos pos, int r, int yRange) {
        int r2 = r * r;
        for (int dy = -yRange; dy <= yRange; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz > r2) continue;
                    if (isHeatBlock(level.getBlockState(pos.offset(dx, dy, dz)))) return true;
                }
            }
        }
        return false;
    }

    private static int nearestHeatDistSq(ServerLevel level, BlockPos pos, int r, int yRange) {
        int best = Integer.MAX_VALUE;
        int r2 = r * r;

        for (int dy = -yRange; dy <= yRange; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dz * dz; // Kreis in XZ
                    if (d2 > r2) continue;

                    BlockPos p = pos.offset(dx, dy, dz);
                    if (isHeatBlock(level.getBlockState(p))) {
                        int full = d2 + dy * dy; // optional 3D Einfluss
                        if (full < best) best = full;
                    }
                }
            }
        }
        return best;
    }


    private static void melt(ServerLevel level, BlockPos pos, BlockState state) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SnowPileBlockEntity pile) {
            RandomSource rand = level.random;
            int x = rand.nextInt(SnowPileBlockEntity.GRID);
            int z = rand.nextInt(SnowPileBlockEntity.GRID);
            pile.decrementCell(x, z, 1);

            if (pile.allEmpty()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static void growExistingPile(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        int best = nearestHeatDistSq(level, pos, 6, 2);
        int maxAllowed = MAX_LAYERS;
        if (best != Integer.MAX_VALUE) {
            float dist = (float) Math.sqrt(best);
            float r = 6f;
            float t = 1f - (dist / r);
            if (random.nextFloat() < 0.35f * t) {
                int melts = 1 + (t > 0.66f ? 1 : 0);
                for (int k = 0; k < melts; k++) melt(level, pos, state);
            }

            maxAllowed = Math.max(1, Math.round(13 * (dist / r)));
        }


        if (isHeatNearby(level, pos, 3, 2)) {
            melt(level, pos, state);
            return;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SnowPileBlockEntity pile)) return;

        int layers = state.getValue(SnowPileBlock.LAYERS);

        if (random.nextFloat() < 0.18f) {
            for (int tries = 0; tries < 6; tries++) {
                int[] c = pickLowCell(pile, random);
                int cx = c[0], cz = c[1];
                if (pile.getCellHeight(cx, cz) < layers) {
                    pile.incrementCell(cx, cz, 1);
                    break;
                }
            }

            pile.smoothOnce();
        }

        if (layers < maxAllowed && shouldLayerUp(pile, layers) && random.nextFloat() < 0.05f) {
            level.setBlock(pos, state.setValue(SnowPileBlock.LAYERS, layers + 1), 3);

            BlockEntity be2 = level.getBlockEntity(pos);
            if (be2 instanceof SnowPileBlockEntity pile2) {
                int[] c = pickLowCell(pile2, random);
                pile2.incrementCell(c[0], c[1], 1);
            }
        }
    }

    private static boolean shouldLayerUp(SnowPileBlockEntity pile, int layers) {
        int nearTop = 0;
        int min = Integer.MAX_VALUE;

        int g = SnowPileBlockEntity.GRID;
        for (int z = 0; z < g; z++) {
            for (int x = 0; x < g; x++) {
                int h = pile.getCellHeight(x, z);
                min = Math.min(min, h);
                if (h >= layers - 1) nearTop++;
            }
        }

        if (min <= layers - 3) return false;

        return nearTop >= NEAR_TOP_TILES_FOR_LAYER_UP;
    }

    private static int[] pickLowCell(SnowPileBlockEntity pile, RandomSource random) {
        int g = SnowPileBlockEntity.GRID;

        int min = Integer.MAX_VALUE;
        for (int z = 0; z < g; z++)
            for (int x = 0; x < g; x++)
                min = Math.min(min, pile.getCellHeight(x, z));

        int target = min + 1;
        int tries = 12;
        while (tries-- > 0) {
            int x = random.nextInt(g);
            int z = random.nextInt(g);
            if (pile.getCellHeight(x, z) <= target) return new int[]{x, z};
        }

        return new int[]{random.nextInt(g), random.nextInt(g)};
    }

    public static void seedChunkSnow(ServerLevel level, ChunkPos cp, boolean initial50) {
        long seed = level.getSeed() ^ (cp.toLong() * 0x9E3779B97F4A7C15L);
        RandomSource rnd = RandomSource.create(seed);

        int startX = cp.getMinBlockX();
        int startZ = cp.getMinBlockZ();

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                if (rnd.nextDouble() > COVERAGE) continue;

                int x = startX + dx;
                int z = startZ + dz;

                BlockPos pos = pickSnowPlacePos(level, x, z);
                if (pos == null) continue;

                // Zielhöhe: initial ~50% ±, sonst kleiner
                int targetLayers;
                if (initial50) {
                    double mean = MAX_LAYERS * 0.5;
                    double val = mean + rnd.nextGaussian() * 1.5; // +/- Streuung
                    targetLayers = Mth.clamp((int) Math.round(val), 1, MAX_LAYERS);
                } else {
                    targetLayers = 1; // oder 1..3 je nach Geschmack
                }

                // Block setzen
                level.setBlock(pos, ModBlocks.SNOW_PILE.defaultBlockState()
                        .setValue(SnowPileBlock.LAYERS, targetLayers), 3);

                BlockEntity be = level.getBlockEntity(pos);
                if (!(be instanceof SnowPileBlockEntity pile)) continue;

                // “bedacht” füllen: eher flach starten, dann smoothen
                pile.clearAll();

                int g = SnowPileBlockEntity.GRID;
                for (int i = 0; i < g * g; i++) {
                    int cx = i % g, cz = i / g;

                    // Basis: um ~50% der targetLayers, aber begrenzt (keine Säulen)
                    double h = (targetLayers * 0.5) + rnd.nextGaussian() * 0.8;
                    int cellH = Mth.clamp((int) Math.round(h), 0, targetLayers);

                    pile.setCellHeight(cx, cz, cellH);
                }

                for (int k = 0; k < RND_PASSES; k++) pile.smoothOnce();
            }
        }
    }
}
