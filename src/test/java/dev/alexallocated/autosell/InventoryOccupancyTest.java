package dev.alexallocated.autosell;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryOccupancyTest {
	@Test
	void allThirtySixOccupiedSlotsAreFull() {
		assertTrue(InventoryOccupancy.isFull(
				Collections.nCopies(InventoryOccupancy.MAIN_INVENTORY_SIZE, true)
		));
	}

	@Test
	void oneEmptySlotIsNotFull() {
		List<Boolean> slots = new ArrayList<>(
				Collections.nCopies(InventoryOccupancy.MAIN_INVENTORY_SIZE, true)
		);
		slots.set(InventoryOccupancy.MAIN_INVENTORY_SIZE - 1, false);

		assertFalse(InventoryOccupancy.isFull(slots));
	}

	@Test
	void incompleteSlotDataIsNeverTreatedAsFull() {
		assertFalse(InventoryOccupancy.isFull(Collections.nCopies(35, true)));
	}
}
