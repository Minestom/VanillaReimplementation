package net.minestom.vanilla.datapack.json;

import net.minestom.server.utils.NamespaceID;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NamespaceTag {
    String value();
}
