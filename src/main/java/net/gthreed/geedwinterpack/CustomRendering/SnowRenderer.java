package net.gthreed.geedwinterpack.CustomRendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public class SnowRenderer {

    public static void renderSnow(Vec3 cameraPos, WeatherRenderState state) {

        Minecraft client = Minecraft.getInstance();
        ClientLevel world = client.level;

        if (world == null) return;

        // Wetterstärke aus Vanilla übernehmen
        float intensity = state.intensity * 0.1f;

        if (intensity <= 0.01f)
            return;

        int particleCount = (int) (1500 * intensity);

        for (int i = 0; i < particleCount; i++) {

            double px = cameraPos.x + (world.random.nextDouble() - 0.5) * 40;
            double py = cameraPos.y + (world.random.nextDouble() - 0.5) * 40;
            double pz = cameraPos.z + (world.random.nextDouble() - 0.5) * 40;

            world.addParticle(
                    ParticleTypes.SNOWFLAKE,
                    px, py, pz,
                    0, -0.04, 0
            );
        }
    }
}
