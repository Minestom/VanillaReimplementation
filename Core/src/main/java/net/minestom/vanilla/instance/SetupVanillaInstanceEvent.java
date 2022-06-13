package net.minestom.vanilla.instance;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public class SetupVanillaInstanceEvent implements InstanceEvent {

    private final Instance instance;

    public SetupVanillaInstanceEvent(@NotNull Instance instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull Instance getInstance() {
        return instance;
    }
}
