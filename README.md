# NOT READY FOR PRODUCTION

Priority is currently on the core of Minestom (see below). This project has only a very limited list of features:

1. Some block interactions (Cakes, Beds, Copper Oxidisation)
2. Some commands (Gamemode, help, execute)
3. Fluid simulation [Thank you TogAr2](https://github.com/TogAr2/MinestomFluids)

# About Minestom

See [Minestom Project on GitHub](https://github.com/Minestom/Minestom)

# Cloning

`git clone --recurse-submodules https://github.com/Minestom/VanillaReimplementation`

# How to use

You can use this repo by finding the latest release [here](https://jitpack.io/#Minestom/VanillaReimplementation).
After selecting your release, make sure to choose which modules (vanila features) you want.
The "core" module is required. Everything else is optional and up to you.

Once you have added your modules to your classpath, you can initiate vri in your server's startup using this snippet:
`VanillaReimplementation vri = VanillaReimplementation.hook(MinecraftServer.process());`.

See [here](https://github.com/Minestom/VanillaReimplementation/blob/93f29ab67ffff7d78e34b12ab5f00619109c84c7/server/src/main/java/net/minestom/vanilla/server/VanillaServer.java#L44) for an example.
