package name.betterbeacons.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import name.betterbeacons.screen.BeaconTrinketScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.List;

public class BeaconTrinketItem extends Item implements Trinket {

    public BeaconTrinketItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            user.openHandledScreen(new ExtendedScreenHandlerFactory<ItemStack>() {
                @Override
                public ItemStack getScreenOpeningData(ServerPlayerEntity player) {
                    return stack; // This is the payload sent to the client constructor
                }

                @Override
                public Text getDisplayName() {
                    return Text.literal("Beacon Trinket");
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInv, PlayerEntity player) {
                    return new BeaconTrinketScreenHandler(syncId, playerInv, stack);
                }
            });
        }
        return TypedActionResult.success(stack);
    }

    // 2. Update the helper methods
    public static DefaultedList<ItemStack> getInventory(ItemStack stack, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        DefaultedList<ItemStack> items = DefaultedList.ofSize(12, ItemStack.EMPTY);

        net.minecraft.component.type.NbtComponent nbtComponent = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);

        if (nbtComponent != null) {
            net.minecraft.nbt.NbtCompound nbt = nbtComponent.copyNbt();
            if (nbt.contains("Items", 9)) {
                // Note the third argument: registries
                net.minecraft.inventory.Inventories.readNbt(nbt, items, registries);
            }
        }
        return items;
    }

    public static void saveInventory(ItemStack stack, DefaultedList<ItemStack> items, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        net.minecraft.nbt.NbtCompound nbt = new net.minecraft.nbt.NbtCompound();
        // Added registries as the third argument
        net.minecraft.inventory.Inventories.writeNbt(nbt, items, registries);

        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.of(nbt));
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!entity.getWorld().isClient()) {
            // Effect logic will go here
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.betterbeacons.beacon_trinket").formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }
}