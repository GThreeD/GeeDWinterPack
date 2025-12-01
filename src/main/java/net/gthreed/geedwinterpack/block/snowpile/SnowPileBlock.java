package net.gthreed.geedwinterpack.block.snowpile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SnowPileBlock extends Block implements EntityBlock {
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 8);
    private static final VoxelShape[] SHAPES = new VoxelShape[9];

    static {
        // Flacher als vorher: ~1 Pixel pro Layer
        for (int i = 1; i <= 8; i++) {
            double h = (i) / 16.0; // 1 Pixel je Layer
            SHAPES[i] = Block.box(0.0, 0.0, 0.0, 16.0, h * 16.0, 16.0);
        }
    }

    public SnowPileBlock(ResourceKey<Block> blockResourceKey) {
        super(BlockBehaviour.Properties.ofFullCopy(Blocks.SNOW)
                .strength(0.1F)
                .noOcclusion()
                .isViewBlocking((s, w, p) -> false)
                .isSuffocating((s, w, p) -> false)
                .setId(blockResourceKey));
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 4));
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(LAYERS)];
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SnowPileBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);

        return below.isFaceSturdy(level, belowPos, Direction.UP)
                || below.is(Blocks.SNOW)
                || below.is(this);
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction == Direction.DOWN && !blockState.canSurvive(levelReader, blockPos)) {
            levelReader.isEmptyBlock(blockPos);
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
        super.createBlockStateDefinition(builder);
    }
}

