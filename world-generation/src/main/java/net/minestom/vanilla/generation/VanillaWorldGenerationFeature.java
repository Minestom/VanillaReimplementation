package net.minestom.vanilla.generation;

import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.instance.SetupVanillaInstanceEvent;
import org.jetbrains.annotations.NotNull;

public class VanillaWorldGenerationFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {
        vri.process().eventHandler().addListener(SetupVanillaInstanceEvent.class, event -> {
//            event.getInstance().setGenerator(new VanillaTestGenerator());
            event.getInstance().setGenerator(unit -> {
                unit.modifier().setAll((x, y, z) -> {
                    return y > -60 && y < -50 && x < 32 && x > 0 && z < 32 && z > 0 ? vri.block(Block.COPPER_BLOCK) : Block.AIR;
                });
            });
        });
    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("vri:worldgeneration");
    }
}
