package dev.luizloyola.outlanders.init;

import dev.luizloyola.outlanders.client.renderer.OutlanderEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.client.render.entity.PlayerEntityRenderer;

public class OutlandersClientRegistry {
    public static void init() {
        EntityRendererFactories.register(OutlandersRegistry.OUTLANDER, ctx -> new OutlanderEntityRenderer(ctx));
    }
}
