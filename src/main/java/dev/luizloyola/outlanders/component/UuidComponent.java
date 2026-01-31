package dev.luizloyola.outlanders.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record UuidComponent(long mostSigBits, long leastSigBits) {
    public static final Codec<UuidComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.LONG.fieldOf("most").forGetter(UuidComponent::mostSigBits),
            Codec.LONG.fieldOf("least").forGetter(UuidComponent::leastSigBits)
    ).apply(builder, UuidComponent::new));

    public static UuidComponent empty() {
        return new UuidComponent(0L, 0L);
    }

    public @Nullable UUID asUuid() {
        if (this.isEmpty()) {
            return null;
        }

        return new UUID(this.mostSigBits, this.leastSigBits);
    }

    public static UuidComponent fromUuid(@Nullable UUID uuid) {
        if (uuid == null) {
            return empty();
        }

        return new UuidComponent(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    public boolean isEmpty() {
        return this.mostSigBits == 0 && this.leastSigBits == 0;
    }
}
