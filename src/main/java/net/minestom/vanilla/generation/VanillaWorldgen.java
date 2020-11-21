package net.minestom.vanilla.generation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockAlternative;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeEffects;
import net.minestom.server.world.biomes.BiomeManager;
import net.minestom.server.world.biomes.BiomeParticles;
import net.minestom.vanilla.io.MinecraftData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jglrxavpok.hephaistos.json.NBTGsonReader;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
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
        if (toCheck.exists()) {
            logger.debug(toCheck.getAbsolutePath() + " exists, assuming worldgen files are present.");
            return;
        }

        // TODO: not production ready?
        // TODO: are we allowed to embed the zip, or redistribute it?
        // TODO: or run "git clone https://github.com/slicedlime/examples/ vanilla_worldgen_example" if possible
        logger.debug(toCheck.getAbsolutePath() + " does not exit, assuming worldgen files are not present, extracting from vanilla_worldgen_example.");

        File worldgen = new File("./vanilla_worldgen_example/vanilla_worldgen.zip");
        try (ZipInputStream input = new ZipInputStream(new BufferedInputStream(new FileInputStream(worldgen)))) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                if (entry.isDirectory())
                    continue;
                File targetFile = new File(root, entry.getName());
                if (!targetFile.getParentFile().exists()) {
                    targetFile.getParentFile().mkdirs();
                }
                if (!targetFile.exists()) {
                    targetFile.createNewFile();
                }
                Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Copied " + entry.getName() + " to " + targetFile.getAbsolutePath());
            }
        }
    }

    public static void registerAllBiomes(BiomeManager biomeManager) throws IOException {
        // TODO: allow support for custom namespaces
        File biomeFolder = new File(ResourceGatherer.DATA_FOLDER, "data/minecraft/worldgen/biome/");
        File[] files = biomeFolder.listFiles();
        int maxID = 0;
        if (files != null) {
            for (File biomeFile : files) {
                String nameWithoutExtension = biomeFile.getName().substring(0, biomeFile.getName().lastIndexOf("."));
                NamespaceID id = NamespaceID.from("minecraft:" + nameWithoutExtension);
                if (id.equals(Biome.PLAINS.getName()))
                    continue;
                Biome biome = buildBiome(id);
                biomeManager.addBiome(biome);
                maxID = Math.max(biome.getId(), maxID);
            }
        }
        Biome.ID_COUNTER.set(maxID);
    }

    private static Biome buildBiome(NamespaceID id) {
        JsonObject biomeJSON = MinecraftData.open(id, "worldgen/biome", JsonObject.class).orElseThrow();
        JsonObject effectsJSON = biomeJSON.getAsJsonObject("effects");
        JsonObject moodSoundsJSON = effectsJSON.getAsJsonObject("mood_sound");
        BiomeEffects.GrassColorModifier grassColorModifier =
                effectsJSON.has("grass_color_modifier") ?
                        BiomeEffects.GrassColorModifier.valueOf(effectsJSON.get("grass_color_modifier").getAsString().toUpperCase()) :
                        BiomeEffects.GrassColorModifier.NONE;
        BiomeEffects.MoodSound moodSounds = null;
        if (moodSoundsJSON == null) {
            final NamespaceID sound = NamespaceID.from(moodSoundsJSON.get("sound").getAsString());
            final int tickDelay = moodSoundsJSON.get("tick_delay").getAsInt();
            final int blockSearchExtent = moodSoundsJSON.get("block_search_extent").getAsInt();
            final double offset = moodSoundsJSON.get("offset").getAsFloat();
            moodSounds = new BiomeEffects.MoodSound(sound, tickDelay, blockSearchExtent, offset);
        }

        int grassColor = effectsJSON.has("grass_color") ? effectsJSON.get("grass_color").getAsInt() : -1;
        int foliageColor = effectsJSON.has("foliage_color") ? effectsJSON.get("foliage_color").getAsInt() : -1;
        BiomeEffects.AdditionsSound additionsSound = null;
        if (effectsJSON.has("additions_sound")) {
            additionsSound = new BiomeEffects.AdditionsSound(NamespaceID.from(effectsJSON.getAsJsonObject("additions_sound").get("sound").getAsString()),
                    effectsJSON.getAsJsonObject("additions_sound").get("tick_chance").getAsFloat());
        }
        NamespaceID ambientSound = null;
        if (effectsJSON.has("ambient_sound")) {
            ambientSound = NamespaceID.from(effectsJSON.get("ambient_sound").getAsString());
        }
        BiomeParticles biomeParticles = null;
        if (effectsJSON.has("biome_particles")) {
            biomeParticles = loadParticles(effectsJSON.getAsJsonObject("biome_particles"));
        }
        BiomeEffects.Music music = null;
        if (effectsJSON.has("music")) {
            final NamespaceID sound = NamespaceID.from(effectsJSON.getAsJsonObject("music").get("sound").getAsString());
            final int minDelay = effectsJSON.getAsJsonObject("music").get("min_delay").getAsInt();
            final int maxDelay = effectsJSON.getAsJsonObject("music").get("max_delay").getAsInt();
            final boolean replaceCurrentMusic = effectsJSON.getAsJsonObject("music").get("replace_current_music").getAsBoolean();
            music = new BiomeEffects.Music(sound, minDelay, maxDelay, replaceCurrentMusic);
        }
        BiomeEffects effects = BiomeEffects.builder()
                .moodSound(moodSounds)
                .waterFogColor(effectsJSON.get("water_fog_color").getAsInt())
                .fogColor(effectsJSON.get("fog_color").getAsInt())
                .skyColor(effectsJSON.get("sky_color").getAsInt())
                .waterColor(effectsJSON.get("water_color").getAsInt())
                .grassColorModifier(grassColorModifier)
                .grassColor(grassColor)
                .foliageColor(foliageColor)
                .additionsSound(additionsSound)
                .ambientSound(ambientSound)
                .biomeParticles(biomeParticles)
                .music(music)
                .build();
        Biome.TemperatureModifier temperatureModifier = Biome.TemperatureModifier.NONE;
        if (biomeJSON.has("temperature_modifier")) {
            temperatureModifier = Biome.TemperatureModifier.valueOf(biomeJSON.get("temperature_modifier").getAsString().toUpperCase());
        }
        return Biome.builder()
                .category(Biome.Category.valueOf(biomeJSON.get("category").getAsString().toUpperCase()))
                .scale(biomeJSON.get("scale").getAsFloat())
                .effects(effects)
                .name(id)
                .depth(biomeJSON.get("depth").getAsFloat())
                .downfall(biomeJSON.get("downfall").getAsFloat())
                .temperature(biomeJSON.get("temperature").getAsFloat())
                .precipitation(Biome.Precipitation.valueOf(biomeJSON.get("precipitation").getAsString().toUpperCase()))
                .temperatureModifier(temperatureModifier)
                .build();
    }

    private static BiomeParticles loadParticles(JsonObject biomeParticles) {
        final String type = biomeParticles.get("type").getAsString();
        BiomeParticles.ParticleOptions particleOptions = null;
        switch (type) {
            case "dust":
                particleOptions = new BiomeParticles.DustParticle(
                        biomeParticles.get("red").getAsFloat(),
                        biomeParticles.get("green").getAsFloat(),
                        biomeParticles.get("blue").getAsFloat(),
                        biomeParticles.get("scale").getAsFloat());

            case "block":
                Block block = Registries.getBlock(biomeParticles.get("Name").getAsString());
                BlockAlternative alternative = block.getAlternative(findAlternative(block, biomeParticles.get("Properties")));
                particleOptions = new BiomeParticles.BlockParticle(alternative);

            case "item":
                ItemStack stack = ItemStack.fromNBT(new NBTGsonReader(new StringReader(biomeParticles.toString())).read(NBTCompound.class));
                particleOptions = new BiomeParticles.ItemParticle(stack);

            default:
                particleOptions = new BiomeParticles.NormalParticle(NamespaceID.from(type));
        }
        return new BiomeParticles(biomeParticles.get("probability").getAsFloat(), particleOptions);
    }

    private static short findAlternative(Block block, JsonElement properties) {
        if (properties == null)
            return block.getBlockId();
        JsonObject props = properties.getAsJsonObject();
        PriorityQueue<String> sortedKeys = new PriorityQueue<>(props.keySet());
        String propertiesString = sortedKeys.stream()
                .map(key -> key + "=" + props.get(key).getAsString())
                .collect(Collectors.joining(","));
        return block.withProperties(propertiesString);
    }
}
