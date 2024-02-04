package net.minestom.vanilla.crafting;

import com.extollit.tuple.Pair;
import dev.goldenstack.window.InventoryView;
import dev.goldenstack.window.v1_20.Views.Smithing;
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
import org.jglrxavpok.hephaistos.nbt.NBT;

import java.util.*;
import java.util.stream.Collectors;

public record SmithingInventoryRecipes(Datapack datapack, VanillaReimplementation vri) {

    static Tag<NBT> trimTag = Tag.NBT("Trim");

    public EventNode<Event> init() {
        EventNode<Event> node = EventNode.all("vri:smithing-inventory-recipes");

        var trims = getTrims();
        trims.left.forEach(trimMaterial -> vri.process().trim().addTrimMaterial(trimMaterial));
        trims.right.forEach(trimPattern -> vri.process().trim().addTrimPattern(trimPattern));

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
                Smithing.OUTPUT.set(inv,Smithing.BASE.get(inv).withMaterial(transform.result().item()));
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
                        NBT.Compound(Map.of(
                                "material",
                                    NBT.String(trimMaterialName),
                                    "pattern",
                                    NBT.String(trimPatternName)
                        ))
                ));
            }

        });

        return node;
    }

    private @Nullable String getTrimMaterialFromIngredient(Material material) {
        TrimMaterial trimMaterial = vri.process().trim().fromIngredient(material);
        if (trimMaterial == null) {
            return null;
        }
        return trimMaterial.name();
    }
    private @Nullable String getTrimPatternFromTemplate(Material template) {
        TrimPattern trimPattern = vri.process().trim().fromTemplate(template);
        if (trimPattern == null) {
            return null;
        }
        return trimPattern.name();
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

    public @NotNull Pair<List<TrimMaterial>, List<TrimPattern>> getTrims() {
        List<TrimMaterial> trimMaterials = new ArrayList<>();
        List<TrimPattern> trimPatterns = new ArrayList<>();

        for (var entry : datapack.namespacedData().entrySet()) {
            Datapack.NamespacedData data = entry.getValue();
            for (String file : data.trim_material().files()) {
                var trimMaterial = data.trim_material().file(file);
                trimMaterials.add(TrimMaterial.create(
                        NamespaceID.from(file),
                        trimMaterial.asset_name(),
                        Objects.requireNonNull(Material.fromNamespaceId(trimMaterial.ingredient())),
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
                trimPatterns.add(TrimPattern.create(
                        NamespaceID.from(file),
                        trimPattern.asset_id(),
                        Objects.requireNonNull(Material.fromNamespaceId(trimPattern.template_item())),
                        trimPattern.description(),
                        trimPattern.decal()
                ));
            }
        }

        return Pair.of(trimMaterials,trimPatterns);
    }
}
