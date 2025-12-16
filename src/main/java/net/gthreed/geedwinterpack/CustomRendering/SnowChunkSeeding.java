package net.gthreed.geedwinterpack.CustomRendering;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public final class SnowChunkSeeding {
    private static final LongArrayFIFOQueue QUEUE = new LongArrayFIFOQueue();

    private SnowChunkSeeding() {
    }

    public static void init() {

        // Bei jedem Chunk-Load (Spawnchunks + Spieler-Nähe + Chunkloader)
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (!(world instanceof ServerLevel level)) return;

            ChunkPos cp = chunk.getPos();
            SnowSeedData data = SnowSeedData.get(level);

            if (!data.isDone(cp)) {
                QUEUE.enqueue(cp.toLong());
                data.markDone(cp);
            }
        });

        // Pro Tick 1 Chunk abarbeiten -> kein Lag-Spike
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerLevel level = server.overworld(); // oder iteriere alle levels wenn du willst
            if (QUEUE.isEmpty()) return;

            ChunkPos cp = new ChunkPos(QUEUE.dequeueLong());

            // Hier kommt dein “initial ~50% +-” rein:
            SnowAccumulation.seedChunkSnow(level, cp, true);
        });
    }
}
