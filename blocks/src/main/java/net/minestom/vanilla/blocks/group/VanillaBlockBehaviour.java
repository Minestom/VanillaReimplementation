package net.minestom.vanilla.blocks.group;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.InventoryType;
import net.minestom.vanilla.blocks.behavior.*;
import net.minestom.vanilla.blocks.group.behaviour.BehaviourGroup;
import net.minestom.vanilla.blocks.group.block.BlockGroup;

import java.util.function.Function;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class VanillaBlockBehaviour extends VanillaRuleset<BehaviourGroup, Function<Block, BlockHandler>> {

    public static final VanillaBlockBehaviour INSTANCE = new VanillaBlockBehaviour();

    public static final BehaviourGroup CRAFTING_TABLE = INSTANCE.group(
        INSTANCE.byBlock(Block.CRAFTING_TABLE),
        new GenericWorkStationRule(
            Block.CRAFTING_TABLE,
            InventoryType.CRAFTING,
            "container.crafting"
        )
    );

    public static final BehaviourGroup ANVIL = INSTANCE.group(
        INSTANCE.byBlock(Block.ANVIL),
        new GenericWorkStationRule(
            Block.ANVIL,
            InventoryType.ANVIL,
            "container.repair"
        )
    );

    public static final BehaviourGroup BREWING_STAND = INSTANCE.group(
        INSTANCE.byBlock(Block.BREWING_STAND),
        new GenericWorkStationRule(
            Block.BREWING_STAND,
            InventoryType.BREWING_STAND,
            "container.brewing"
        )
    );

    public static final BehaviourGroup LOOM = INSTANCE.group(
        INSTANCE.byBlock(Block.LOOM),
        new GenericWorkStationRule(
            Block.LOOM,
            InventoryType.LOOM,
            "container.loom"
        )
    );

    public static final BehaviourGroup GRINDSTONE = INSTANCE.group(
        INSTANCE.byBlock(Block.GRINDSTONE),
        new GenericWorkStationRule(
            Block.GRINDSTONE,
            InventoryType.GRINDSTONE,
            "container.grindstone"
        )
    );

    public static final BehaviourGroup SMITHING_TABLE = INSTANCE.group(
        INSTANCE.byBlock(Block.SMITHING_TABLE),
        new GenericWorkStationRule(
            Block.SMITHING_TABLE,
            InventoryType.SMITHING,
            "container.upgrade"
        )
    );

    public static final BehaviourGroup CARTOGRAPHY_TABLE = INSTANCE.group(
        INSTANCE.byBlock(Block.CARTOGRAPHY_TABLE),
        new GenericWorkStationRule(
            Block.CARTOGRAPHY_TABLE,
            InventoryType.CARTOGRAPHY,
            "container.cartography_table"
        )
    );

    public static final BehaviourGroup STONECUTTER = INSTANCE.group(
        INSTANCE.byBlock(Block.STONECUTTER),
        new GenericWorkStationRule(
            Block.STONECUTTER,
            InventoryType.STONE_CUTTER,
            "container.stonecutter"
        )
    );

    public static final BehaviourGroup ENCHANTING_TABLE = INSTANCE.group(
        INSTANCE.byBlock(Block.ENCHANTING_TABLE),
        new GenericWorkStationRule(
            Block.ENCHANTING_TABLE,
            InventoryType.ENCHANTMENT,
            "container.enchant"
        )
    );

    public static final BehaviourGroup TRAPDOOR = INSTANCE.group(
        INSTANCE.byTag("minecraft:wooden_trapdoors"),
        WoodenTrapDoorOpenRule::new
    );

    public static final BehaviourGroup FENCE_GATE = INSTANCE.group(
        INSTANCE.byTag("minecraft:fence_gates"),
        GateOpenRule::new
    );

    public static final BehaviourGroup COPPER = INSTANCE.group(
        INSTANCE.byList(CopperOxidationRule.oxidationStages.keySet()),
        CopperOxidationRule::new
    );

    public static final BehaviourGroup WOODEN_DOORS = INSTANCE.group(
        INSTANCE.byExclusion(
            INSTANCE.byTag("minecraft:doors"),
            INSTANCE.byBlock(Block.IRON_DOOR)
        ),
        DoorOpenRule::new
    );

    public static final BehaviourGroup SIGNS = INSTANCE.group(
        INSTANCE.byTag("minecraft:all_signs"),
        SignEditRule::new
    );

    public static final BehaviourGroup CAKE = INSTANCE.group(
        INSTANCE.byBlock(Block.CAKE),
        CakeEatRule::new
    );

    public static final BehaviourGroup CANDLE_CAKE = INSTANCE.group(
        INSTANCE.byTag("minecraft:candle_cakes"),
        CandleCakeRule::new
    );

    public static final BehaviourGroup STRIPPABLE_WOOD = INSTANCE.group(
        INSTANCE.byList(StrippingBehaviorRule.getStrippableBlocks()),
        StrippingBehaviorRule::new
    );

    @Override
    protected BehaviourGroup createGroup(
        BlockGroup blockGroup,
        Function<Block, BlockHandler> valueFunction
    ) {
        return new BehaviourGroup(blockGroup, valueFunction);
    }

    public BehaviourGroup group(BlockGroup blockGroup, BlockHandler handler) {
        BehaviourGroup group = new BehaviourGroup(blockGroup, block -> handler);
        ALL.add(group);
        return group;
    }
}
