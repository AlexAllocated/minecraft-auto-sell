# Auto Sell

Auto Sell is a client-only Fabric mod for Minecraft Java 26.2. When enabled, it waits until all 36 main inventory and hotbar slots are occupied, opens an approved server sell menu with `/sell`, shift-clicks the inventory into that menu, and closes it to complete the sale.

It was built for a server whose operators explicitly permit this automation. Check the rules before using it elsewhere.

## Install

1. Install Minecraft Java 26.2, [Fabric Loader 0.19.3 or newer](https://fabricmc.net/use/installer/), and a [Fabric API build for Minecraft 26.2](https://modrinth.com/mod/fabric-api).
2. Copy `autosell-1.0.0.jar` into the Minecraft `mods` directory.
3. Join the server and press **F6** to enable Auto Sell. Press F6 again to disable it.

The toggle appears under **Options > Controls > Key Binds > Auto Sell**, where it can be rebound. Auto Sell always starts disabled and resets to disabled after disconnecting.

## Behavior and safeguards

- Only the 36 main inventory/hotbar slots are monitored and moved. Armor and offhand are untouched.
- A sale starts when none of those 36 slots are empty. Partially filled stacks still count as occupied.
- The mod only deposits into a standard chest-style screen whose plain title is exactly `Items verkaufen`.
- It never clicks any sell-menu slot, including the green confirmation button.
- Items are shift-clicked one at a time with a short delay. The screen is closed through Minecraft's normal container-close action.
- A full-inventory event produces at most one `/sell` attempt. The mod rearms only after observing an empty inventory slot, preventing command spam if items cannot be sold.
- If the screen closes or changes unexpectedly, the mod stops the transfer and leaves the new screen alone.

Try the first run with disposable items while watching the client. The server controls the final transaction behavior.

## Configuration

The mod creates `config/autosell.json` after the game starts:

```json
{
  "windowTitle": "Items verkaufen",
  "screenTimeoutTicks": 100,
  "clickDelayTicks": 3,
  "closeDelayTicks": 10
}
```

These are client game ticks, normally 20 per second. The defaults therefore wait up to 5 seconds for the menu, pause about 150 ms between clicks, and pause about 500 ms before closing. They are not redstone ticks. Invalid or non-positive timing values are replaced with safe defaults.

## Build

JDK 25 is required. The included Gradle wrapper downloads the rest of the toolchain:

```sh
./gradlew build
```

The installable JAR is written to `build/libs/autosell-1.0.0.jar`.

## License

[MIT](LICENSE)
