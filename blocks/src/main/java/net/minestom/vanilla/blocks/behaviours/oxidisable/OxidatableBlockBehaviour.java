package net.minestom.vanilla.blocks.behaviours.oxidisable;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.inventory.InventoryManipulation;
import net.minestom.vanilla.randomticksystem.RandomTickable;
import net.minestom.vanilla.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Oxidation (<a href="https://minecraft.fandom.com/wiki/Block_of_Copper#Waxing">Source</a>)
 * <p>
 *     Non-waxed copper blocks have four stages of oxidation (including the initial normal state). Lightning bolts and
 *     axes can remove the oxidation on copper blocks. As the block begins to oxidize (exposed copper), it gets
 *     discolored and green spots begin to appear. As the oxidation continues (weathered copper), the block is a green
 *     color with brown spots. In the last stage (oxidized copper), the block is teal with several green spots.
 *     Oxidation of copper blocks relies only on random ticks. Rain or water does not accelerate oxidation, and covering
 *     copper blocks with other blocks does not prevent oxidation. In Java Edition, groups of non-waxed copper blocks
 *     oxidize far more slowly than single copper blocks that are spaced at least 4 blocks apart. This is because a
 *     block in a group being less oxidized than the others slows down the oxidation process for all other blocks within
 *     4 blocks of taxicab distance. However, if one wishes to increase the oxidation speed, placing oxidized copper
 *     blocks around less oxidized copper blocks does not offer a speed improvement over simply placing the blocks 4
 *     apart. The calculations for the oxidation behavior are as follows:
 *     In Java Edition, when a random tick is given, a copper block has a 64/1125 chance to enter a state called
 *     pre-oxidation. This means a copper block enters pre-oxidation after approximately 20 minutes.
 *     In pre-oxidation, the copper block searches its nearby non-waxed copper blocks for a distance of 4 blocks taxicab
 *     distance. If there is any copper block that has a lower oxidation level, then the pre-oxidation ends, meaning
 *     that this copper block does not weather. Let a be the number of all nearby non-waxed copper blocks, and b be the
 *     number of nearby non-waxed copper blocks that have a higher oxidation level. We derive the value of c from this
 *     equation: c = b + 1/a + 1. We also let the modifying factor m be 0.75 if the copper block has no oxidation level,
 *     or 1 if the copper block is exposed or weathered.[1] Then the oxidation probability is mc2. For example, an
 *     unweathered copper block surrounded by 6 unweathered copper blocks and 6 exposed copper blocks has a 21.7% chance
 *     to oxidize if it enters the pre-oxidation state. In this case, a = 12, b = 6, and m = 0.75.[2] The most efficient
 *     way of laying out the copper blocks for oxidation is in a 7×7×6 face-centered cubic (fcc)/cubic close-packed
 *     (ccp) lattice.
 * <p/>
 */
public class OxidatableBlockBehaviour extends WaxableBlockBehaviour implements RandomTickable, OxygenSensitive {

    private final short previous;
    private final short oxidised;
    private final int oxidisedLevel;


    public OxidatableBlockBehaviour(VanillaBlocks.@NotNull BlockContext context, Block previous, Block oxidised, Block waxed, int oxidisedLevel) {
        super(context, waxed);
        this.previous = (short) previous.stateId();
        this.oxidised = (short) oxidised.stateId();
        this.oxidisedLevel = oxidisedLevel;
    }

    @Override
    public void randomTick(@NotNull RandomTick randomTick) {
        // Exit now if the block cannot be oxidised anymore
        if (oxidised == context.stateId()) return;

        Random random = context.vri().random(randomTick.instance());
        // In Java Edition, when a random tick is given, a copper block has a 64/1125 chance to enter a state called pre-oxidation.
        // This means a copper block enters pre-oxidation after approximately 20 minutes.
        if (random.nextInt(1125) >= 64) {
            return;
        }

        // In pre-oxidation, the copper block searches its nearby non-waxed copper blocks for a distance of 4 blocks
        // taxicab distance. If there is any copper block that has a lower oxidation level, then the pre-oxidation ends,
        // meaning that this copper block does not weather.
        List<Block> nearbyBlocks = MathUtils.getWithinManhattanDistance(randomTick.position(), 4)
                .stream()
                .map(point -> randomTick.instance().isChunkLoaded(point) ?
                        randomTick.instance().getBlock(point) : Block.AIR)
                .toList();
        int minOxidisedAround = nearbyBlocks.stream()
                .filter(block -> block.handler() instanceof OxygenSensitive)
                .map(block -> (OxygenSensitive) block.handler())
                .mapToInt(OxygenSensitive::oxidisedLevel)
                .min()
                .orElse(Integer.MAX_VALUE);

        if (minOxidisedAround < oxidisedLevel) {
            return;
        }

        // Let a be the number of all nearby non-waxed copper blocks, and b be the number of nearby non-waxed copper
        // blocks that have a higher oxidation level. We derive the value of c from this equation: c = b + 1/a + 1.
        // We also let the modifying factor m be 0.75 if the copper block has no oxidation level, or 1 if the copper
        // block is exposed or weathered.[1] Then the oxidation probability is mc2.
        // For example, an unweathered copper block surrounded by 6 unweathered copper blocks and 6 exposed copper
        // blocks has a 21.7% chance to oxidize if it enters the pre-oxidation state. In this case, a = 12, b = 6, and
        // m = 0.75.[2]
        double a = (int) nearbyBlocks.stream()
                .filter(block -> block.handler() instanceof OxygenSensitive os
                        && os.oxidisedLevel() == oxidisedLevel()) // Filter out unrelated blocks
                .count();
        double b = (int) nearbyBlocks.stream()
                .filter(block -> !(block.handler() instanceof WaxedBlockBehaviour))
                .filter(block -> block.handler() instanceof OxygenSensitive os
                        && os.oxidisedLevel() > oxidisedLevel()) // Filter out unrelated blocks
                .map(block -> (OxygenSensitive) block.handler()).filter(Objects::nonNull)
                .filter(handler -> handler.oxidisedLevel() > oxidisedLevel)
                .count();
        double m = oxidisedLevel == 0 ? 0.75 : 1;
        double c = (b + 1) / (a + 1);
        double probability = m * c * c;

        if (random.nextDouble() < probability) {
            Block block = Block.fromStateId(oxidised);
            Objects.requireNonNull(block, "Block with state id " + oxidised + " was not found");
            randomTick.instance().setBlock(randomTick.position(), block);
        }
    }

    @Override
    public int oxidisedLevel() {
        return oxidisedLevel;
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        Player.Hand hand = interaction.getHand();
        Player player = interaction.getPlayer();
        Block interactionBlock = interaction.getBlock();

        ItemStack item = player.getInventory().getItemInHand(hand);
        Material material = item.material();

        if (interactionBlock.stateId() != previous
                && material.namespace().value().toLowerCase().contains("_axe")) { // TODO: Better way to check if it's an axe
            Block previousBlock = Block.fromStateId(previous);
            Objects.requireNonNull(previousBlock, "Block with state id " + previous + " was not found");
            interaction.getInstance().setBlock(interaction.getBlockPosition(), previousBlock);
            InventoryManipulation.damageItemIfNotCreative(player, hand, 1);
            return false;
        }
        return super.onInteract(interaction);
    }
}
