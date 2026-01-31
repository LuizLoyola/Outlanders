package dev.luizloyola.outlanders;

import dev.luizloyola.outlanders.client.renderer.OutlandersEntityRendererFactories;
import dev.luizloyola.outlanders.entity.ClientPersonEntity;
import net.fabricmc.api.ClientModInitializer;

public class OutlandersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		OutlandersEntityRendererFactories.init();

		ClientPersonEntity.setFactory();
	}
}