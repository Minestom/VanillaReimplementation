package net.minestom.vanilla.mixin.world;

import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biomes.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class BiomeMixin {

    @Shadow(remap = false)
    private NamespaceID name;

    @Inject(at = @At("HEAD"), method = "getId", remap = false, require = 1, cancellable = true)
    public void remapID(CallbackInfoReturnable<Integer> info) {
        if(name == null)
            return;
        if(name.getDomain().equals("minecraft")) {
            info.setReturnValue(VanillaBiomeIDs.fromID(name.getPath()).getID());
        }
    }

}
