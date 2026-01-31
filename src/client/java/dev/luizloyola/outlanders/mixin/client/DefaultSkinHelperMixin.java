package dev.luizloyola.outlanders.mixin.client;

import dev.luizloyola.outlanders.mixinInterfaces.DefaultSkinHelperExt;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.SkinTextures;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DefaultSkinHelper.class)
public class DefaultSkinHelperMixin implements DefaultSkinHelperExt {
    @Shadow
    @Final
    private static SkinTextures[] SKINS;

    @Unique
    public SkinTextures outlanders$getSkinByName(@Nullable String name) {
        if (name == null) {
            return SKINS[6];
        }

        switch (name) {
            case "alex" -> {
                return SKINS[0];
            }
            case "ari" -> {
                return SKINS[1];
            }
            case "efe" -> {
                return SKINS[2];
            }
            case "kai" -> {
                return SKINS[12];
            }
            case "makena" -> {
                return SKINS[4];
            }
            case "noor" -> {
                return SKINS[14];
            }
            case "steve" -> {
                return SKINS[15];
            }
            case "sunny" -> {
                return SKINS[16];
            }
            case "zuri" -> {
                return SKINS[17];
            }
            default -> {
                // default to slim steve
                return SKINS[6];
            }
        }
    }
}



