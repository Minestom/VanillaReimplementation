package net.minestom.vanilla.blocks.oxidisable;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.vanilla.blocks.VanillaBlockHandler;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.inventory.InventoryManipulation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class WaxableBlockHandler extends VanillaBlockHandler {
    protected final short waxedBlock;
    protected WaxableBlockHandler(VanillaBlocks.@NotNull BlockContext context, Block waxedTarget) {
        super(context);
        this.waxedBlock = waxedTarget.stateId();
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        Player.Hand hand = interaction.getHand();
        Player player = interaction.getPlayer();

        ItemStack item = player.getInventory().getItemInHand(hand);
        Material material = item.material();

        if (Material.HONEYCOMB.equals(material)) {
            Block block = context.vri().block(waxedBlock);
            interaction.getInstance().setBlock(interaction.getBlockPosition(), block);
            InventoryManipulation.consumeItemIfNotCreative(player, hand, 1);
            return false;
        }
        return super.onInteract(interaction);
    }
}
