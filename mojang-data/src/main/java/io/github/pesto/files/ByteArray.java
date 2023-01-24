package io.github.pesto.files;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

public class ByteArray {

    private final byte[] bytes;

    private ByteArray(byte[] b) {
        this.bytes = deepCopy(b);
    }

    public byte[] array() {
        return deepCopy(bytes);
    }

    public int size() {
        return bytes.length;
    }

    public byte index(int i) throws ArrayIndexOutOfBoundsException {
        if (i > size())
            throw new ArrayIndexOutOfBoundsException();

        return bytes[i];
    }

    public InputStream toStream() {
        return new ByteArrayInputStream(bytes);
    }

    private byte[] deepCopy(byte[] source) {
        byte[] copy = new byte[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    public static ByteArray of(byte[] bytes) {
        return new ByteArray(bytes);
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