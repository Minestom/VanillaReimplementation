package net.minestom.vanilla.generation;

import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.instance.SetupVanillaInstanceEvent;
import org.jetbrains.annotations.NotNull;

public class VanillaWorldGenerationFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull HookContext context) {
        context.vri().process().eventHandler().addListener(SetupVanillaInstanceEvent.class, event -> {
//            event.getInstance().setGenerator(new VanillaTestGenerator());
            event.getInstance().setGenerator(unit -> unit.modifier().setAll((x, y, z) -> {
                return (y > 0 && y < 16) ? Block.COPPER_BLOCK : Block.AIR;
            }));
        });
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("vri:worldgeneration");
    }
}
