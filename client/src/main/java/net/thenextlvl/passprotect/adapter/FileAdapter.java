package net.thenextlvl.passprotect.adapter;

import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;

public class FileAdapter implements JsonSerializer<File>, JsonDeserializer<File> {

    @Override
    public File deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        return new File(element.getAsString());
    }

    @Override
    public JsonElement serialize(File file, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(file.getAbsolutePath());
    }
}
