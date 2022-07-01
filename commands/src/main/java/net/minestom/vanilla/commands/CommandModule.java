package net.minestom.vanilla.commands;

import com.google.auto.service.AutoService;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.Module;
import net.minestom.vanilla.ModuleRegistry;
import net.minestom.vanilla.Registry;

import java.util.function.Supplier;

@AutoService(Module.class)
public class CommandModule implements Module {
    private static final NamespaceID ID = NamespaceID.from("vri:commands");

    @Override
    public NamespaceID id() {
        return ID;
    }

    @Override
    public void hook() {
        registerCommand(DifficultyCommand::new);
        registerCommand("vri:commands", SummonCommand::new);
    }

    private void registerCommand(Supplier<Command> ctor) {
        MinecraftServer.getCommandManager().register(ctor.get());
    }

    private void registerCommand(String module, Supplier<Command> ctor) {
        if (Registry.isPresent(ModuleRegistry.class, NamespaceID.from(module))) {
            MinecraftServer.getCommandManager().register(ctor.get());
        }
    }
}
