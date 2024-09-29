package net.minestom.vanilla.crafting;

import dev.goldenstack.window.InventoryView;
import dev.goldenstack.window.Views.Smithing;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.recipe.Recipe;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public record SmithingInventoryRecipes(Datapack datapack, VanillaReimplementation vri) {

    static Tag<BinaryTag> trimTag = Tag.NBT("Trim");

    public EventNode<Event> init() {
        EventNode<Event> node = EventNode.all("vri:smithing-inventory-recipes");

        Trims trims = getTrims();
        trims.materials().forEach(vri.process().trimMaterial()::register);
        trims.patterns().forEach(vri.process().trimPattern()::register);

        // TODO: shift-click mass crafting and take out.

        CraftingUtils.addOutputSlotEventHandler(node, Smithing.OUTPUT, InventoryType.SMITHING);

        node.addListener(InventoryClickEvent.class, event -> {
            int slot = event.getSlot();
            Inventory inv = event.getPlayer().getOpenInventory();
            if (inv == null) return;
            if (event.getPlayer().getOpenInventory().getInventoryType() != InventoryType.SMITHING) return;
            if (Smithing.OUTPUT.isValidExternal(slot) && !event.getClickedItem().isAir()) {
                InventoryView.Singular input;
                for (int i = 0; i < 3; i++) {
                    input = Smithing.VIEW.fork(i);
                    input.set(inv, input.get(inv).withAmount(prev -> prev - 1));
                }
            }

            Recipe.Smithing recipe = getRecipe(
                    Smithing.TEMPLATE.get(inv).material(),
                    Smithing.BASE.get(inv).material(),
                    Smithing.ADDITION.get(inv).material());

            if (recipe == null) {
                Smithing.OUTPUT.set(inv, ItemStack.AIR);
                return;
            }

            if (recipe instanceof Recipe.SmithingTransform transform) {
                Smithing.OUTPUT.set(inv,Smithing.BASE.get(inv).withMaterial(transform.result().id()));
            }
            else if (recipe instanceof Recipe.SmithingTrim trim) {
                String trimPatternName = getTrimPatternFromTemplate(Smithing.TEMPLATE.get(inv).material());
                String trimMaterialName = getTrimMaterialFromIngredient(Smithing.ADDITION.get(inv).material());
                if (trimPatternName == null || trimMaterialName == null) {
                    Logger.warn("Can't find the trim data for %s while a recipe was found! why is this happening?".formatted(
                            Smithing.TEMPLATE.get(inv).material().name()
                    ));
                    return;
                }
                Smithing.OUTPUT.set(inv, Smithing.BASE.get(inv).withTag(trimTag,
                        CompoundBinaryTag.from(Map.of(
                                "material",
                                    StringBinaryTag.stringBinaryTag(trimMaterialName),
                                    "pattern",
                                    StringBinaryTag.stringBinaryTag(trimPatternName)
                        ))
                ));
            }

        });

        return node;
    }

    private @Nullable String getTrimMaterialFromIngredient(Material material) {
        TrimMaterial trimMaterial = vri.process().trimMaterial().get(material.namespace());
        if (trimMaterial == null) {
            return null;
        }
        return trimMaterial.assetName();
    }
    private @Nullable String getTrimPatternFromTemplate(Material template) {
        TrimPattern trimPattern = vri.process().trimPattern().get(template.namespace());
        if (trimPattern == null) {
            return null;
        }
        return trimPattern.assetId().asString();
    }

    private @Nullable Recipe.Smithing getRecipe(Material template, Material base, Material addition) {
        CraftingUtils utils = new CraftingUtils(datapack);

        for (var entry : datapack.namespacedData().entrySet()) {
            Datapack.NamespacedData data = entry.getValue();


            for (String file : data.recipes().files()) {
                Recipe recipe = data.recipes().file(file);


                if (recipe instanceof Recipe.Smithing smithing) {
                    if (
                            utils.ingredientToMaterials(smithing.template()).contains(template) &&
                            utils.ingredientToMaterials(smithing.base()).contains(base) &&
                            utils.ingredientToMaterials(smithing.addition()).contains(addition)
                    ) {
                        return smithing;
                    }
                }
            }
        }

        return null;
    }

    public record Trims(Map<NamespaceID, TrimMaterial> materials, Map<NamespaceID, TrimPattern> patterns) {
    }

    public @NotNull Trims getTrims() {
        Map<NamespaceID, TrimMaterial> trimMaterials = new HashMap<>();
        Map<NamespaceID, TrimPattern> trimPatterns = new HashMap<>();

        for (var entry : datapack.namespacedData().entrySet()) {
            Datapack.NamespacedData data = entry.getValue();
            for (String file : data.trim_material().files()) {
                var trimMaterial = data.trim_material().file(file);
                trimMaterials.put(NamespaceID.from(file), TrimMaterial.create(
                        trimMaterial.asset_name(),
                        Objects.requireNonNull(Material.fromNamespaceId(trimMaterial.ingredient().asString())),
                        trimMaterial.item_model_index(),
                        Objects.requireNonNullElse(trimMaterial.override_armor_materials(), new HashMap<NamespaceID, String>())
                                .entrySet().stream().collect(Collectors.toMap(
                                        mapEntry -> mapEntry.getKey().asString(), Map.Entry::getValue
                                )),
                        trimMaterial.description()
                ));
            }

            for (String file : data.trim_pattern().files()) {
                var trimPattern = data.trim_pattern().file(file);
                trimPatterns.put(NamespaceID.from(file), TrimPattern.create(
                        NamespaceID.from(trimPattern.asset_id()),
                        Objects.requireNonNull(Material.fromNamespaceId(trimPattern.template_item().asString())),
                        trimPattern.description(),
                        trimPattern.decal()
                ));
            }
        }

        return new Trims(trimMaterials, trimPatterns);
    }
}
