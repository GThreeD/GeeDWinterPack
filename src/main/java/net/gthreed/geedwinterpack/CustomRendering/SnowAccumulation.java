package net.gthreed.geedwinterpack.CustomRendering;

import net.gthreed.geedwinterpack.block.ModBlocks;
import net.gthreed.geedwinterpack.block.snowpile.SnowPileBlock;
import net.gthreed.geedwinterpack.block.snowpile.SnowPileBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SnowAccumulation {
    public static void tick(ServerLevel level) {
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

                int airY = level.getHeight(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                        x, z
                );

                BlockPos placePos = new BlockPos(x, airY, z);
                BlockPos belowPos = placePos.below();

                if (!level.canSeeSky(placePos)) continue;

                maybePlaceSnowPile(level, placePos, belowPos, random);
            }
        }
    }

    private static void maybePlaceSnowPile(ServerLevel level, BlockPos pos, BlockPos belowPos, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        BlockState below = level.getBlockState(belowPos);

        if (state.is(ModBlocks.SNOW_PILE)) {
            if (isHeatNearby(level, pos, 3, 2)) { // radius 3
                melt(level, pos, state);
                return;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SnowPileBlockEntity pile) {
                int layers = state.getValue(SnowPileBlock.LAYERS);

                if (random.nextFloat() < 0.15f) {
                    int x = random.nextInt(SnowPileBlockEntity.GRID);
                    int z = random.nextInt(SnowPileBlockEntity.GRID);
                    pile.incrementCell(x, z, 1);
                }

                if (layers < 13 && pile.allFull() && random.nextFloat() < 0.05f) {
                    level.setBlock(pos, state.setValue(SnowPileBlock.LAYERS, layers + 1), 3);
                    BlockEntity be2 = level.getBlockEntity(pos);
                    if (be2 instanceof SnowPileBlockEntity pile2) {
                        pile2.fillToLayers();
                    }
                }
            }
            return;
        }

        if (state.isAir() && (below.is(ModBlocks.SNOW_PILE) || below.is(Blocks.SNOW))) {
            if (below.is(ModBlocks.SNOW_PILE)) {
                BlockEntity beBelow = level.getBlockEntity(belowPos);
                if (beBelow instanceof SnowPileBlockEntity pileBelow) {
                    int layers = below.getValue(SnowPileBlock.LAYERS);
                    if (layers < 8 && random.nextFloat() < 0.4f) {
                        BlockState newBelowState = below.setValue(SnowPileBlock.LAYERS, layers + 1);
                        level.setBlock(belowPos, newBelowState, 3);
                    }
                }
            }
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

    private static void melt(ServerLevel level, BlockPos pos, BlockState state) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SnowPileBlockEntity pile) {
            // langsam schmelzen: random tile runter
            RandomSource rand = level.random;
            int x = rand.nextInt(SnowPileBlockEntity.GRID);
            int z = rand.nextInt(SnowPileBlockEntity.GRID);
            pile.decrementCell(x, z, 1);

            if (pile.allEmpty()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

}
