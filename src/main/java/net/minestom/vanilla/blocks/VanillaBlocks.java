package net.minestom.vanilla.blocks;

import fr.themode.command.Command;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.network.ConnectionManager;
import net.minestom.vanilla.commands.GamemodeCommand;
import net.minestom.vanilla.commands.HelpCommand;

import java.util.function.Supplier;

/**
 * All blocks available in the vanilla reimplementation
 */
public enum VanillaBlocks {

    JUKEBOX(JukeboxBlock::new);

    private final Supplier<CustomBlock> blockSupplier;

    private VanillaBlocks(Supplier<CustomBlock> blockSupplier) {
        this.blockSupplier = blockSupplier;
    }

    /**
     * Register this vanilla block to the given BlockManager, ConnectionManager is used to replace the basic block with its custom variant
     * @param connectionManager
     * @param blockManager
     */
    public void register(ConnectionManager connectionManager, BlockManager blockManager) {
        CustomBlock block = this.blockSupplier.get();
        connectionManager.addPlayerInitialization(player -> {
            player.addEventCallback(PlayerBlockPlaceEvent.class, event -> {
                if(event.getBlockId() == block.getBlockId()) {
                    event.setCustomBlockId(block.getCustomBlockId());
                }
            });
        });
        blockManager.registerCustomBlock(block);
    }

    /**
     * Register all vanilla commands into the given blockManager. ConnectionManager is used to replace the basic block with its custom counterpart
     * @param blockManager
     */
    public static void registerAll(ConnectionManager connectionManager, BlockManager blockManager) {
        for(VanillaBlocks vanillaBlock : values()) {
            vanillaBlock.register(connectionManager, blockManager);
        }
    }
}
