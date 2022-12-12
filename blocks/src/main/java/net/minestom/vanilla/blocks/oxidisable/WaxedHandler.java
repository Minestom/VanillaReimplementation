package net.minestom.vanilla.blocks.oxidisable;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.inventory.InventoryManipulation;
import org.jetbrains.annotations.NotNull;

public class WaxedHandler extends OxidatedHandler {
    private final short unWaxed;

    public WaxedHandler(VanillaBlocks.@NotNull BlockContext context, Block unWaxed, int oxidisedLevel) {
        super(context, context.vri().block(context.stateId()), oxidisedLevel);
        this.unWaxed = unWaxed.stateId();
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        Player.Hand hand = interaction.getHand();
        Player player = interaction.getPlayer();
        Block interactionBlock = interaction.getBlock();

        ItemStack item = player.getInventory().getItemInHand(hand);
        Material material = item.material();

        if (material.namespace().value().toLowerCase().contains("_axe")) { // TODO: Better way to check if it's an axe
            System.out.format("Axeing (pos=%s, old=%s, new=%s)%n", interaction.getBlockPosition(), interactionBlock.stateId(), unWaxed);
            Block previousBlock = context.vri().block(unWaxed);
            interaction.getInstance().setBlock(interaction.getBlockPosition(), previousBlock);
            InventoryManipulation.damageItemIfNotCreative(player, hand, 1);
            return false;
        }
        return super.onInteract(interaction);
    }
}
