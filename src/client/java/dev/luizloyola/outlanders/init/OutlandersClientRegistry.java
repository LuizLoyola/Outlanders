package dev.luizloyola.outlanders.init;

import dev.luizloyola.outlanders.client.renderer.PersonEntityRenderer;
import dev.luizloyola.outlanders.registry.OutlandersEntities;
import net.minecraft.client.render.entity.EntityRendererFactories;

public class OutlandersClientRegistry {
    public static void init() {
        EntityRendererFactories.register(OutlandersEntities.PERSON, PersonEntityRenderer::new);
    }
}
