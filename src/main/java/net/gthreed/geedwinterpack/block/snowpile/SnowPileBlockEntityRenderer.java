package net.gthreed.geedwinterpack.block.snowpile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SnowPileBlockEntityRenderer
        implements BlockEntityRenderer<SnowPileBlockEntity, SnowPileRenderState> {

    public SnowPileBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public @NotNull SnowPileRenderState createRenderState() {
        return new SnowPileRenderState();
    }

    @Override
    public void extractRenderState(
            SnowPileBlockEntity be,
            SnowPileRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);

        int[] src = be.getCells();
        System.arraycopy(src, 0, state.cells, 0, state.cells.length);

        if (be.getLevel() != null) {
            BlockPos pos = be.getBlockPos();
            state.lightCoords = LevelRenderer.getLightColor(be.getLevel(), pos);
        } else {
            state.lightCoords = 0;
        }
    }

    @Override
    public void submit(
            SnowPileRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        // Wenn komplett leer -> gar nix rendern
        boolean anySnow = false;
        for (int v : state.cells) {
            if (v > 0) {
                anySnow = true;
                break;
            }
        }
        if (!anySnow) {
            return;
        }

        int grid = SnowPileBlockEntity.GRID;
        float cellSize = 1.0f / (float) grid;

        float baseHeight = 0.6f;

        matrices.pushPose();

        for (int z = 0; z < grid; z++) {
            for (int x = 0; x < grid; x++) {
                int idx = z * grid + x;
                int val = state.cells[idx];
                if (val <= 0) continue;

                float minX = x * cellSize;
                float minZ = z * cellSize;

                float height = baseHeight * val;

                matrices.pushPose();

                matrices.translate(minX, 0.001f, minZ);

                matrices.scale(cellSize, height, cellSize);

                queue.submitBlock(
                        matrices,
                        Blocks.SNOW.defaultBlockState(),
                        state.lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        0
                );

                matrices.popPose();
            }
        }

        matrices.popPose();
    }
}
