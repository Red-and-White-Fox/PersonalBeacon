package name.betterbeacons;

import dev.emi.trinkets.api.TrinketsApi;
import name.betterbeacons.item.BeaconMedaillon;
import name.betterbeacons.item.BeaconTrinketItem;
import name.betterbeacons.component.ModDataComponents;

import name.betterbeacons.item.DragonLeather;
import name.betterbeacons.screen.BeaconTrinketScreenHandler;
import name.betterbeacons.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Betterbeacons implements ModInitializer {
	public static final String MOD_ID = "betterbeacons";

	// 1. ADD THIS FIELD (Ensure it is public and static)
	public static final ScreenHandlerType<BeaconTrinketScreenHandler> BEACON_TRINKET_SCREEN_HANDLER =
			Registry.register(
					Registries.SCREEN_HANDLER,
					Identifier.of(MOD_ID, "beacon_trinket"),
					new ExtendedScreenHandlerType<>(BeaconTrinketScreenHandler::new, ItemStack.PACKET_CODEC)
			);

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Create the item instance
	public static final BeaconTrinketItem BEACON_TRINKET = new BeaconTrinketItem(new Item.Settings().maxCount(1));
	public static final DragonLeather DRAGON_LEATHER = new DragonLeather(new Item.Settings().rarity(Rarity.EPIC));
	public static final BeaconMedaillon BEACON_MEDAILLON = new BeaconMedaillon(new Item.Settings().maxCount(1));

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		// 1. Register Data Components first
		ModDataComponents.register();
		ModScreenHandlers.register();

		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "dragon_leather"), DRAGON_LEATHER);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "beacon_medaillon"), BEACON_MEDAILLON);
		// 2. Register the Item
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "beacon_trinket"), BEACON_TRINKET);

		// Inside onInitialize()
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			// Check if the current loot table is the Ender Dragon's
			if (EntityType.ENDER_DRAGON.getLootTableId().equals(key)) {

				// Create a new pool for your drop
				LootPool.Builder poolBuilder = LootPool.builder()
						.rolls(ConstantLootNumberProvider.create(1))
						.conditionally(RandomChanceLootCondition.builder(0.99f)) // 30% chance
						.with(ItemEntry.builder(DRAGON_LEATHER));

				tableBuilder.pool(poolBuilder);
			}
		});



		PayloadTypeRegistry.playC2S().register(OpenBeaconTrinketPayload.ID, OpenBeaconTrinketPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(OpenBeaconTrinketPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();

				TrinketsApi.getTrinketComponent(player).ifPresent(trinkets -> {
					var equipped = trinkets.getEquipped(stack -> stack.getItem() instanceof BeaconTrinketItem);

					if (!equipped.isEmpty()) {
						ItemStack stack = equipped.get(0).getRight();

						// --- THIS IS THE PART YOU MODIFY ---
						player.openHandledScreen(new ExtendedScreenHandlerFactory<ItemStack>() {
							@Override
							public ItemStack getScreenOpeningData(ServerPlayerEntity player) {
								// This sends the ItemStack to the client-side ScreenHandler constructor
								return stack;
							}

							@Override
							public Text getDisplayName() {
								return Text.literal("Beacon Necklace");
							}

							@Override
							public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity p) {
								return new BeaconTrinketScreenHandler(syncId, inv, stack);
							}
						});
						// ------------------------------------
					}
				});
			});
		});
	}
}