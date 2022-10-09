package net.minestom.vanilla.crafting;

public interface Recipe {

    Type type();
    String group();

    enum Type {
        SHAPELESS,
        SHAPED,
        SMELTING,
        BLASTING,
        CAMPFIRE_COOKING,
        SMOKING,
        STONECUTTING,
        SMITHING
    }
}
