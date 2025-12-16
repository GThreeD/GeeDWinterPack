package net.gthreed.geedwinterpack.CustomRendering;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.List;
import java.util.stream.LongStream;

public class SnowSeedData extends SavedData {
    private static final String KEY = "geedwinterpack_snowseed";
    private static final Codec<SnowSeedData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.LONG.listOf().optionalFieldOf("done", List.of())
                    .forGetter(d -> LongStream.of(d.done.toLongArray()).boxed().toList())
    ).apply(inst, SnowSeedData::fromList));
    public static final SavedDataType<SnowSeedData> TYPE =
            new SavedDataType<>(KEY, SnowSeedData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    private final LongOpenHashSet done = new LongOpenHashSet();

    public SnowSeedData() {
    }

    private static SnowSeedData fromList(List<Long> list) {
        SnowSeedData d = new SnowSeedData();
        for (long v : list) d.done.add(v);
        return d;
    }

    public static SnowSeedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE); // so wie in 1.21.x gedacht :contentReference[oaicite:1]{index=1}
    }

    public boolean isDone(ChunkPos pos) {
        return done.contains(pos.toLong());
    }

    public void markDone(ChunkPos pos) {
        done.add(pos.toLong());
        setDirty(); // ✅ sonst wird nicht gespeichert
    }
}

