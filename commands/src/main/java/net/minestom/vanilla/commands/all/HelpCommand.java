package net.minestom.vanilla.commands.all;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.vanilla.commands.VanillaCommand;
import net.minestom.vanilla.commands.VanillaCommandsFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
// Not explicitly used but good to keep if needed
import java.util.List;
// For Objects.requireNonNullElse for better null handling if needed
// Good practice to include if using streams


/**
 * Returns the list of all available commands
 */
public class HelpCommand extends VanillaCommand {
    public HelpCommand() {
        super("help");

        setCondition(permission(LEVEL_ALL));

        setDefaultExecutor(this::defaultor);

        ArgumentWord commandArg = ArgumentType.Word("command");

        addSyntax((sender, context) -> {
            String commandName = context.get(commandArg);

            // Attempt to retrieve the command instance
            VanillaCommand targetCommand = VanillaCommandsFeature.getRegisteredCommands().get(commandName);

            if (targetCommand == null) {
                sender.sendMessage(Component.translatable("commands.help.failed", NamedTextColor.RED));
                return;
            }

            viewUsage(sender, context, targetCommand);

        }, commandArg); // Associate this executor with the 'commandArg'
    }

    /**
     * Displays the usage for a specific command.
     * @param sender The command sender.
     * @param context The command context.
     * @param command The VanillaCommand instance whose usage is to be displayed. Can be null if not found.
     */
    private void viewUsage(@NotNull CommandSender sender, CommandContext context, @Nullable VanillaCommand command) {
        if (command == null) {
            return;
        }

        Component usage = command.usage(sender, context);

        if (usage == null) {
            return;
        }
        sender.sendMessage(usage);
    }

    private int compareCommands(VanillaCommand a, VanillaCommand b) {
        return a.getName().compareTo(b.getName());
    }

    @Override
    public Component usage(CommandSender sender, CommandContext context) {
        return Component.text("/help [<command>]");
    }

    public void defaultor(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("=== Help ==="));

        List<VanillaCommand> commands = new ArrayList<>(VanillaCommandsFeature.getRegisteredCommands().values());

        commands.sort(this::compareCommands);

        commands.forEach(command -> sender.sendMessage(Component.text("/" + command.getName().toLowerCase())));

        sender.sendMessage(Component.text("============"));
    }
}