package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.kyori.adventure.key.Key;
import okio.Buffer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FloatProvider JSON parsing to ensure backward compatibility when converting to codecs.
 */
public class FloatProviderParsingTests {

    @Test
    public void testConstantFloatProviderFromJson() throws IOException {
        String json = "5.5";

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        FloatProvider provider = FloatProvider.fromJson(reader);
        assertNotNull(provider);
        assertTrue(provider instanceof FloatProvider.Constant);
        
        FloatProvider.Constant constant = (FloatProvider.Constant) provider;
        assertEquals(5.5f, constant.value(), 0.001f);
        assertEquals(Key.key("minecraft:constant"), constant.type());
    }

    @Test
    public void testConstantObjectFloatProviderFromJson() throws IOException {
        String json = """
                {
                    "type": "minecraft:constant",
                    "value": 3.14
                }
                """;

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        FloatProvider provider = FloatProvider.fromJson(reader);
        assertNotNull(provider);
        assertTrue(provider instanceof FloatProvider.Constant);
        
        FloatProvider.Constant constant = (FloatProvider.Constant) provider;
        assertEquals(3.14f, constant.value(), 0.001f);
        assertEquals(Key.key("minecraft:constant"), constant.type());
    }

    @Test
    public void testUniformFloatProviderFromJson() throws IOException {
        String json = """
                {
                    "type": "minecraft:uniform",
                    "value": {
                        "min_inclusive": 0.0,
                        "max_exclusive": 1.0
                    }
                }
                """;

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        FloatProvider provider = FloatProvider.fromJson(reader);
        assertNotNull(provider);
        assertTrue(provider instanceof FloatProvider.Uniform);
        
        FloatProvider.Uniform uniform = (FloatProvider.Uniform) provider;
        assertEquals(0.0f, uniform.value().min_inclusive(), 0.001f);
        assertEquals(1.0f, uniform.value().max_exclusive(), 0.001f);
        assertEquals(Key.key("minecraft:uniform"), uniform.type());
    }

    @Test
    public void testClampedNormalFloatProviderFromJson() throws IOException {
        String json = """
                {
                    "type": "minecraft:clamped_normal",
                    "value": {
                        "mean": 0.5,
                        "deviation": 0.1,
                        "min": 0.0,
                        "max": 1.0
                    }
                }
                """;

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        FloatProvider provider = FloatProvider.fromJson(reader);
        assertNotNull(provider);
        assertTrue(provider instanceof FloatProvider.ClampedNormal);
        
        FloatProvider.ClampedNormal clampedNormal = (FloatProvider.ClampedNormal) provider;
        assertEquals(0.5f, clampedNormal.value().mean(), 0.001f);
        assertEquals(0.1f, clampedNormal.value().deviation(), 0.001f);
        assertEquals(0.0f, clampedNormal.value().min(), 0.001f);
        assertEquals(1.0f, clampedNormal.value().max(), 0.001f);
        assertEquals(Key.key("minecraft:clamped_normal"), clampedNormal.type());
    }

    @Test
    public void testTrapezoidFloatProviderFromJson() throws IOException {
        String json = """
                {
                    "type": "minecraft:trapezoid",
                    "value": {
                        "min": 0.0,
                        "max": 2.0,
                        "plateau": 1.0
                    }
                }
                """;

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        FloatProvider provider = FloatProvider.fromJson(reader);
        assertNotNull(provider);
        assertTrue(provider instanceof FloatProvider.Trapezoid);
        
        FloatProvider.Trapezoid trapezoid = (FloatProvider.Trapezoid) provider;
        assertEquals(0.0f, trapezoid.value().min(), 0.001f);
        assertEquals(2.0f, trapezoid.value().max(), 0.001f);
        assertEquals(1.0f, trapezoid.value().plateau(), 0.001f);
        assertEquals(Key.key("minecraft:trapezoid"), trapezoid.type());
    }

    @Test
    public void testCodecStructureCompiles() {
        // Test that all codec constants are accessible and properly defined
        assertNotNull(FloatProvider.CODEC);
        assertNotNull(FloatProvider.Constant.CODEC);
        assertNotNull(FloatProvider.Uniform.CODEC);
        assertNotNull(FloatProvider.Uniform.Value.CODEC);
        assertNotNull(FloatProvider.ClampedNormal.CODEC);
        assertNotNull(FloatProvider.ClampedNormal.Value.CODEC);
        assertNotNull(FloatProvider.Trapezoid.CODEC);
        assertNotNull(FloatProvider.Trapezoid.Value.CODEC);
    }

    @Test
    public void testUnknownTypeThrowsException() {
        String json = """
                {
                    "type": "minecraft:unknown_float_provider",
                    "value": 5.0
                }
                """;

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        assertThrows(IOException.class, () -> FloatProvider.fromJson(reader));
    }
}