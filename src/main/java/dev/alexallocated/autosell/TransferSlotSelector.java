package dev.alexallocated.autosell;

import java.util.List;

public final class TransferSlotSelector {
	public record SlotView(
			int menuSlot,
			boolean playerInventory,
			int inventorySlot,
			boolean occupied
	) {
	}

	private TransferSlotSelector() {
	}

	public static List<Integer> selectMainInventorySlots(List<SlotView> slots) {
		return slots.stream()
				.filter(SlotView::playerInventory)
				.filter(SlotView::occupied)
				.filter(slot -> slot.inventorySlot() >= 0)
				.filter(slot -> slot.inventorySlot() < InventoryOccupancy.MAIN_INVENTORY_SIZE)
				.map(SlotView::menuSlot)
				.toList();
	}
}
