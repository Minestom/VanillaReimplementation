package net.minestom.vanilla.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.world.Difficulty;

import static net.minestom.server.command.builder.arguments.ArgumentEnum.Format;

/**
 * Sets the difficulty level (peaceful, easy, etc.).
 *
 * @see <a href=https://minecraft.fandom.com/wiki/Commands/difficulty>Source<a/>
 */
public class DifficultyCommand extends VanillaCommand {

    public DifficultyCommand() {
        super("difficulty", 2);
        var difficulty = arg();
        setDefaultExecutor(((sender, context) -> this.sendTranslatable(sender, "commands.difficulty.query", difficulty)));
        addSyntax(this::execute, difficulty);
    }

    public Component difficulty() {
        String difficulty = MinecraftServer.getDifficulty().name().toLowerCase();
        String key = "options.difficulty." + difficulty;
        return Component.translatable(key);
    }

    @Override
    protected String usage() {
        return """
                /difficulty [peaceful]
                /difficulty [easy]
                /difficulty [normal]
                /difficulty [hard]""";
    }

    private void execute(CommandSender sender, CommandContext arguments) {
        String difficultyName = arguments.get("difficulty");
        Difficulty difficulty = Difficulty.valueOf(difficultyName.toUpperCase());
        MinecraftServer.setDifficulty(difficulty);
        sendTranslatable(sender, "commands.difficulty.success", difficulty());
    }

    private ArgumentEnum<Difficulty> arg() {
        var arg = ArgumentType.Enum("difficulty", Difficulty.class).setFormat(Format.LOWER_CASED);
        arg.setCallback((sender, exception) -> sendTranslatable(sender, "command.unknown.argument"));
        return arg;
    }
}