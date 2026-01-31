package dev.luizloyola.outlanders;

import dev.luizloyola.outlanders.init.OutlandersClientRegistry;
import net.fabricmc.api.ClientModInitializer;

public class OutlandersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		OutlandersClientRegistry.init();
	}
}