package dev.alexallocated.autosell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.alexallocated.autosell.AutoSellStateMachine.Action.BEGIN_TRANSFER;
import static dev.alexallocated.autosell.AutoSellStateMachine.Action.CLOSE_SCREEN;
import static dev.alexallocated.autosell.AutoSellStateMachine.Action.MOVE_NEXT_SLOT;
import static dev.alexallocated.autosell.AutoSellStateMachine.Action.NONE;
import static dev.alexallocated.autosell.AutoSellStateMachine.Action.SCREEN_TIMEOUT;
import static dev.alexallocated.autosell.AutoSellStateMachine.Action.SEND_SELL_COMMAND;
import static dev.alexallocated.autosell.AutoSellStateMachine.Action.TRANSFER_INTERRUPTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoSellStateMachineTest {
	private static final AutoSellSettings SETTINGS = AutoSellSettings.defaults();
	private AutoSellStateMachine machine;

	@BeforeEach
	void setUp() {
		machine = new AutoSellStateMachine();
	}

	@Test
	void enablingWhileFullStartsOneSaleAttempt() {
		assertTrue(machine.toggle());
		assertEquals(SEND_SELL_COMMAND, machine.tick(input(true, true, true, false, false), SETTINGS));
		assertEquals(AutoSellStateMachine.Phase.WAITING_FOR_SCREEN, machine.phase());
	}

	@Test
	void waitsForNormalGameplayBeforeSendingCommand() {
		machine.toggle();

		assertEquals(NONE, machine.tick(input(true, false, true, false, false), SETTINGS));
		assertEquals(SEND_SELL_COMMAND, machine.tick(input(true, true, true, false, false), SETTINGS));
	}

	@Test
	void matchingScreenStartsTransferAndSpacesClicks() {
		startWaitingForScreen();
		assertEquals(BEGIN_TRANSFER, machine.tick(input(true, false, true, true, false), SETTINGS));
		assertEquals(MOVE_NEXT_SLOT, machine.tick(input(true, false, true, true, true), SETTINGS));
		assertEquals(NONE, machine.tick(input(true, false, true, true, true), SETTINGS));
		assertEquals(NONE, machine.tick(input(true, false, true, true, true), SETTINGS));
		assertEquals(MOVE_NEXT_SLOT, machine.tick(input(true, false, true, true, true), SETTINGS));
	}

	@Test
	void waitsConfiguredDelayBeforeClosing() {
		AutoSellSettings shortDelay = new AutoSellSettings("Items verkaufen", 100, 3, 2);
		startWaitingForScreen();
		machine.tick(input(true, false, true, true, false), shortDelay);

		assertEquals(NONE, machine.tick(input(true, false, true, true, false), shortDelay));
		assertEquals(NONE, machine.tick(input(true, false, true, true, false), shortDelay));
		assertEquals(CLOSE_SCREEN, machine.tick(input(true, false, true, true, false), shortDelay));
	}

	@Test
	void successfulTransferRearmsEvenWhenFarmRefillsInventoryImmediately() {
		AutoSellSettings shortDelay = new AutoSellSettings("Items verkaufen", 100, 3, 1);
		startWaitingForScreen();
		machine.tick(input(true, false, true, true, false), shortDelay);
		machine.tick(input(true, false, true, true, false), shortDelay);

		assertEquals(CLOSE_SCREEN, machine.tick(input(true, false, true, true, false, true), shortDelay));
		assertEquals(SEND_SELL_COMMAND, machine.tick(input(true, true, true, false, false), shortDelay));
	}

	@Test
	void unsuccessfulTransferStillRequiresAnEmptySlotBeforeRetrying() {
		AutoSellSettings shortDelay = new AutoSellSettings("Items verkaufen", 100, 3, 1);
		startWaitingForScreen();
		machine.tick(input(true, false, true, true, false), shortDelay);
		machine.tick(input(true, false, true, true, false), shortDelay);

		assertEquals(CLOSE_SCREEN, machine.tick(input(true, false, true, true, false, false), shortDelay));
		assertEquals(NONE, machine.tick(input(true, true, true, false, false), shortDelay));

		machine.tick(input(true, true, false, false, false), shortDelay);
		assertEquals(SEND_SELL_COMMAND, machine.tick(input(true, true, true, false, false), shortDelay));
	}

	@Test
	void timesOutAtConfiguredLimitAndDoesNotSpamRetry() {
		AutoSellSettings shortTimeout = new AutoSellSettings("Items verkaufen", 2, 3, 10);
		machine.toggle();
		assertEquals(SEND_SELL_COMMAND, machine.tick(input(true, true, true, false, false), shortTimeout));
		assertEquals(NONE, machine.tick(input(true, true, true, false, false), shortTimeout));
		assertEquals(SCREEN_TIMEOUT, machine.tick(input(true, true, true, false, false), shortTimeout));
		assertEquals(NONE, machine.tick(input(true, true, true, false, false), shortTimeout));

		machine.tick(input(true, true, false, false, false), shortTimeout);
		assertEquals(SEND_SELL_COMMAND, machine.tick(input(true, true, true, false, false), shortTimeout));
	}

	@Test
	void changedScreenInterruptsTransferWithoutClosingIt() {
		startWaitingForScreen();
		machine.tick(input(true, false, true, true, false), SETTINGS);

		assertEquals(TRANSFER_INTERRUPTED, machine.tick(input(true, false, true, false, true), SETTINGS));
		assertEquals(AutoSellStateMachine.Phase.MONITORING, machine.phase());
	}

	@Test
	void disconnectResetsToDisabled() {
		machine.toggle();
		machine.tick(input(false, false, false, false, false), SETTINGS);

		assertFalse(machine.isEnabled());
		assertEquals(AutoSellStateMachine.Phase.DISABLED, machine.phase());
	}

	@Test
	void togglingOffDuringTransferDoesNotRequestAClosingAction() {
		startWaitingForScreen();
		machine.tick(input(true, false, true, true, false), SETTINGS);

		assertFalse(machine.toggle());
		assertEquals(NONE, machine.tick(input(true, false, true, true, false), SETTINGS));
	}

	private void startWaitingForScreen() {
		machine.toggle();
		machine.tick(input(true, true, true, false, false), SETTINGS);
	}

	private AutoSellStateMachine.Input input(
			boolean inWorld,
			boolean normalGameplay,
			boolean inventoryFull,
			boolean matchingScreen,
			boolean hasQueuedSlot
	) {
		return input(
				inWorld,
				normalGameplay,
				inventoryFull,
				matchingScreen,
				hasQueuedSlot,
				false
		);
	}

	private AutoSellStateMachine.Input input(
			boolean inWorld,
			boolean normalGameplay,
			boolean inventoryFull,
			boolean matchingScreen,
			boolean hasQueuedSlot,
			boolean transferSucceeded
	) {
		return new AutoSellStateMachine.Input(
				inWorld,
				normalGameplay,
				inventoryFull,
				matchingScreen,
				hasQueuedSlot,
				transferSucceeded
		);
	}
}
