package net.minestom.vanilla.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentResourceLocation;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.Registry;
import net.minestom.vanilla.entities.EntityRegistry;
import org.jetbrains.annotations.NotNull;

public class SummonCommand extends Command {
    private final ArgumentResourceLocation typeArg = ArgumentType.ResourceLocation("type");

    public SummonCommand() {
        super("summon");

        addSyntax(this::execute, typeArg);
    }

    public void execute(@NotNull CommandSender sender, @NotNull CommandContext context) {
        NamespaceID entityType = NamespaceID.from(context.get(typeArg));

        Entity entity = Registry.get(EntityRegistry.class, entityType);


    }
}
