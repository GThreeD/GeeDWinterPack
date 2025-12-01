package net.gthreed.geedwinterpack.mixin;

import net.gthreed.geedwinterpack.CustomRendering.SnowAccumulation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "advanceWeatherCycle", at = @At("HEAD"), cancellable = true)
    private void alwaysSnow(CallbackInfo ci) {
        ServerLevel level = (ServerLevel)(Object)this;

        level.setWeatherParameters(
                6000,      // clear weather time
                0,   // rain duration
                false,   // raining
                false   // thundering
        );

        ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void geedwinterpack$tickSnow(CallbackInfo ci) {
        ServerLevel level = (ServerLevel)(Object)this;
        SnowAccumulation.tick(level);
    }
}



