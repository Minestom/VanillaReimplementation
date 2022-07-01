package net.minestom.vanilla.entities;

import com.google.auto.service.AutoService;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.Module;

@AutoService(Module.class)
public class EntityModule implements Module {
    private static final NamespaceID ID = NamespaceID.from("vri:entities");

    @Override
    public NamespaceID id() {
        return ID;
    }

    @Override
    public void hook() {
        // Register each entity i guess
    }
}
