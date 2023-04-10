package net.minestom.vanilla.datapack.worldgen;

import io.github.pesto.files.ByteArray;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DataVersion: Data version of the NBT structure.
 * author: Name of the player who created this structure. Only exists for structures saved before 1.13.
 * size: 3 TAG_Int describing the size of the structure.
 * palette: Set of different block states used in the structure.
 * A block.
 * Name: Block ID.
 * Properties: List of block state properties, with [name] being the name of the block state property.
 * Name: The block state name and its value.
 * palettes: Sets of different block states used in the structure, a random palette gets selected based on coordinates. Used in vanilla by shipwrecks.
 * A set of different block states used in the structure.
 * A block.
 * Name: Block ID.
 * Properties: List of block state properties, with [name] being the name of the block state property.
 * Name: The block state name and its value.
 * blocks: List of individual blocks in the structure.
 * An individual block.
 * state: Index of the block in the palette.
 * pos: 3 TAG_Int describing the position of this block.
 * nbt: NBT of the associated block entity (optional, only present if the block has one). Does not contain x, y, or z fields. See Block entity format.
 * entities: List of entities in the structure.
 * An entity.
 * pos: 3 TAG_Double describing the exact position of the entity.
 * blockPos: 3 TAG_Int describing the block position of the entity.
 * nbt: NBT of the entity (required). See entity format.
 *
 * @param DataVersion Data version of the NBT structure.
 * @param author      Name of the player who created this structure. Only exists for structures saved before 1.13.
 * @param size        3 TAG_Int describing the size of the structure.
 * @param palette     Set of different block states used in the structure.
 * @param palettes    Sets of different block states used in the structure, a random palette gets selected based on coordinates. Used in vanilla by shipwrecks.
 * @param blocks      List of individual blocks in the structure.
 * @param entities    List of entities in the structure.
 */
public record Structure(int DataVersion, @Nullable String author, Point size,
                        @Nullable Set<BlockState> palette, @UnknownNullability Set<Set<BlockState>> palettes,
                        List<Block> blocks, List<Entity> entities) {
    public static Structure fromInput(ByteArray content) {
        try (NBTReader reader = new NBTReader(content.toStream())) {

            NBTCompound root = (NBTCompound) reader.read();
            String str = root.toSNBT();
            Objects.requireNonNull(root);

            int DataVersion = Objects.requireNonNull(root.getInt("DataVersion"));
            @Nullable String author = root.getString("author");

            NBTList<NBTInt> nbt_size = Objects.requireNonNull(root.getList("size"));
            NBTList<NBTCompound> nbt_palette = root.getList("palette");
            NBTList<NBTList<NBTCompound>> nbt_palettes = root.getList("palettes");
            NBTList<NBTCompound> nbt_blocks = Objects.requireNonNull(root.getList("blocks"));
            NBTList<NBTCompound> nbt_entities = Objects.requireNonNull(root.getList("entities"));

            Point size = parsePoint(nbt_size);
            // Only one of "palette" OR "palettes" is present
            Set<BlockState> palette = nbt_palette == null ? null : parsePalette(nbt_palette);
            Set<Set<BlockState>> palettes = nbt_palettes == null ? null : parsePalettes(nbt_palettes);
            List<Block> blocks = parseBlocks(nbt_blocks);
            List<Entity> entities = parseEntities(nbt_entities);

            return new Structure(DataVersion, author, size, palette, palettes, blocks, entities);
        } catch (IOException | NBTException e) {
            throw new RuntimeException(e);
        }
    }

    private static Point parsePoint(NBTList<NBTInt> nbtSize) {
        return new Vec(nbtSize.get(0).getValue(), nbtSize.get(1).getValue(), nbtSize.get(2).getValue());
    }

    private static Point parseDoublePoint(NBTList<NBTDouble> nbtSize) {
        return new Vec(nbtSize.get(0).getValue(), nbtSize.get(1).getValue(), nbtSize.get(2).getValue());
    }

    private static BlockState parseBlockState(NBTCompound block) {
        String blockId = Objects.requireNonNull(block.getString("Name"));
        NBTCompound properties = block.getCompound("Properties");
        if (properties == null) {
            return new BlockState(blockId, Map.of());
        }
        return new BlockState(
                blockId,
                properties
                        .asMapView().entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey(), (NBTString) entry.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue()))
        );
    }

    private static Set<BlockState> parsePalette(NBTList<NBTCompound> nbtPalette) {
        return nbtPalette.asListView().stream()
                .map(Structure::parseBlockState)
                .collect(Collectors.toSet());
    }

    private static Set<Set<BlockState>> parsePalettes(NBTList<NBTList<NBTCompound>> nbtPalettes) {
        return nbtPalettes.asListView().stream()
                .map(palette -> palette.asListView().stream()
                        .map(Structure::parseBlockState)
                        .collect(Collectors.toSet()))
                .collect(Collectors.toSet());
    }

    private static List<Block> parseBlocks(NBTList<NBTCompound> nbtBlocks) {
        return nbtBlocks.asListView().stream()
                .map(block -> new Block(
                        Objects.requireNonNull(block.getInt("state")),
                        parsePoint(Objects.requireNonNull(block.getList("pos"))),
                        block.get("nbt")
                ))
                .collect(Collectors.toList());
    }

    private static List<Entity> parseEntities(NBTList<NBTCompound> nbtEntities) {
        return nbtEntities.asListView().stream()
                .map(entity -> new Entity(
                        parseDoublePoint(Objects.requireNonNull(entity.getList("pos"))),
                        parsePoint(Objects.requireNonNull(entity.getList("blockPos"))),
                        Objects.requireNonNull(entity.getCompound("nbt"))
                ))
                .collect(Collectors.toList());
    }

    public record Block(int state, Point pos, @Nullable NBT nbt) {
    }

    public record Entity(Point pos, Point blockPos, NBT nbt) {
    }
}
