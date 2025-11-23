package net.gthreed.geedwinterpack.mixin;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "advanceWeatherCycle", at = @At("HEAD"), cancellable = true)
    private void alwaysSnow(CallbackInfo ci) {
        ServerLevel level = (ServerLevel)(Object)this;

        // regen=true sorgt für Schnee in kalten Biomen
        // thunder=false damit keine Gewitter entstehen
        level.setWeatherParameters(
                0,      // clear weather time
                6000,   // rain duration
                true,   // raining
                false   // thundering
        );

        ci.cancel(); // Vanilla-Wetter-Tick komplett blockieren
    }
}
