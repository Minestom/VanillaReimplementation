package net.minestom.vanilla.generation;

import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeEffects;
import net.minestom.server.world.biomes.BiomeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class VanillaWorldgen {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Extracts the zip from vanilla_worldgen_example into the minecraft_data/data/assets/minecraft/ folder if minecraft_data/data/assets/minecraft/dimension_type does not exist
     */
    public static void prepareFiles() throws IOException {
        File root = new File(ResourceGatherer.DATA_FOLDER, "data/minecraft/");
        File toCheck = new File(root, "dimension_type");
        if(toCheck.exists()) {
            logger.debug(toCheck.getAbsolutePath()+" exists, assuming worldgen files are present.");
            return;
        }

        // TODO: not production ready?
        // TODO: are we allowed to embed the zip, or redistribute it?
        // TODO: or run "git clone https://github.com/slicedlime/examples/ vanilla_worldgen_example" if possible
        logger.debug(toCheck.getAbsolutePath()+" does not exit, assuming worldgen files are not present, extracting from vanilla_worldgen_example.");

        File worldgen = new File("./vanilla_worldgen_example/vanilla_worldgen.zip");
        try(ZipInputStream input = new ZipInputStream(new BufferedInputStream(new FileInputStream(worldgen)))) {
            ZipEntry entry;
            while((entry = input.getNextEntry()) != null) {
                if(entry.isDirectory())
                    continue;
                File targetFile = new File(root, entry.getName());
                if(!targetFile.getParentFile().exists()) {
                    targetFile.getParentFile().mkdirs();
                }
                if(!targetFile.exists()) {
                    targetFile.createNewFile();
                }
                Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Copied "+entry.getName()+" to "+targetFile.getAbsolutePath());
            }
        }
    }

    public static void registerAllBiomes(BiomeManager biomeManager) {
        // TODO: allow support for custom namespaces
        File biomeFolder = new File(ResourceGatherer.DATA_FOLDER, "data/minecraft/worldgen/biome/");
        File[] files = biomeFolder.listFiles();
        int maxID = 0;
        if(files != null) {
            for(File biomeFile : files) {
                String nameWithoutExtension = biomeFile.getName().substring(0, biomeFile.getName().lastIndexOf("."));
                NamespaceID id = NamespaceID.from("minecraft:"+nameWithoutExtension);
                if(id.equals(Biome.PLAINS.getName()))
                    continue;
                // TODO: load more information
                Biome biome = Biome.builder()
                        .name(id)
                        .effects(BiomeEffects.builder()
                                .build())
                        .category(Biome.Category.NONE)
                        .build();
                biomeManager.addBiome(biome);
                maxID = Math.max(biome.getId(), maxID);
            }
        }
        Biome.idCounter.set(maxID);
    }
}
