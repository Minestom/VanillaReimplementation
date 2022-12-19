package net.minestom.vanilla.commands.execute;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.location.RelativeVec;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.commands.ArgumentParsers;
import net.minestom.vanilla.commands.data.VanillaCommandData;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minestom.vanilla.commands.data.Anchor;

import net.minestom.vanilla.commands.data.Swizzle;

// TODO: What is the correct behaviour for conflicting execute modifiers?
// e.g. Should this run for a single player, or all players? execute as @p as @a run help
public class ExecuteCommand extends Command {

    ExecuteCommandArguments arguments = new ExecuteCommandArguments();

    private final VanillaReimplementation vri;

    public ExecuteCommand(@NotNull VanillaReimplementation vri) {
        super("execute");
        this.vri = vri;

        addSyntax(this::usageRun, arguments.fullLoop);
        setDefaultExecutor((sender, context) -> {
            // TODO: Better error/usage message
            sender.sendMessage("Usage: /execute <run|if|unless|as|at|positioned|rotated|facing|align|anchored> ...");
        });
    }

    private void usageRun(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Queue<CommandContext> contextStack = new ArrayDeque<>(context.get(arguments.fullLoop));

        // Setup modificator
        CommandSender commandSender = sender;
        Pos position = sender instanceof Entity entity ? entity.getPosition() : Pos.ZERO;
        Instance instance = sender instanceof Entity entity ? entity.getInstance() : MinecraftServer.getInstanceManager()
                .getInstances()
                .stream()
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No instances loaded. Cannot execute command."));
        Anchor anchor = Anchor.DEFAULT;
        assert instance != null;

        VanillaCommandData start = new VanillaCommandData(commandSender, position, instance, anchor);
        Stream<VanillaCommandData> stream = Stream.of(start);

        while (!contextStack.isEmpty()) {
            CommandContext next = contextStack.poll();
            if (!next.getInput().equals("run")) {
                stream = process(next, () -> contextStack.remove().getInput(), stream);
            } else {
                break;
            }
        }

        // run the command
        String commandToRun = contextStack.stream()
                .map(CommandContext::getInput)
                .collect(Collectors.joining(" "));
        contextStack.clear();

        Logger.info("Running command: {}", commandToRun);

        for (VanillaCommandData data : stream.toList()) {
            // TODO: Make this based on the vri object, not static process.
            // TODO: Actually use this data
            MinecraftServer.getCommandManager().execute(data.commandSender(), commandToRun);
        }
    }

    private Stream<VanillaCommandData> process(CommandContext context, Supplier<String> next,
                                               Stream<VanillaCommandData> stream) {
        String input = context.getInput();
        Logger.info(input);

        if (arguments.modify.groups.containsKey(input)) {
            return handleModify(input, next, stream);
        }

        // TODO: Handle other command syntax groups
        return stream;
    }

    private Stream<VanillaCommandData> handleModify(String input, Supplier<String> next,
                                                    Stream<VanillaCommandData> stream) {
        return switch (input) {
            // Modify
            case "align" -> handleModifyAlign(next, stream);
            case "anchored" -> handleModifyAnchored(next, stream);
            case "as" -> handleModifyAs(next, stream);
            case "at" -> handleModifyAt(next, stream);
            case "facing" -> {
                String nextInput = next.get();
                yield nextInput.equals("entity") ? handleModifyFacingEntity(next, stream) :
                        handleModifyFacing(nextInput, next, stream);
            }
            case "in" -> handleModifyIn(next, stream);
            case "positioned" -> {
                String nextInput = next.get();
                yield nextInput.equals("as") ? handleModifyPositionedAs(next, stream) :
                        handleModifyPositioned(nextInput, next, stream);
            }
            case "rotated" -> {
                String nextInput = next.get();
                yield nextInput.equals("as") ? handleModifyRotatedAs(next, stream) :
                        handleModifyRotated(nextInput, next, stream);
            }
            default -> throw new IllegalStateException("Unexpected value: " + input);
        };
    }

    private Stream<VanillaCommandData> handleModifyRotated(String nextInput, Supplier<String> next, Stream<VanillaCommandData> stream) {
        @NotNull RelativeVec pos = ArgumentParsers.RELATIVE_VEC_2.parse(next, nextInput);
        return stream.map(data -> {
            Pos old = data.position();
            Vec targetPos = pos.fromSender(data.commandSender());
            return data.with(new Pos(old.x(), old.y(), old.z(), (float) targetPos.x(), (float) targetPos.y()));
        });
    }

    private Stream<VanillaCommandData> handleModifyPositionedAs(Supplier<String> next, Stream<VanillaCommandData> stream) {
        @NotNull EntityFinder finder = ArgumentParsers.ENTITY.parse(next);
        return stream.flatMap(data -> {
            return finder.find(data.commandSender())
                    .stream()
                    .filter(entity -> entity.getInstance() != null)
                    .map(entity -> data.with(entity.getPosition(), entity.getInstance()));
        });
    }

    private Stream<VanillaCommandData> handleModifyAnchored(Supplier<String> next, Stream<VanillaCommandData> stream) {
        Anchor anchor = ArgumentParsers.ANCHOR.parse(next.get());
        return stream.map(data -> data.with(anchor));
    }

    private Stream<VanillaCommandData> handleModifyAs(Supplier<String> next, Stream<VanillaCommandData> stream) {
        @NotNull EntityFinder finder = ArgumentParsers.ENTITY.parse(next.get());
        return stream.flatMap(data -> {
            return finder.find(data.commandSender())
                    .stream()
                    .filter(entity -> entity instanceof CommandSender)
                    .map(entity -> (CommandSender) entity)
                    .map(data::with);
        });
    }

    private Stream<VanillaCommandData> handleModifyAt(Supplier<String> next, Stream<VanillaCommandData> stream) {
        @NotNull EntityFinder finder = ArgumentParsers.ENTITY.parse(next.get());
        return stream.flatMap(data -> {
            return finder.find(data.commandSender())
                    .stream()
                    .map(entity -> data.with(entity.getPosition()));
        });
    }

    private Stream<VanillaCommandData> handleModifyFacing(String nextInput, Supplier<String> next, Stream<VanillaCommandData> stream) {
        @NotNull RelativeVec pos = ArgumentParsers.RELATIVE_VEC_3.parse(nextInput);
        return stream.map(data -> {
            Pos old = data.position();
            Vec targetPos = pos.fromSender(data.commandSender());

            // TODO: Is this math correct?
            float angleYaw = (float) Math.atan2(targetPos.z() - old.z(), targetPos.x() - old.x());
            float anglePitch = (float) Math.atan2(targetPos.y() - old.y(), Math.sqrt(Math.pow(targetPos.x() - old.x(), 2) + Math.pow(targetPos.z() - old.z(), 2)));
            Pos newPos = new Pos(old.x(), old.y(), old.z(), angleYaw, anglePitch);

//          double eyeHeight = entity.getEyeHeight();
//          double yOffset = anchor == Anchor.feet ? 0 : eyeHeight;

            return data.with(newPos);
        });
    }

    private Stream<VanillaCommandData> handleModifyFacingEntity(Supplier<String> next, Stream<VanillaCommandData> stream) {
        EntityFinder finder = ArgumentParsers.ENTITY.parse(next.get());
        Anchor anchor = ArgumentParsers.ANCHOR.parse(next.get());

        return stream.flatMap(data -> {
            return finder.find(data.commandSender())
                    .stream()
                    .map(entity -> {
                        Pos old = data.position();
                        double eyeHeight = entity.getEyeHeight();
                        double yOffset = anchor == Anchor.feet ? 0 : eyeHeight;
                        Pos targetPos = entity.getPosition().add(0, yOffset, 0);

                        float angleYaw = (float) Math.atan2(targetPos.z() - old.z(), targetPos.x() - old.x());
                        float anglePitch = (float) Math.atan2(targetPos.y() - old.y(), Math.sqrt(Math.pow(targetPos.x() - old.x(), 2) + Math.pow(targetPos.z() - old.z(), 2)));

                        Pos newPos = new Pos(old.x(), old.y(), old.z(), angleYaw, anglePitch);
                        return data.with(newPos);
                    });
        });
    }

    private Stream<VanillaCommandData> handleModifyIn(Supplier<String> next, Stream<VanillaCommandData> stream) {
        String dimensionId = ArgumentParsers.RESOURCE_LOCATION.parse(next.get());
        Instance instance = vri.getInstance(NamespaceID.from(dimensionId));
        if (instance == null) {
            throw new IllegalArgumentException("Unknown dimension: " + dimensionId);
        }
        return stream.map(data -> data.with(instance));
    }

    private Stream<VanillaCommandData> handleModifyAlign(Supplier<String> next, Stream<VanillaCommandData> stream) {
        Swizzle swizzle = ArgumentParsers.SWIZZLE.parse(next);
        return stream.map(data -> {
            Pos old = data.position();
            return data.with(new Pos(swizzle.has_x ? old.blockX() : old.x(),
                    swizzle.has_y ? old.blockY() : old.y(),
                    swizzle.has_z ? old.blockZ() : old.z()));
        });
    }

    private Stream<VanillaCommandData> handleModifyPositioned(String nextInput, Supplier<String> next, Stream<VanillaCommandData> stream) {
        @NotNull RelativeVec pos = ArgumentParsers.RELATIVE_VEC_3.parse(next, nextInput);
        return stream.map(data -> {
            Pos old = data.position();
            Vec targetPos = pos.fromSender(data.commandSender());
            return data.with(new Pos(targetPos.x(), targetPos.y(), targetPos.z(), old.yaw(), old.pitch()));
        });
    }

    private Stream<VanillaCommandData> handleModifyRotatedAs(Supplier<String> next, Stream<VanillaCommandData> stream) {
        @NotNull EntityFinder finder = ArgumentParsers.ENTITY.parse(next.get());
        return stream.flatMap(data -> {
            return finder.find(data.commandSender())
                    .stream()
                    .map(entity -> {
                        Pos old = data.position();
                        Pos entityPos = entity.getPosition();
                        return data.with(new Pos(old.x(), old.y(), old.z(), entityPos.yaw(), entityPos.pitch()));
                    });
        });
    }
}
