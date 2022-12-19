package net.minestom.vanilla.blocks.oxidisable;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.vanilla.blocks.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.inventory.InventoryManipulation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class WaxableBlockBehaviour extends VanillaBlockBehaviour {
    protected final short waxedBlock;
    protected WaxableBlockBehaviour(VanillaBlocks.@NotNull BlockContext context, Block waxedTarget) {
        super(context);
        this.waxedBlock = waxedTarget.stateId();
    }

    @Override
    public boolean onInteract(@NotNull VanillaInteraction interaction) {
        Player.Hand hand = interaction.hand();
        Player player = interaction.player();

        ItemStack item = player.getInventory().getItemInHand(hand);
        Material material = item.material();

        if (Material.HONEYCOMB.equals(material)) {
            Block block = Block.fromStateId(waxedBlock);
            Objects.requireNonNull(block, "Waxed block with state id " + waxedBlock + " does not exist");
            interaction.instance().setBlock(interaction.blockPosition(), block);
            InventoryManipulation.consumeItemIfNotCreative(player, hand, 1);
            return false;
        }
        return super.onInteract(interaction);
    }
}
