package net.minestom.vanilla;

import net.minestom.server.Bootstrap;

public class LaunchServer {

    public static void main(String[] args) {
        // allow to load mixins without using an extension, nor enforcing launch arguments
        String[] argsWithMixins = new String[args.length+2];
        System.arraycopy(args, 0, argsWithMixins, 0, args.length);
        argsWithMixins[argsWithMixins.length-2] = "--mixin";
        argsWithMixins[argsWithMixins.length-1] = "mixins.vanilla.json";
        Bootstrap.bootstrap("net.minestom.vanilla.VanillaServer", argsWithMixins);
    }

}
