package net.gthreed.geedwinterpack.CustomRendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public class SnowRenderer {
    private static Vec3 smoothedPos = null;

    public static void renderSnow(Vec3 cameraPos, WeatherRenderState state) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel world = client.level;
        if (world == null) return;

        float intensity = 0.7f;

        int particleCount = (int) (120 * intensity);

        if (smoothedPos == null) {
            smoothedPos = cameraPos;
        } else {
            double lerpFactor = 0.12;
            smoothedPos = new Vec3(
                    smoothedPos.x + (cameraPos.x - smoothedPos.x) * lerpFactor,
                    smoothedPos.y + (cameraPos.y - smoothedPos.y) * lerpFactor,
                    smoothedPos.z + (cameraPos.z - smoothedPos.z) * lerpFactor
            );
        }

        for (int i = 0; i < particleCount; i++) {
            double px = smoothedPos.x + (world.random.nextDouble() - 0.5) * 30;
            double py = smoothedPos.y + (world.random.nextDouble() - 0.5) * 30;
            double pz = smoothedPos.z + (world.random.nextDouble() - 0.5) * 30;

            BlockPos pos = BlockPos.containing(px, py + 1, pz);
            if (!world.canSeeSky(pos)) continue;

            world.addParticle(
                    ParticleTypes.SNOWFLAKE,
                    px, py, pz,
                    0.01 * (world.random.nextDouble() - 0.5),
                    -0.03,
                    0.01 * (world.random.nextDouble() - 0.5)
            );
        }
    }
}

