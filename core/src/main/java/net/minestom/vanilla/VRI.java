package net.minestom.vanilla;

import java.util.ServiceLoader;

public class VRI {

    public static void hook() {
        ServiceLoader<Module> loader = ServiceLoader.load(Module.class);
        for (Module module : loader) {
            module.hook();
        }
    }

}
