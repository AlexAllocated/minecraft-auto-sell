package dev.alexallocated.autosell;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TransferSlotSelectorTest {
	@Test
	void selectsOnlyOccupiedMainInventorySlots() {
		List<TransferSlotSelector.SlotView> slots = new ArrayList<>();

		for (int menuSlot = 0; menuSlot < 36; menuSlot++) {
			boolean greenConfirmationButton = menuSlot == 35;
			slots.add(new TransferSlotSelector.SlotView(
					menuSlot,
					false,
					menuSlot,
					greenConfirmationButton
			));
		}

		for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
			slots.add(new TransferSlotSelector.SlotView(
					36 + inventorySlot,
					true,
					inventorySlot,
					inventorySlot != 10
			));
		}

		slots.add(new TransferSlotSelector.SlotView(72, true, 40, true));

		List<Integer> selected = TransferSlotSelector.selectMainInventorySlots(slots);

		assertEquals(35, selected.size());
		assertFalse(selected.contains(35), "the container's confirmation button must not be clicked");
		assertFalse(selected.contains(46), "empty player slots must not be clicked");
		assertFalse(selected.contains(72), "equipment and offhand slots must not be clicked");
	}
}
