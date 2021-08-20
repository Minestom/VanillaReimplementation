package net.minestom.vanilla.generation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jglrxavpok.hephaistos.json.NBTGsonReader;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeEffects;
import net.minestom.server.world.biomes.BiomeManager;
import net.minestom.server.world.biomes.BiomeParticles;

public final class VanillaWorldgen {

    private static final Logger logger = LogManager.getLogger();

    // TODO: Vanila worldgen
}
