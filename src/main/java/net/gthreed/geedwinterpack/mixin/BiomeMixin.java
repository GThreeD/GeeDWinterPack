package net.gthreed.geedwinterpack.mixin;

import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class BiomeMixin {

    @Inject(method = "getTemperature", at = @At("HEAD"), cancellable = true)
    public void alwaysCold(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.0F); // unter 0.15 = Schnee
    }
}
