package net.minestom.vanilla.blocks.behaviours.oxidisable;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.inventory.InventoryManipulation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WaxedBlockBehaviour extends OxidatedBlockBehaviour {
    private final short unWaxed;

    public WaxedBlockBehaviour(VanillaBlocks.@NotNull BlockContext context, Block unWaxed, int oxidisedLevel) {
        super(context, Block.fromStateId(context.stateId()), oxidisedLevel);
        this.unWaxed = (short) unWaxed.stateId();
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        Player.Hand hand = interaction.getHand();
        Player player = interaction.getPlayer();
        Block interactionBlock = interaction.getBlock();

        ItemStack item = player.getInventory().getItemInHand(hand);
        Material material = item.material();

        if (material.namespace().value().toLowerCase().contains("_axe")) { // TODO: Better way to check if it's an axe
            Block previousBlock = Block.fromStateId(unWaxed);
            Objects.requireNonNull(previousBlock, "Previous block with state id " + unWaxed + " was not found");
            interaction.getInstance().setBlock(interaction.getBlockPosition(), previousBlock);
            InventoryManipulation.damageItemIfNotCreative(player, hand, 1);
            return false;
        }
        return super.onInteract(interaction);
    }
}
