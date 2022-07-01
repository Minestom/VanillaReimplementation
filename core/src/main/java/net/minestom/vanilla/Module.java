package net.minestom.vanilla;

import net.minestom.server.utils.NamespaceID;

public interface Module { // Basically Feature, but i didnt realize till later
    // Service provider interface for vri modules

    NamespaceID id();

    void hook();


}
