package net.gthreed.geedwinterpack.mixin;

import net.gthreed.geedwinterpack.CustomRendering.SnowTracks;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void geedwinterpack$onTick(CallbackInfo ci) {
        SnowTracks.onPlayerTick((ServerPlayer)(Object)this);
    }
}
