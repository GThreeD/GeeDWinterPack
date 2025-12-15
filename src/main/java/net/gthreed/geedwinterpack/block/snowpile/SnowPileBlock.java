package net.gthreed.geedwinterpack.block.snowpile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SnowPileBlock extends Block implements EntityBlock {
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 13);
    private static final VoxelShape[] SHAPES = new VoxelShape[14];


    static {
        // Flacher als vorher: ~1 Pixel pro Layer
        for (int i = 1; i <= 13; i++) {
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
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
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
        return RenderShape.INVISIBLE;
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

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SnowPileBlockEntity pile) {
            int grid = SnowPileBlockEntity.GRID;
            double cell = 16.0 / grid;

            VoxelShape shape = Shapes.empty();
            double inset = 0.0; // optional später >0 um "reinrutschen" zu erleichtern

            for (int z = 0; z < grid; z++) {
                for (int x = 0; x < grid; x++) {
                    int h = pile.getCellHeight(x, z); // 0..layers (Pixel)
                    if (h <= 0) continue;

                    double x0 = x * cell + inset;
                    double z0 = z * cell + inset;
                    double x1 = (x + 1) * cell - inset;
                    double z1 = (z + 1) * cell - inset;

                    shape = Shapes.or(shape, Block.box(x0, 0, z0, x1, h, z1));
                }
            }
            return shape;
        }
        return super.getCollisionShape(state, level, pos, ctx);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SnowPileBlockEntity pile) {
            int grid = SnowPileBlockEntity.GRID;
            double lx = hit.getLocation().x - pos.getX();
            double lz = hit.getLocation().z - pos.getZ();
            int cx = Mth.clamp((int) (lx * grid), 0, grid - 1);
            int cz = Mth.clamp((int) (lz * grid), 0, grid - 1);

            pile.incrementCell(cx, cz, 1);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }


}

