package net.minestom.codegen.biomes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.minestom.codegen.EnumGenerator;
import net.minestom.codegen.MinestomEnumGenerator;
import net.minestom.codegen.PrismarinePaths;
import net.minestom.server.registry.ResourceGatherer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
public class VanillaBiomeGenerator extends MinestomEnumGenerator<VanillaBiomeContainer> {

    private final String targetVersion;
    private final File targetFolder;

    public static void main(String[] args) throws IOException {
        String targetVersion;
        if (args.length < 1) {
            System.err.println("Usage: <MC version> [target folder]");
            return;
        }

        targetVersion = args[0];

        try {
            ResourceGatherer.ensureResourcesArePresent(targetVersion, null); // TODO
        } catch (IOException e) {
            e.printStackTrace();
        }

        String targetPart = DEFAULT_TARGET_PATH;
        if (args.length >= 2) {
            targetPart = args[1];
        }

        File targetFolder = new File(targetPart);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        new VanillaBiomeGenerator(targetVersion, targetFolder);
    }

    private VanillaBiomeGenerator(String targetVersion, File targetFolder) throws IOException {
        this.targetVersion = targetVersion;
        this.targetFolder = targetFolder;
        generateTo(targetFolder);
    }

    @Override
    protected Collection<VanillaBiomeContainer> compile() throws IOException {
        Gson gson = new Gson();
        log.debug("Finding path for PrismarineJS biomes");
        JsonObject dataPaths = gson.fromJson(new BufferedReader(new FileReader(PRISMARINE_JS_DATA_PATHS)), JsonObject.class);
        JsonObject pathsJson = dataPaths.getAsJsonObject("pc").getAsJsonObject(targetVersion);

        PrismarinePaths paths = gson.fromJson(pathsJson, PrismarinePaths.class);
        log.debug("Loading PrismarineJS biomes data");
        return parseBiomesFromPrismarine(gson, paths.getBiomesFile());
    }

    private List<VanillaBiomeContainer> parseBiomesFromPrismarine(Gson gson, File biomeFile) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(biomeFile))) {
            VanillaBiomeContainer[] blocks = gson.fromJson(bufferedReader, VanillaBiomeContainer[].class);
            return Arrays.asList(blocks);
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    protected void writeSingle(EnumGenerator enumGenerator, VanillaBiomeContainer container) {
        enumGenerator.addInstance(container.name.toUpperCase(), container.id);
    }

    @Override
    protected void postWrite(EnumGenerator enumGenerator) {}

    @Override
    protected void postGeneration() {}

    @Override
    public String getPackageName() {
        return "net.minestom.vanilla.data";
    }

    @Override
    public String getClassName() {
        return "VanillaBiomeIDs";
    }

    @Override
    protected void prepare(EnumGenerator enumGenerator) {
        enumGenerator.setParams("int id");
        enumGenerator.addMethod("fromID", "(String name)", "static "+getClassName(),
                "return "+getClassName()+".valueOf(name.toUpperCase());"
        );
        enumGenerator.addMethod("getID", "()", "int", "return id;");
    }
}
