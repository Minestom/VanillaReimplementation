package net.minestom.vanilla.system;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.effects.Effects;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Every useful method linked to Nether portals goes here
 * TODO: Could be repurposed to create custom portals
 *
 * @author jglrxavpok
 */
@SuppressWarnings("UnstableApiUsage")
public final class NetherPortal {

    private static final int MINIMUM_WIDTH = 2;
    private static final int MINIMUM_HEIGHT = 3;
    private static final int MAXIMUM_HEIGHT = 22;
    private static final int MAXIMUM_WIDTH = 22;

    private static long nextID = 0;
    private static final Map<Long, NetherPortal> portalsById = new HashMap<>();

    /**
     * Only NORTH and WEST are valid
     */
    private final Axis axis;
    private final Point frameTopLeftCorner;
    private final Point frameBottomRightCorner;
    private final Vec averagePosition;
    private final long id;

    /**
     * Prevents considering this portal as non-valid during generation (otherwise portals may try to break themselves when
     * they are being placed due to neighbor updates of portal blocks)
     */
    private boolean generating;

    public static final NetherPortal NONE = new NetherPortal(Axis.X, new Pos(0, -1, 0), new Pos(0, -1, 0));

    public static @Nullable NetherPortal fromId(@Nullable Long id) {
        return portalsById.get(id);
    }

    public NetherPortal(Axis axis, Point frameBottomRightCorner, Point frameTopLeftCorner) {
        this.axis = axis;
        this.frameBottomRightCorner = frameBottomRightCorner;
        this.frameTopLeftCorner = frameTopLeftCorner;
        this.averagePosition = new Vec(
                (frameBottomRightCorner.x() + frameTopLeftCorner.x()) / 2D,
                (frameBottomRightCorner.y() + frameTopLeftCorner.y()) / 2D,
                (frameBottomRightCorner.z() + frameTopLeftCorner.z()) / 2D
        );

        this.id = nextID++;
        portalsById.put(this.id, this);
    }

    public Axis getAxis() {
        return axis;
    }

    public Point getFrameBottomRightCorner() {
        return frameBottomRightCorner;
    }

    public Point getFrameTopLeftCorner() {
        return frameTopLeftCorner;
    }

    /**
     * Position of the center of the frame
     * Used to compute the closest portal when linking portals
     */
    public Vec getCenter() {
        return averagePosition;
    }

    public boolean isStillValid(Instance instance) {
        return generating || checkFrameIsObsidian(instance, axis, frameBottomRightCorner, frameTopLeftCorner);
    }

    public void breakFrame(Instance instance) {
        Set<Point> blockPositions = new HashSet<>();
        replaceFrameContents(instance, true, Block.AIR, blockPositions);

        for (Point pos : blockPositions) {
            // play break animation for each portal block
            int x = pos.blockX();
            int y = pos.blockY();
            int z = pos.blockZ();

            ParticlePacket particlePacket = new ParticlePacket(
                    Particle.BLOCK.withBlock(Block.NETHER_PORTAL),
                    x + 0.5f, y, z + 0.5f,
                    0.4f, 0.5f, 0.4f,
                    0.3f, 10
            );

            EffectPacket effectPacket = new EffectPacket(
                    Effects.BLOCK_BREAK.getId(),
                    pos,
                    Block.NETHER_PORTAL.id(),
                    false
            );

            Chunk chunk = instance.getChunkAt(pos);
            if (chunk != null) {
                chunk.sendPacketToViewers(particlePacket);
                chunk.sendPacketToViewers(effectPacket);
            }
        }
    }

    public boolean tryFillFrame(Instance instance) {
        if (instance.getDimensionType().namespace().equals(NamespaceID.from("the_end"))) {
            return false;
        }

        // Block to use
        Block block = Block.NETHER_PORTAL;// .withTag(NetherPortalBlockHandler.RELATED_PORTAL_KEY, this.id());

        return replaceFrameContents(instance, true, block, null);
    }

    /**
     * @param instance            the instance to build the frame
     * @param checkPreviousBlocks should check if frame is full of air/portal/fire
     * @param block               the block to place
     * @param blockPositions      the set to fill with block positions
     */
    private boolean replaceFrameContents(Instance instance, boolean checkPreviousBlocks, Block block, @Nullable Set<Point> blockPositions) {
        int minX = Math.min(frameTopLeftCorner.blockX(), frameBottomRightCorner.blockX());
        int minY = Math.min(frameBottomRightCorner.blockY(), frameTopLeftCorner.blockY());
        int minZ = Math.min(frameTopLeftCorner.blockZ(), frameBottomRightCorner.blockZ());

        int maxX = Math.max(frameTopLeftCorner.blockX(), frameBottomRightCorner.blockX());
        int maxY = Math.max(frameBottomRightCorner.blockY(), frameTopLeftCorner.blockY());
        int maxZ = Math.max(frameTopLeftCorner.blockZ(), frameBottomRightCorner.blockZ());

        int width = computeWidth() - 1; // encompasses frame blocks

        if (checkPreviousBlocks) {
            if (!checkInsideFrameForAir(instance, minX, maxX, minY, maxY, minZ, maxZ, axis)) {
                return false;
            }
        }

        // Fill portal
        int xMul = axis.xMultiplier;
        int zMul = axis.zMultiplier;

        for (int d = 1; d < width; d++) {
            for (int y = minY + 1; y <= maxY - 1; y++) {
                int x = minX + (d * xMul);
                int z = minZ + (d * zMul);

                instance.setBlock(x, y, z, block);

                if (blockPositions != null) {
                    blockPositions.add(new Pos(x, y, z));
                }
            }
        }
        return true;
    }

    /**
     * Gets a {@link NetherPortal} frame description from a block that would be contained inside the frame.
     *
     * @param instance the instance to draw blocks from
     * @param pos      the position of the potential future frame block
     * @return null if no valid frame was found, a new {@link NetherPortal} instance with detailed info otherwise
     */
    public static NetherPortal findPortalFrameFromFrameBlock(Instance instance, Point pos) {
        NetherPortal alongAxisX = findPortalFrameFromFrameBlock(instance, pos, Axis.X);
        if (alongAxisX != null)
            return alongAxisX;
        return findPortalFrameFromFrameBlock(instance, pos, Axis.Z);
    }


    private static NetherPortal findPortalFrameFromFrameBlock(Instance instance, Point frameBlock, Axis axis) {
        List<Point> insideFrame = new LinkedList<>();
        List<Point> considered = new LinkedList<>();
        Queue<Point> neighbors = new LinkedBlockingDeque<>();
        neighbors.add(frameBlock);

        while (!neighbors.isEmpty()) {
            Point position = neighbors.poll();
            considered.add(position);

            int xDistance = Math.abs(position.blockX() - frameBlock.blockX());
            int zDistance = Math.abs(position.blockZ() - frameBlock.blockZ());

            int height = Math.abs(position.blockY() - frameBlock.blockY());
            int width = xDistance * axis.xMultiplier + zDistance * axis.zMultiplier;
            if (width >= MAXIMUM_WIDTH) {
                continue;
            }

            if (height >= MAXIMUM_HEIGHT) {
                continue;
            }

            Block block = instance.getBlock(position);

            if (!block.isAir() &&
                    block != Block.FIRE &&
                    block != Block.NETHER_PORTAL
            ) {
                continue;
            }

            insideFrame.add(position);

            Point above = position.add(0, +1, 0);
            Point below = position.add(0, -1, 0);
            Point left = position.add(-1 * axis.xMultiplier, 0, -1 * axis.zMultiplier);
            Point right = position.add(axis.xMultiplier, 0, axis.zMultiplier);

            if (!considered.contains(above) && !neighbors.contains(above)) {
                neighbors.add(above);
            }

            if (!considered.contains(below) && !neighbors.contains(below)) {
                neighbors.add(below);
            }

            if (!considered.contains(left) && !neighbors.contains(left)) {
                neighbors.add(left);
            }

            if (!considered.contains(right) && !neighbors.contains(right)) {
                neighbors.add(right);
            }
        }

        // check that insideFrame represent a rectangle full of air
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (Point framePosition : insideFrame) {
            int x = framePosition.blockX();
            int y = framePosition.blockY();
            int z = framePosition.blockZ();

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }

        boolean isRectangleOfAir = checkInsideFrameForAir(instance, minX, maxX, minY, maxY, minZ, maxZ, axis);
        if (!isRectangleOfAir) {
            return null;
        }

        int width = (maxX - minX) * axis.xMultiplier + (maxZ - minZ) * axis.zMultiplier + 1; // does not encompass frame
        int height = maxY - minY + 1;

        if (width < MINIMUM_WIDTH) { // too narrow
            return null;
        }

        if (height < MINIMUM_HEIGHT) { // too small
            return null;
        }

        Pos bottomRight = null;
        Pos topLeft = null;
        switch (axis) {
            case X -> {
                bottomRight = new Pos(maxX + 1, minY - 1, minZ);
                topLeft = new Pos(minX - 1, maxY + 1, minZ);
            }
            case Z -> {
                bottomRight = new Pos(minX, minY - 1, maxZ + 1);
                topLeft = new Pos(minX, maxY + 1, minZ - 1);
            }
        }

        // TODO: check that frame is obsidian
        if (!checkFrameIsObsidian(instance, axis, bottomRight, topLeft)) {
            return null;
        }

        return new NetherPortal(axis, bottomRight, topLeft);
    }

    private static boolean checkFrameIsObsidian(Instance instance, Axis axis, Point bottomRightCorner, Point topLeftCorner) {
        int minX = Math.min(topLeftCorner.blockX(), bottomRightCorner.blockX());
        int minY = bottomRightCorner.blockY();
        int minZ = Math.min(topLeftCorner.blockZ(), bottomRightCorner.blockZ());

        int maxX = Math.max(topLeftCorner.blockX(), bottomRightCorner.blockX());
        int maxY = topLeftCorner.blockY();
        int maxZ = Math.max(topLeftCorner.blockZ(), bottomRightCorner.blockZ());

        int width = (maxX - minX) * axis.xMultiplier + (maxZ - minZ) * axis.zMultiplier + 1; // encompasses frame blocks
        int height = maxY - minY + 1;

        // offsets by one are used to ignore portal corners

        // top and bottom
        for (int i = 1; i < width - 1; i++) {
            int x = minX;
            int z = minZ;
            if (axis == Axis.X) {
                x += i;
            } else {
                z += i;
            }

            // bottom
            Block frameBlock = instance.getBlock(x, minY, z);
            if (!frameBlock.compare(Block.OBSIDIAN)) {
                return false;
            }

            // top
            frameBlock = instance.getBlock(x, maxY, z);
            if (!frameBlock.compare(Block.OBSIDIAN)) {
                return false;
            }
        }

        // left and right
        for (int j = 1; j < height - 1; j++) {
            int x = minX;
            int z = minZ;

            // left
            Block frameBlock = instance.getBlock(x, minY + j, z);
            if (!frameBlock.compare(Block.OBSIDIAN)) {
                return false;
            }

            if (axis == Axis.X) {
                x += width - 1;
            } else {
                z += width - 1;
            }

            // right
            frameBlock = instance.getBlock(x, minY + j, z);
            if (!frameBlock.compare(Block.OBSIDIAN)) {
                return false;
            }
        }
        return true;
    }


    private static boolean checkInsideFrameForAir(Instance instance, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Axis axis) {
        int width = (maxX - minX) * axis.xMultiplier + (maxZ - minZ) * axis.zMultiplier;

        for (int i = 1; i <= width - 1; i++) {
            for (int y = minY + 1; y <= maxY - 1; y++) {
                int x = minX;
                int z = minZ;
                if (axis == Axis.X) {
                    x += i;
                } else {
                    z += i;
                }
                Block currentBlock = instance.getBlock(x, y, z);
                if (!currentBlock.isAir() &&
                        (!currentBlock.compare(Block.FIRE)) &&
                        (!currentBlock.compare(Block.NETHER_PORTAL))
                ) {
                    return false;
                }
            }
        }
        return true;
    }

    public void unregister(Instance instance) {
//        instance.setTag(Utils.);
//        if (instance.getData() != null) {
//            Data data = instance.getData();
//            NetherPortalList list = data.getOrDefault(LIST_KEY, null);
//            if(list != null) {
//                list.remove(this);
//            }
//        }
    }

    public void register(Instance instance) {
//        if (instance.getData() != null) {
//            Data data = instance.getData();
//            NetherPortalList list = data.getOrDefault(LIST_KEY, null);
//            if(list == null) {
//                NetherPortalList newList = new NetherPortalList();
//                data.set(LIST_KEY, newList, NetherPortalList.class);
//                list = newList;
//            }
//
//            list.add(this);
//        }
    }

    public void generate(Instance instance) {
        generating = true;
        // NetherPortalBlockHandler portalBlock = (NetherPortalBlockHandler) Block.NETHER_PORTAL.handler();

        loadAround(instance, frameTopLeftCorner).join();
        loadAround(instance, frameBottomRightCorner).join();

        createFrame(instance);

        Block block = Block.NETHER_PORTAL; // .withTag(NetherPortalBlockHandler.RELATED_PORTAL_KEY, this.id());

        replaceFrameContents(instance, false, block, null);

        register(instance);
        generating = false;
    }

    /**
     * Ensure chunks around the portal corner are loaded (3x3 area centered on chunk containing frame corner)
     */
    private CompletableFuture<Void> loadAround(Instance instance, Point corner) {
        int chunkX = corner.blockX() >> 4;
        int chunkZ = corner.blockZ() >> 4;

        ObjectArrayList<CompletableFuture<Chunk>> futures = new ObjectArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                futures.add(instance.loadChunk(chunkX + x, chunkZ + z));
            }
        }

        return CompletableFuture.allOf(futures.elements());
    }

    private void createFrame(Instance instance) {
        int minX = Math.min(frameTopLeftCorner.blockX(), frameBottomRightCorner.blockX());
        int minY = frameBottomRightCorner.blockY();
        int minZ = Math.min(frameTopLeftCorner.blockZ(), frameBottomRightCorner.blockZ());

        int maxY = frameTopLeftCorner.blockY();

        int width = computeWidth(); // encompasses frame blocks
        int height = computeHeight();

        // top and bottom
        for (int i = 0; i < width; i++) {
            int x = minX;
            int z = minZ;
            if (axis == Axis.X) {
                x += i;
            } else {
                z += i;
            }

            // bottom
            instance.setBlock(x, minY, z, Block.OBSIDIAN);

            // top
            instance.setBlock(x, maxY, z, Block.OBSIDIAN);
        }

        // left and right
        for (int j = 0; j < height; j++) {
            int x = minX;
            int z = minZ;

            // left
            instance.setBlock(x, minY + j, z, Block.OBSIDIAN);

            if (axis == Axis.X) {
                x += width - 1;
            } else {
                z += width - 1;
            }

            // right
            instance.setBlock(x, minY + j, z, Block.OBSIDIAN);
        }
    }

    public int computeWidth() {
        int minX = Math.min(frameTopLeftCorner.blockX(), frameBottomRightCorner.blockX());
        int minZ = Math.min(frameTopLeftCorner.blockZ(), frameBottomRightCorner.blockZ());

        int maxX = Math.max(frameTopLeftCorner.blockX(), frameBottomRightCorner.blockX());
        int maxZ = Math.max(frameTopLeftCorner.blockZ(), frameBottomRightCorner.blockZ());
        return (maxX - minX) * axis.xMultiplier + (maxZ - minZ) * axis.zMultiplier + 1;
    }

    public int computeHeight() {
        int minY = frameBottomRightCorner.blockY();
        int maxY = frameTopLeftCorner.blockY();
        return maxY - minY + 1;
    }

    public long id() {
        return this.id;
    }

    public enum Axis {
        X(1, 0),
        Z(0, 1);

        public final int xMultiplier;
        public final int zMultiplier;

        Axis(int xMultiplier, int zMultiplier) {
            this.xMultiplier = xMultiplier;
            this.zMultiplier = zMultiplier;
        }


        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
