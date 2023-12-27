package net.minestom.vanilla.files;

import com.google.gson.JsonElement;

import java.io.InputStream;
import java.util.function.Function;

interface FileSystemMappers {
    Function<InputStream, ByteArray> INPUT_STREAM_TO_BYTES = inputStream -> {
        try {
            return ByteArray.wrap(inputStream.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };
    Function<ByteArray, String> BYTES_TO_STRING = ByteArray::toCharacterString;
    Function<String, JsonElement> STRING_TO_JSON = string -> FileSystemUtil.gson.fromJson(string, JsonElement.class);


    Function<InputStream, String> INPUT_STREAM_TO_STRING = INPUT_STREAM_TO_BYTES.andThen(BYTES_TO_STRING);
    Function<InputStream, JsonElement> INPUT_STREAM_TO_JSON = INPUT_STREAM_TO_STRING.andThen(STRING_TO_JSON);

    Function<ByteArray, JsonElement> BYTES_TO_JSON = BYTES_TO_STRING.andThen(STRING_TO_JSON);
}
