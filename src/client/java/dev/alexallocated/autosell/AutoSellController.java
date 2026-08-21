package dev.alexallocated.autosell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

final class AutoSellController {
	private final AutoSellStateMachine stateMachine = new AutoSellStateMachine();
	private final AutoSellSettings settings;
	private final Deque<Integer> transferQueue = new ArrayDeque<>();
	private int occupiedSlotsAtStart;

	AutoSellController(AutoSellSettings settings) {
		this.settings = settings;
	}

	void toggle(Minecraft client) {
		boolean enabled = stateMachine.toggle();
		transferQueue.clear();
		if (client.player != null) {
			client.player.sendSystemMessage(Component.translatable(
					enabled ? "message.autosell.enabled" : "message.autosell.disabled"
			));
		}
	}

	void reset() {
		stateMachine.reset();
		transferQueue.clear();
		occupiedSlotsAtStart = 0;
	}

	void tick(Minecraft client) {
		LocalPlayer player = client.player;
		boolean inWorld = player != null && client.level != null;
		boolean matchingScreen = inWorld && isMatchingSellScreen(client);
		boolean inventoryFull = inWorld && isMainInventoryFull(player);

		AutoSellStateMachine.Input input = new AutoSellStateMachine.Input(
				inWorld,
				inWorld && client.gui.screen() == null,
				inventoryFull,
				matchingScreen,
				!transferQueue.isEmpty()
		);

		AutoSellStateMachine.Action action = stateMachine.tick(input, settings);
		if (!inWorld) {
			transferQueue.clear();
			return;
		}

		switch (action) {
			case NONE -> {
			}
			case SEND_SELL_COMMAND -> sendSellCommand(client);
			case BEGIN_TRANSFER -> beginTransfer(player);
			case MOVE_NEXT_SLOT -> moveNextSlot(client, player);
			case CLOSE_SCREEN -> closeSellScreen(client, player);
			case SCREEN_TIMEOUT -> notify(player, "message.autosell.timeout");
			case TRANSFER_INTERRUPTED -> {
				transferQueue.clear();
				notify(player, "message.autosell.interrupted");
			}
		}
	}

	private boolean isMatchingSellScreen(Minecraft client) {
		return client.gui.screen() instanceof ContainerScreen screen
				&& settings.windowTitle().equals(screen.getTitle().getString());
	}

	private boolean isMainInventoryFull(LocalPlayer player) {
		List<Boolean> occupied = player.getInventory().getNonEquipmentItems().stream()
				.map(stack -> !stack.isEmpty())
				.toList();
		return InventoryOccupancy.isFull(occupied);
	}

	private int countOccupiedMainSlots(LocalPlayer player) {
		return (int) player.getInventory().getNonEquipmentItems().stream()
				.filter(stack -> !stack.isEmpty())
				.count();
	}

	private void sendSellCommand(Minecraft client) {
		if (client.getConnection() != null) {
			client.getConnection().sendCommand("sell");
		}
	}

	private void beginTransfer(LocalPlayer player) {
		transferQueue.clear();
		occupiedSlotsAtStart = countOccupiedMainSlots(player);

		AbstractContainerMenu menu = player.containerMenu;
		List<TransferSlotSelector.SlotView> slots = new ArrayList<>();
		for (int menuSlot = 0; menuSlot < menu.slots.size(); menuSlot++) {
			Slot slot = menu.slots.get(menuSlot);
			slots.add(new TransferSlotSelector.SlotView(
					menuSlot,
					slot.container == player.getInventory(),
					slot.getContainerSlot(),
					slot.hasItem()
			));
		}

		transferQueue.addAll(TransferSlotSelector.selectMainInventorySlots(slots));
	}

	private boolean isMainInventorySlot(Slot slot, LocalPlayer player) {
		int inventorySlot = slot.getContainerSlot();
		return slot.container == player.getInventory()
				&& inventorySlot >= 0
				&& inventorySlot < InventoryOccupancy.MAIN_INVENTORY_SIZE;
	}

	private void moveNextSlot(Minecraft client, LocalPlayer player) {
		Integer menuSlot = transferQueue.pollFirst();
		if (menuSlot == null || client.gameMode == null) {
			return;
		}

		AbstractContainerMenu menu = player.containerMenu;
		if (menuSlot >= menu.slots.size()) {
			return;
		}

		Slot slot = menu.slots.get(menuSlot);
		if (isMainInventorySlot(slot, player) && slot.hasItem()) {
			client.gameMode.handleContainerInput(
					menu.containerId,
					menuSlot,
					0,
					ContainerInput.QUICK_MOVE,
					player
			);
		}
	}

	private void closeSellScreen(Minecraft client, LocalPlayer player) {
		transferQueue.clear();
		int occupiedNow = countOccupiedMainSlots(player);
		if (occupiedNow >= occupiedSlotsAtStart) {
			notify(player, "message.autosell.no_progress");
		}

		if (client.gui.screen() instanceof ContainerScreen screen
				&& settings.windowTitle().equals(screen.getTitle().getString())) {
			player.closeContainer();
		}
	}

	private void notify(LocalPlayer player, String translationKey) {
		player.sendSystemMessage(Component.translatable(translationKey));
	}
}
