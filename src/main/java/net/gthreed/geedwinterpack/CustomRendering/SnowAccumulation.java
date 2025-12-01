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
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SnowPileBlockEntity pile) {
                int layers = state.getValue(SnowPileBlock.LAYERS);
                if (layers < 8 && random.nextFloat() < 0.3f) {
                    state = state.setValue(SnowPileBlock.LAYERS, layers + 1);
                    level.setBlock(pos, state, 3);
                }

                if (random.nextFloat() < 0.15f) {
                    pile.fillAll();
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
                    if (random.nextFloat() < 0.1f) {
                        pileBelow.fillAll();
                    }
                }
            }
            return;
        }

        if (state.isAir()) {
            if (random.nextFloat() < 0.25f) {
                level.setBlock(pos, ModBlocks.SNOW_PILE.defaultBlockState(), 3);
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof SnowPileBlockEntity pile) {
                    pile.fillAll();
                }
            }
        }
    }
}
