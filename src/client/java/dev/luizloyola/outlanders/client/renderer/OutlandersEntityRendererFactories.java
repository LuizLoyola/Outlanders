package dev.luizloyola.outlanders.client.renderer;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.player.PlayerSkinType;

import java.util.Map;

public class OutlandersEntityRendererFactories {
    public static void init() {
//        EntityRendererFactories.register(OutlandersEntities.PERSON, context -> {
//
//        });
    }

    public static Map<PlayerSkinType, PersonEntityRenderer> reloadPersonRenderers(EntityRendererFactory.Context ctx) {
        try {
            return Map.of(
                    PlayerSkinType.WIDE, new PersonEntityRenderer(ctx, false),
                    PlayerSkinType.SLIM, new PersonEntityRenderer(ctx, true)
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to create person models", ex);
        }
    }
}
