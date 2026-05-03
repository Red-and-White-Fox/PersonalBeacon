package name.betterbeacons.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    // We use ExtendedScreenHandlerType and provide the ItemStack codec
    public static final ScreenHandlerType<BeaconTrinketScreenHandler> BEACON_TRINKET_SCREEN_HANDLER =
            new ExtendedScreenHandlerType<>(BeaconTrinketScreenHandler::new, ItemStack.PACKET_CODEC);

    public static void register() {
        Registry.register(Registries.SCREEN_HANDLER,
                Identifier.of("betterbeacons", "beacon_trinket"),
                BEACON_TRINKET_SCREEN_HANDLER);
    }
}