package net.gthreed.geedwinterpack.mixin;

import net.gthreed.geedwinterpack.CustomRendering.SnowRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void replaceRainWithSnow(
            MultiBufferSource bufferSource,
            Vec3 cameraPos,
            WeatherRenderState state,
            CallbackInfo ci
    ) {
        SnowRenderer.renderSnow(cameraPos, state);

        // Vanilla-Wetter blockieren
        ci.cancel();
    }
}
