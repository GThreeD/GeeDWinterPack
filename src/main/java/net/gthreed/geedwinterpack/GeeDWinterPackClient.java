package net.gthreed.geedwinterpack;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.gthreed.geedwinterpack.block.ModBlockEntities;
import net.gthreed.geedwinterpack.block.snowpile.SnowPileBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeeDWinterPackClient implements ClientModInitializer {

    public static final String MOD_ID = "geedwinterpack";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static SimpleParticleType CUSTOM_SNOW;

    @Override
    public void onInitializeClient() {

        LOGGER.info("Hello Fabric world!");

        /*CUSTOM_SNOW = Registry.register(
                Registries.PARTICLE_TYPE,
                ResourceLocation.fromNamespaceAndPath("geedwinterpack", "custom_snow"),
                new SimpleParticleType(true, SimpleParticleType.Factory.INSTANCE)
        );*/

        ParticleFactoryRegistry.getInstance().register(
                ParticleTypes.SNOWFLAKE,
                CustomSnowParticleProvider::new
        );

        BlockEntityRenderers.register(
                ModBlockEntities.SNOW_PILE,
                SnowPileBlockEntityRenderer::new
        );

//        ClientTickEvents.END_CLIENT_TICK.register(client -> {
//            LocalPlayer player = client.player;
//            if (player != null) {
//                SnowTracksClient.onPlayerTick(player);
//            }
//        });

    }
}