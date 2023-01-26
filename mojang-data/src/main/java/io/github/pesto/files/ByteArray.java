package io.github.pesto.files;

import net.minestom.server.utils.MathUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteArray {

    private final byte[] bytes;

    private ByteArray(byte[] b, boolean copy) {
        this.bytes = copy ? deepCopy(b) : b;
    }

    public static ByteArray wrap(byte[] bytes) {
        return new ByteArray(bytes, false);
    }

    public static ByteArray copyOf(byte[] bytes) {
        return new ByteArray(bytes, true);
    }

    public byte[] array() {
        return deepCopy(bytes);
    }

    public int size() {
        return bytes.length;
    }

    public byte index(int i) {
        if (!MathUtils.isBetween(i, 0, size()))
            throw new ArrayIndexOutOfBoundsException();
        return bytes[i];
    }

    public InputStream toStream() {
        return new ByteArrayInputStream(bytes);
    }

    public String toString() {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private byte[] deepCopy(byte[] source) {
        byte[] copy = new byte[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteArray byteArray = (ByteArray) o;
        return Arrays.equals(bytes, byteArray.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}