package dev.alexallocated.autosell;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class AutoSellClient implements ClientModInitializer {
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath("autosell", "main")
	);

	private KeyMapping toggleKey;
	private AutoSellController controller;

	@Override
	public void onInitializeClient() {
		controller = new AutoSellController(AutoSellConfigStore.load());
		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.autosell.toggle",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_F6,
				CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) {
				controller.toggle(client);
			}
			controller.tick(client);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> controller.reset());
	}
}
