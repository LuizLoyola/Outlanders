package dev.luizloyola.outlanders.util;

import dev.luizloyola.outlanders.OutlandersMod;
import net.minecraft.util.Identifier;

public class IdentifierUtils {
    public static Identifier idFor(String name) {
        return Identifier.of(OutlandersMod.MOD_ID, name);
    }
}
