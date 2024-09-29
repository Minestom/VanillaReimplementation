package net.minestom.vanilla.crafting.smelting;

import dev.goldenstack.window.InventoryView;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.WindowPropertyPacket;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.PacketUtils;
import net.minestom.vanilla.crafting.CraftingUtils;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackUtils;
import net.minestom.vanilla.datapack.recipe.Recipe;
import net.minestom.vanilla.tag.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public record SmeltingHandler(Datapack datapack, int speed, Map<Material, Integer> mat2Fuel,
                              Tag<Integer> cookingTicksTag, Tag<Material> lastCookedItemTag, Tag<Integer> cookingProgressTag,
                              InventoryView.Singular inputSlot, InventoryView.Singular outputSlot, InventoryView.Singular fuelSlot
) {
    public SmeltingHandler(Datapack datapack, int speed,
                           Tag<Integer> cookingTicksTag, Tag<Material> lastCookedItemTag, Tag<Integer> cookingProgressTag,
                           InventoryView.Singular inputSlot, InventoryView.Singular outputSlot, InventoryView.Singular fuelSlot) {
        this(datapack, speed, getFuel(datapack), cookingTicksTag, lastCookedItemTag, cookingProgressTag, inputSlot, outputSlot, fuelSlot);
    }

    public @Nullable Block handle(Inventory inventory, Block block, @Nullable Material recipeResult, @Nullable Integer recipeCookingTime) {
        ItemStack input = inputSlot.get(inventory);
        ItemStack fuel = fuelSlot.get(inventory);
        ItemStack output = outputSlot.get(inventory);

        int cookingTicks = block.getTag(cookingTicksTag);
        Material lastCookedItem = block.getTag(lastCookedItemTag);
        int cookingProgress = block.getTag(cookingProgressTag);
        int fuelBurnTicks = mat2Fuel.getOrDefault(fuel.material(), -1);

        if (cookingTicks == 0) {
            if (!Material.AIR.equals(lastCookedItem)) {
                // this means that the furnace was cooking, but ran out of fuel
                WindowPropertyPacket clearProgress = new WindowPropertyPacket(inventory.getWindowId(), (short) 0, (short) 0);
                PacketUtils.sendGroupedPacket(inventory.getViewers(), clearProgress);
                return block.withTag(Tags.Blocks.Smelting.LAST_COOKED_ITEM, Material.AIR);
            }

            // furnace is not cooking, check if we can start cooking
            if (input.isAir() || fuel.isAir() || fuelBurnTicks == -1 || recipeResult == null) {
                // we've stopped cooking, but we can't start cooking again
                // reset the current progress
                WindowPropertyPacket clearProgress = new WindowPropertyPacket(inventory.getWindowId(), (short) 2, (short) 0);
                PacketUtils.sendGroupedPacket(inventory.getViewers(), clearProgress);
                return block.withTag(Tags.Blocks.Smelting.COOKING_PROGRESS, 0);
            }

            // start cooking
            cookingTicks = fuelBurnTicks;
            lastCookedItem = fuel.material();

            fuelSlot.set(inventory, fuel.withAmount(fuel.amount() - 1));
            inventory.update();
        } else {
            cookingTicks -= speed;
        }

        Integer lastCookedItemBurnTicks = mat2Fuel.get(lastCookedItem);

        if (recipeResult == null) {
            // if there was no recipe found, and the cooking progress is not 0, reset the cooking progress
            Block newBlock = block.withTag(Tags.Blocks.Smelting.COOKING_TICKS, cookingTicks);
            if (cookingProgress != 0) {
                WindowPropertyPacket clearProgress = new WindowPropertyPacket(inventory.getWindowId(), (short) 2, (short) 0);
                PacketUtils.sendGroupedPacket(inventory.getViewers(), clearProgress);
                newBlock = newBlock.withTag(Tags.Blocks.Smelting.COOKING_PROGRESS, 0);
            }
            WindowPropertyPacket maximumFuelBurnTime = new WindowPropertyPacket(inventory.getWindowId(), (short) 1, lastCookedItemBurnTicks == null ? (short) 0 : lastCookedItemBurnTicks.shortValue());
            WindowPropertyPacket fuelBurnTime = new WindowPropertyPacket(inventory.getWindowId(), (short) 0, (short) cookingTicks);
            PacketUtils.sendGroupedPacket(inventory.getViewers(), maximumFuelBurnTime);
            PacketUtils.sendGroupedPacket(inventory.getViewers(), fuelBurnTime);
            return newBlock;
        }

        if (output.material() != Material.AIR && output.material() != recipeResult) {
            // output slot is occupied by something other than the recipe result
            return null;
        }

        if (lastCookedItemBurnTicks == null) {
            throw new IllegalStateException("Last cooked id " + lastCookedItem + " has no burn time");
        }

        int recipeCompleteTicks = Objects.requireNonNullElse(recipeCookingTime, 100);

        cookingProgress += speed;

        // send furnace progress to viewers
        // 0: Fire icon (fuel left) 	counting from fuel burn time down to 0 (in-game ticks)
        // 1: Maximum fuel burn time 	fuel burn time or 0 (in-game ticks)
        // 2: Progress arrow 	counting from 0 to maximum progress (in-game ticks)
        // 3: Maximum progress 	always 200 on the notchian server
        WindowPropertyPacket maximumFuelBurnTime = new WindowPropertyPacket(inventory.getWindowId(), (short) 1, lastCookedItemBurnTicks.shortValue());
        WindowPropertyPacket fuelBurnTime = new WindowPropertyPacket(inventory.getWindowId(), (short) 0, (short) cookingTicks);
        WindowPropertyPacket maximumProgress = new WindowPropertyPacket(inventory.getWindowId(), (short) 3, (short) recipeCompleteTicks);
        WindowPropertyPacket progress = new WindowPropertyPacket(inventory.getWindowId(), (short) 2, (short) cookingProgress);

        // TODO: Bundle these packets together

        PacketUtils.sendGroupedPacket(inventory.getViewers(), maximumFuelBurnTime);
        PacketUtils.sendGroupedPacket(inventory.getViewers(), fuelBurnTime);
        PacketUtils.sendGroupedPacket(inventory.getViewers(), maximumProgress);
        PacketUtils.sendGroupedPacket(inventory.getViewers(), progress);

        if (cookingProgress >= recipeCompleteTicks) {
            // recipe complete
            cookingProgress = 0;
            if (output.isAir()) {
                outputSlot.set(inventory, ItemStack.of(recipeResult));
            } else {
                outputSlot.set(inventory, output.withAmount(output.amount() + 1));
            }
            inputSlot.set(inventory, input.withAmount(input.amount() - 1));
            inventory.update();

            // clear cooking progress
            WindowPropertyPacket clearProgress = new WindowPropertyPacket(inventory.getWindowId(), (short) 2, (short) 0);
            PacketUtils.sendGroupedPacket(inventory.getViewers(), clearProgress);
        }

        return block.withTag(Tags.Blocks.Smelting.COOKING_PROGRESS, cookingProgress)
                .withTag(Tags.Blocks.Smelting.COOKING_TICKS, cookingTicks)
                .withTag(Tags.Blocks.Smelting.LAST_COOKED_ITEM, lastCookedItem);
    }

    private static Map<Material, Integer> getFuel(Datapack datapack) {
        Map<Material, Integer> material2burnTicks = new HashMap<>();

        material2burnTicks.put(Material.LAVA_BUCKET, 20000);
        material2burnTicks.put(Material.COAL_BLOCK, 16000);
        material2burnTicks.put(Material.BLAZE_ROD, 2400);
        material2burnTicks.put(Material.COAL, 1600);
        material2burnTicks.put(Material.CHARCOAL, 1600);
        material2burnTicks.put(Material.BAMBOO_MOSAIC, 300);
        material2burnTicks.put(Material.BAMBOO_MOSAIC_STAIRS, 300);
        material2burnTicks.put(Material.BAMBOO_MOSAIC_SLAB, 150);
        material2burnTicks.put(Material.NOTE_BLOCK, 300);
        material2burnTicks.put(Material.BOOKSHELF, 300);
        material2burnTicks.put(Material.CHISELED_BOOKSHELF, 300);
        material2burnTicks.put(Material.LECTERN, 300);
        material2burnTicks.put(Material.JUKEBOX, 300);
        material2burnTicks.put(Material.CHEST, 300);
        material2burnTicks.put(Material.TRAPPED_CHEST, 300);
        material2burnTicks.put(Material.CRAFTING_TABLE, 300);
        material2burnTicks.put(Material.DAYLIGHT_DETECTOR, 300);
        material2burnTicks.put(Material.BOW, 300);
        material2burnTicks.put(Material.FISHING_ROD, 300);
        material2burnTicks.put(Material.LADDER, 300);
        material2burnTicks.put(Material.WOODEN_SHOVEL, 200);
        material2burnTicks.put(Material.WOODEN_SWORD, 200);
        material2burnTicks.put(Material.WOODEN_HOE, 200);
        material2burnTicks.put(Material.WOODEN_AXE, 200);
        material2burnTicks.put(Material.WOODEN_PICKAXE, 200);
        material2burnTicks.put(Material.STICK, 100);
        material2burnTicks.put(Material.BOWL, 100);
        material2burnTicks.put(Material.DRIED_KELP_BLOCK, 4001);
        material2burnTicks.put(Material.CROSSBOW, 300);
        material2burnTicks.put(Material.BAMBOO, 50);
        material2burnTicks.put(Material.DEAD_BUSH, 100);
        material2burnTicks.put(Material.SCAFFOLDING, 50);
        material2burnTicks.put(Material.LOOM, 300);
        material2burnTicks.put(Material.BARREL, 300);
        material2burnTicks.put(Material.CARTOGRAPHY_TABLE, 300);
        material2burnTicks.put(Material.FLETCHING_TABLE, 300);
        material2burnTicks.put(Material.SMITHING_TABLE, 300);
        material2burnTicks.put(Material.COMPOSTER, 300);
        material2burnTicks.put(Material.AZALEA, 100);
        material2burnTicks.put(Material.FLOWERING_AZALEA, 100);
        material2burnTicks.put(Material.MANGROVE_ROOTS, 300);

        // tags
        addItemTags(datapack, material2burnTicks, "logs", 300);
        addItemTags(datapack, material2burnTicks, "bamboo_blocks", 300);
        addItemTags(datapack, material2burnTicks, "planks", 300);
        addItemTags(datapack, material2burnTicks, "wooden_stairs", 300);
        addItemTags(datapack, material2burnTicks, "wooden_slabs", 150);
        addItemTags(datapack, material2burnTicks, "wooden_trapdoors", 300);
        addItemTags(datapack, material2burnTicks, "wooden_pressure_plates", 300);
        addItemTags(datapack, material2burnTicks, "wooden_fences", 300);
        addItemTags(datapack, material2burnTicks, "fence_gates", 300);
        addItemTags(datapack, material2burnTicks, "banners", 300);
        addItemTags(datapack, material2burnTicks, "signs", 200);
        addItemTags(datapack, material2burnTicks, "hanging_signs", 800);
        addItemTags(datapack, material2burnTicks, "wooden_doors", 200);
        addItemTags(datapack, material2burnTicks, "boats", 1200);
        addItemTags(datapack, material2burnTicks, "wool", 100);
        addItemTags(datapack, material2burnTicks, "wooden_buttons", 100);
        addItemTags(datapack, material2burnTicks, "saplings", 100);
        addItemTags(datapack, material2burnTicks, "wool_carpets", 67);

        return Map.copyOf(material2burnTicks);
    }

    private static void addItemTags(Datapack datapack, Map<Material, Integer> material2burnTicks, String tagName, int burnTime) {
        for (Key item : DatapackUtils.findTags(datapack, "item", NamespaceID.from(tagName))) {
            Material mat = Material.fromNamespaceId(item.asString());
            material2burnTicks.put(mat, burnTime);
        }
    }

    public @Nullable <T extends Recipe.CookingRecipe> T findRecipe(Class<T> clazz, Material inputMat) {
        CraftingUtils utils = new CraftingUtils(datapack);
        for (var entry : datapack.namespacedData().entrySet()) {
            var data = entry.getValue();

            for (String file : data.recipes().files()) {
                Recipe recipe = data.recipes().file(file);
                if (clazz.isInstance(recipe)) {
                    T cooking = clazz.cast(recipe);
                    Set<Material> inputMats = cooking.ingredient().stream()
                            .map(utils::ingredientToMaterials)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toUnmodifiableSet());

                    if (!inputMats.contains(inputMat)) continue;

                    return cooking;
                }
            }
        }
        return null;
    }
}
