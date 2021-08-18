package net.minestom.vanilla;

import net.minestom.server.Bootstrap;

public class LaunchServer {
    public static void main(String[] args) {
        var argsWithMixins = withMixins(args, "world");
        Bootstrap.bootstrap("net.minestom.vanilla.VanillaServer", argsWithMixins);
    }

    private static String[] withMixins(String[] args, String... mixins) {
        // allow to load mixins without using an extension, nor enforcing launch arguments
        String[] argsWithMixins = new String[args.length + (mixins.length * 2)];
        System.arraycopy(args, 0, argsWithMixins, 0, args.length);
        for (int i = args.length; i < args.length + (mixins.length * 2); i += 2) {
            argsWithMixins[i] = "--mixin";
            argsWithMixins[i + 1] = "mixins." + mixins[i - args.length] + ".json";
        }
        return argsWithMixins;
    }
}
