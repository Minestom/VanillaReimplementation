package net.minestom.vanilla.datapack.worldgen.random;

import net.minestom.vanilla.datapack.worldgen.Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

record XoroshiroPositionalRandom(long seedLow, long seedHigh) implements WorldgenRandom.Positional {

    @Override
    public WorldgenRandom fromHashOf(String name) {
        try {
            var messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(name.getBytes());
            byte[] hash = messageDigest.digest();
            long lo = Util.longfromBytes(hash[0], hash[1], hash[2], hash[3], hash[4], hash[5], hash[6], hash[7]);
            long hi = Util.longfromBytes(hash[8], hash[9], hash[10], hash[11], hash[12], hash[13], hash[14], hash[15]);
            return new XoroshiroRandom(lo ^ this.seedLow, hi ^ this.seedHigh);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WorldgenRandom fromSeed(long seed) {
        return new XoroshiroRandom(seed ^ seedLow, seedHigh);
    }

    @Override
    public long[] seedKey() {
        return new long[]{seedLow, seedHigh};
    }
}
