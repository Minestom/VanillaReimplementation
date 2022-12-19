package net.minestom.vanilla.commands.execute;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentGroup;
import net.minestom.server.command.builder.arguments.ArgumentLoop;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.vanilla.commands.data.AllOrMasked;
import net.minestom.vanilla.commands.data.Anchor;
import net.minestom.vanilla.commands.data.Swizzle;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;
import org.tinylog.Supplier;

import java.util.HashMap;
import java.util.Map;

import static net.minestom.server.command.builder.arguments.ArgumentType.Double;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;

import java.lang.String;

class ExecuteCommandArguments {

    interface LoopArgument {
        void handle(CommandContext context, MutableNBTCompound data, Supplier<CommandContext> next);
    }

    private static abstract class GroupLoopArgument extends ArgumentGroup implements LoopArgument {
        public GroupLoopArgument(@NotNull String id, @NotNull Argument<?>... group) {
            super(id, group);
        }
    }

    //////////////////////
    // Common Arguments //
    //////////////////////

    static final @NotNull Argument<String> COMPARATOR = Word("comparator").from("<", "<=", "=", ">=", ">")
            .setDefaultValue("=");
    static final Argument<CommandContext> TARGET_SCORE_HOLDER = Group("target", Entity("entity"), Literal("*"));
    static final Argument<CommandContext> SOURCE_SCORE_HOLDER = Group("source", Entity("entity"), Literal("*"));
    static final ArgumentWord OBJECTIVE = Word("objective");
    static final ArgumentWord NBT_PATH = Word("path");
    static final ArgumentWord DATA_TYPE = Word("type").from("byte", "short", "int", "long", "float", "double");

    /////////////////
    // Subcommands //
    /////////////////

    // Modify subcommands
    Modify modify = new Modify();

    public static class Modify {
        final ArgumentGroup align = Group("align", Literal("align"), Swizzle.ARGUMENT);
        final ArgumentGroup anchored = Group("anchored", Literal("anchored"), Anchor.ARGUMENT);
        final ArgumentGroup as = Group("as", Literal("as"), Entity("target"));
        final ArgumentGroup at = Group("at", Literal("at"), Entity("target"));
        final ArgumentGroup facing = Group("facing", Literal("facing"), RelativeVec3("pos"));
        final ArgumentGroup facingEntity = Group("facingEntity", Literal("facing"), Literal("entity"),
                Entity("targets"), Anchor.ARGUMENT);
        final ArgumentGroup in = Group("in", Literal("in"), ResourceLocation("dimension"));
        final ArgumentGroup positioned = Group("positioned", Literal("positioned"), RelativeVec3("pos"));
        final ArgumentGroup positionedAs = Group("positionedAs", Literal("positioned"), Literal("as"),
                Entity("target").singleEntity(true));
        final ArgumentGroup rotated = Group("rotated", Literal("rotated"), RelativeVec2("rotation"));
        final ArgumentGroup rotatedAs = Group("rotatedAs", Literal("rotated"), Literal("as"),
                Entity("target").singleEntity(true));
        final Map<String, ArgumentGroup> groups = Map.copyOf(new HashMap<>() {{
            put("align", align);
            put("anchored", anchored);
            put("as", as);
            put("at", at);
            put("facing", facing);
            put("facingEntity", facingEntity);
            put("in", in);
            put("positioned", positioned);
            put("positionedAs", positionedAs);
            put("rotated", rotated);
            put("rotatedAs", rotatedAs);
        }});
    }

    // Conditional subcommands
    Conditional conditional = new Conditional();

    public static class Conditional {
        final ArgumentWord ifUnless = Word("ifUnless").from("if", "unless");
        final ArgumentGroup conditionBlock = Group("ifBlock", ifUnless, Literal("block"),
                RelativeBlockPosition("pos"), BlockState("block"));
        final ArgumentGroup conditionBlocks = Group("ifBlocks", ifUnless, Literal("blocks"),
                RelativeBlockPosition("start"), RelativeBlockPosition("end"), RelativeBlockPosition("destination"),
                AllOrMasked.ARGUMENT);
        final ArgumentGroup conditionDataBlock = Group("ifDataBlock", ifUnless, Literal("data"), Literal("block"),
                RelativeBlockPosition("pos"), NBT_PATH);
        final ArgumentGroup conditionDataEntity = Group("ifDataEntity", ifUnless, Literal("data"), Literal("entity"),
                Entity("target").singleEntity(true), NBT_PATH);
        final ArgumentGroup conditionDataStorage = Group("ifDataStorage", ifUnless, Literal("data"), Literal("storage"),
                ResourceLocation("namespace"), NBT_PATH);
        final ArgumentGroup conditionEntity = Group("ifEntity", ifUnless, Literal("entity"), Entity("target"));
        final ArgumentGroup conditionPredicate = Group("ifPredicate", ifUnless, Literal("predicate"),
                ResourceLocation("namespace"));
        final ArgumentGroup conditionScore = Group("ifScore", ifUnless, Literal("score"), TARGET_SCORE_HOLDER,
                Word("targetObjective"), COMPARATOR, SOURCE_SCORE_HOLDER, Word("sourceObjective"));
        final ArgumentGroup conditionScoreMatches = Group("ifScoreMatches", ifUnless, Literal("score"), TARGET_SCORE_HOLDER,
                Word("targetObjective"), COMPARATOR, IntRange("range"));
        final Map<String, ArgumentGroup> groups = Map.copyOf(new HashMap<>() {{
            put("block", conditionBlock);
            put("blocks", conditionBlocks);
            put("data", Group("dataConditions", conditionDataBlock, conditionDataEntity, conditionDataStorage));
            put("entity", conditionEntity);
            put("predicate", conditionPredicate);
            put("score", Group("scoreConditions", conditionScore, conditionScoreMatches));
        }});
    }

    // Store subcommands
    Store store = new Store();

    public static class Store {
        final @NotNull Argument<String> resultOrSuccess = Word("resultOrSuccess").from("result", "success");
        final ArgumentGroup storeBlock = Group("storeBlock", Literal("store"), resultOrSuccess, Literal("block"),
                RelativeBlockPosition("targetPos"), NBT_PATH, DATA_TYPE, Double("scale"));
        final ArgumentGroup storeBossbar = Group("storeBossbar", Literal("store"), resultOrSuccess, Literal("bossbar"),
                ResourceLocation("id"), Word("value").from("value", "max"));
        final ArgumentGroup storeEntity = Group("storeEntity", Literal("store"), resultOrSuccess, Literal("entity"),
                Entity("target").singleEntity(true), NBT_PATH, DATA_TYPE, Double("scale"));
        final ArgumentGroup storeScore = Group("storeScore", Literal("store"), resultOrSuccess, Literal("score"),
                TARGET_SCORE_HOLDER, OBJECTIVE);
        final ArgumentGroup storeStorage = Group("storeStorage", Literal("store"), resultOrSuccess, Literal("storage"),
                ResourceLocation("target"), NBT_PATH, DATA_TYPE, Double("scale"));
        final Map<String, ArgumentGroup> groups = Map.copyOf(new HashMap<>() {{
            put("block", storeBlock);
            put("bossbar", storeBossbar);
            put("entity", storeEntity);
            put("score", storeScore);
            put("storage", storeStorage);
        }});
    }

    // Run subcommands
    final ArgumentGroup run = Group("run", Command("run"));
    Map<String, ArgumentGroup> runGroups = Map.copyOf(new HashMap<>() {{
        put("run", run);
    }});

    private final Map<String, ArgumentGroup> allGroups = Map.copyOf(new HashMap<>() {{
        putAll(modify.groups);
        putAll(conditional.groups);
        putAll(store.groups);
        putAll(runGroups);
    }});
    final ArgumentLoop<CommandContext> fullLoop = Loop("loop", allGroups.values().toArray(ArgumentGroup[]::new));
}
