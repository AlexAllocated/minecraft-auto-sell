package dev.alexallocated.autosell;

import java.util.List;

public final class InventoryOccupancy {
	public static final int MAIN_INVENTORY_SIZE = 36;

	private InventoryOccupancy() {
	}

	public static boolean isFull(List<Boolean> occupiedSlots) {
		return occupiedSlots.size() == MAIN_INVENTORY_SIZE
				&& occupiedSlots.stream().allMatch(Boolean.TRUE::equals);
	}
}
