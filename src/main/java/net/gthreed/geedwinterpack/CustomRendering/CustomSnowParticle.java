package net.gthreed.geedwinterpack.CustomRendering;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class CustomSnowParticle extends SingleQuadParticle {

    public CustomSnowParticle(
            ClientLevel level,
            double x, double y, double z,
            double vx, double vy, double vz,
            TextureAtlasSprite sprite,
            RandomSource random
    ) {
        super(level, x, y, z, sprite);

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.gravity = 0.014f;
        this.friction = 0.995f;

        this.quadSize = 0.08f;

        this.lifetime = 120 + random.nextInt(40);

        // keine Rotation!
        this.roll = 0;
    }

    @Override
    public void tick() {
        super.tick();

        this.xd += (random.nextDouble() - 0.5) * 0.0003;
        this.zd += (random.nextDouble() - 0.5) * 0.0003;

        if (this.age > this.lifetime) {
            this.remove();
        }

        if (this.onGround) {
            this.age = this.lifetime - 5;
        } else {
            int x = Mth.floor(this.x);
            int z = Mth.floor(this.z);
            int terrainY = this.level.getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    x, z
            );
            if (this.y < terrainY - 0.1) {
                this.remove();
            }
        }
    }


    @Override
    protected @NotNull Layer getLayer() {
        return Layer.TRANSLUCENT;
    }
}
