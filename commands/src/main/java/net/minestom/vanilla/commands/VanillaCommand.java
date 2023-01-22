package net.minestom.vanilla.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public abstract class VanillaCommand extends Command {
    protected final String permission;
    protected final int level;

    public VanillaCommand(@NotNull String name, int level, @Nullable String... aliases) {
        super(name, aliases);
        this.permission = "minecraft.command." + name;
        this.level = level;
        setCondition(this::condition);
        setDefaultExecutor(((sender, context) -> sender.sendMessage(usage())));
    }

    public VanillaCommand(@NotNull String name, int level) {
        this(name, level, new String[0]);
    }

    protected boolean condition(@NotNull CommandSender sender, String commandName) {
        return (sender instanceof ConsoleSender) || sender.hasPermission(permission) || (sender instanceof Player) && ((Player) sender).getPermissionLevel() >= level;
    }

    protected abstract String usage();

    @Override
    public @NotNull Collection<CommandSyntax> addSyntax(@NotNull CommandExecutor executor, @NotNull Argument<?>... args) {
        return addConditionalSyntax(this::condition, executor, args);
    }

    @Override
    public @NotNull Collection<CommandSyntax> addSyntax(@NotNull CommandExecutor executor, @NotNull String format) {
        return addConditionalSyntax(this::condition, executor, ArgumentType.generate(format));
    }

    // TODO: Move to own util class
    protected void sendTranslatable(@NotNull CommandSender sender, @NotNull String key) {
        sendTranslatable(sender, key, NamedTextColor.WHITE, Collections.emptyList());
    }

    // TODO: Move to own util class
    protected void sendTranslatable(@NotNull CommandSender sender, @NotNull String key, @NotNull Object @NotNull ... args) {
        sendTranslatable(sender, key, NamedTextColor.WHITE, args);
    }

    // TODO: Move to own util class
    protected void sendTranslatable(@NotNull CommandSender sender, @NotNull String key, @NotNull NamedTextColor color, @NotNull Object... args) {
        sender.sendMessage(Component.translatable(key, color, Arrays.stream(args)
                .map(arg -> arg instanceof Component c ? c : Component.text(arg.toString())).toList()));
    }

    protected ArgumentEntity argPlayer(@NotNull String id) {
        var arg = ArgumentType.Entity(id).onlyPlayers(true);
        return arg;
    }
}
