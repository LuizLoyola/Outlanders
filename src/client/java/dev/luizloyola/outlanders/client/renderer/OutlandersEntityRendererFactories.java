package dev.luizloyola.outlanders.client.renderer;

import dev.luizloyola.outlanders.registry.OutlandersEntities;
import net.minecraft.client.render.entity.EntityRendererFactories;

public class OutlandersEntityRendererFactories {
    public static void init() {
        EntityRendererFactories.register(OutlandersEntities.PERSON, PersonEntityRenderer::new);
    }
}
