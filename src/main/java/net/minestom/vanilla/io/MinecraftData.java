package net.minestom.vanilla.io;

import com.google.gson.*;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.utils.NamespaceID;

import java.io.*;
import java.util.Optional;

public final class MinecraftData {

    private static final Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(NamespaceID.class, (JsonDeserializer<?>) (json, typeOfT, context) -> NamespaceID.from(json.getAsString()))
                .create();
    }

    public static InputStream openStream(NamespaceID resource, String type) throws FileNotFoundException {
        return new FileInputStream(new File(ResourceGatherer.DATA_FOLDER, "data/"+resource.getDomain()+"/"+type+"/"+resource.getPath()+".json"));
    }

    public static InputStreamReader openReader(NamespaceID resource, String type) throws FileNotFoundException {
        return new InputStreamReader(openStream(resource, type));
    }

    public static <T> Optional<T> open(NamespaceID resource, String type, Class<T> typeClass) {
        try(var reader = openReader(resource, type)) {
            return Optional.of(gson.fromJson(reader, typeClass));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
