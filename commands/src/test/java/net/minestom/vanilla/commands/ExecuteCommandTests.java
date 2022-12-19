package net.minestom.vanilla.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;
import org.junit.jupiter.api.Test;
import org.tinylog.Logger;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minestom.vanilla.utils.SystemUtils.captureSystemOut;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("JUnitMalformedDeclaration")
@EnvTest
public class ExecuteCommandTests {

    @Test
    public void testParsingExceptions(Env env) {
        // Setup vri + command requirements
        VanillaReimplementation vri = VanillaReimplementation.hook(env.process());
        Instance instance = vri.createInstance(NamespaceID.from("vri:test"), VanillaDimensionTypes.OVERWORLD);
        env.createPlayer(instance, new Pos(0, 0, 0));
        env.tick();
        env.tick();

        // Register dummy command
        Command testCommand = new Command("test");
        AtomicBoolean executed = new AtomicBoolean(false);
        env.process().command().register(testCommand);
        testCommand.setDefaultExecutor((sender, context) -> executed.set(true));

        String sout = captureSystemOut(() -> env.process().command().executeServerCommand("execute " +
                "align xy " +
                "anchored feet " +
                "as @a " +
                "at @s " +
                "facing entity @a feet " +
                "in vri:test " +
                "positioned 0 0 0 " +
                "rotated as @s " +
                "run test"));

        assertFalse(sout.contains("ERROR"), "There should be no errors in the console: \n" + sout);
        assertTrue(executed.get(), "The command should have been executed");
    }

}
