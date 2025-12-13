package net.gthreed.geedwinterpack.mixin;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void blockRainSounds(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {

        String path = sound.getIdentifier().getPath();

        // Alle Regengeräusche blockieren
        if (path.contains("weather.rain")
                || path.contains("weather.thunder")
                || path.contains("ambient.weather")) {

            // Wir geben "NOT_PLAYED" zurück → Sound wird still blockiert
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
            cir.cancel();
        }
    }
}