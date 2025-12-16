package net.gthreed.geedwinterpack;

import net.gthreed.geedwinterpack.CustomRendering.CustomSnowParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class CustomSnowParticleProvider implements ParticleProvider<SimpleParticleType> {

    private final SpriteSet sprites;

    public CustomSnowParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Override
    public Particle createParticle(
            SimpleParticleType type,
            ClientLevel level,
            double x, double y, double z,
            double vx, double vy, double vz,
            RandomSource random
    ) {
        return new CustomSnowParticle(level, x, y, z, vx, vy, vz, sprites.get(random), random);
    }
}