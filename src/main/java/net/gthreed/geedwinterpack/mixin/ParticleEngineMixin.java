package net.gthreed.geedwinterpack.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private <T extends ParticleOptions> void blockRainParticles(
            T particleData,
            double x, double y, double z,
            double vx, double vy, double vz,
            CallbackInfoReturnable<Particle> cir
    ) {
        ParticleType<?> type = particleData.getType();

        // Alle Regen/Boden-Splash Partikel blockieren
        if (type == ParticleTypes.RAIN) {

            cir.setReturnValue(null); // Partikel verhindern
            cir.cancel();
        }
    }
}