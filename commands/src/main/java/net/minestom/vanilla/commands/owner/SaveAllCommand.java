package net.minestom.vanilla.commands.owner;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.vanilla.commands.VanillaCommand;
import net.minestom.vanilla.logging.Logger;

/**
 * Save the server
 */
public class SaveAllCommand extends VanillaCommand {
    public SaveAllCommand() {
        super("save-all");
        setCondition(permission(LEVEL_OWNER));
        setDefaultExecutor(this::defaultor);

        //Todo add flush argument thiny
    }

    @Override
    public Component usage(CommandSender sender, CommandContext context) {
        return Component.text("/save-all [flush]");
    }

    @Override
    public void defaultor(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.translatable("commands.save.saving"));
        MinecraftServer.getInstanceManager().getInstances().forEach(i -> {
            i.saveChunksToStorage();
            Logger.info("Saved dimension " + i.getDimensionType().name());
        });
        sender.sendMessage(Component.translatable("commands.save.success"));
    }
}
