package net.minestom.vanilla.system;

import net.minestom.server.data.Data;
import net.minestom.server.data.DataManager;
import net.minestom.server.effects.Effects;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Vector;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.blockentity.NetherPortalBlockEntity;
import net.minestom.vanilla.blocks.NetherPortalBlock;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.data.NetherPortalDataType;
import net.minestom.vanilla.data.NetherPortalList;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

/**
 * Every useful method linked to Nether portals goes here
 * TODO: Could be repurposed to create custom portals
 *
 * @author jglrxavpok
 */
public final class NetherPortal {

    private static final int MINIMUM_WIDTH = 2;
    private static final int MINIMUM_HEIGHT = 3;
    private static final int MAXIMUM_HEIGHT = 22;
    private static final int MAXIMUM_WIDTH = 22;

    public static final String LIST_KEY = "minestom:nether_portals";

    /**
     * Only NORTH and WEST are valid
     */
    private Axis axis;
    private BlockPosition frameTopLeftCorner;
    private BlockPosition frameBottomRightCorner;
    private Vector averagePosition;

    /**
     * Prevents considering this portal as non-valid during generation (otherwise portals may try to break themselves when
     * they are being placed due to neighbor updates of portal blocks)
     */
    private boolean generating;

    public static final NetherPortal NONE = new NetherPortal(Axis.X, new BlockPosition(0, -1, 0), new BlockPosition(0, -1, 0));

    public NetherPortal(Axis axis, BlockPosition frameBottomRightCorner, BlockPosition frameTopLeftCorner) {
        this.axis = axis;
        this.frameBottomRightCorner = frameBottomRightCorner;
        this.frameTopLeftCorner = frameTopLeftCorner;
        this.averagePosition = new Vector(frameBottomRightCorner.getX(), frameBottomRightCorner.getY(), frameBottomRightCorner.getZ());
        this.averagePosition.add(frameTopLeftCorner.getX(), frameTopLeftCorner.getY(), frameTopLeftCorner.getZ());
        this.averagePosition.setX(this.averagePosition.getX()/2);
        this.averagePosition.setY(this.averagePosition.getY()/2);
        this.averagePosition.setZ(this.averagePosition.getZ()/2);
    }

    public Axis getAxis() {
        return axis;
    }

    public BlockPosition getFrameBottomRightCorner() {
        return frameBottomRightCorner;
    }

    public BlockPosition getFrameTopLeftCorner() {
        return frameTopLeftCorner;
    }

    /**
     * Position of the center of the frame
     * Used to compute closest portal when linking portals
     * @return
     */
    public Vector getCenter() {
        return averagePosition;
    }

    public boolean isStillValid(Instance instance) {
        return generating || checkFrameIsObsidian(instance, axis, frameBottomRightCorner, frameTopLeftCorner);
    }

    public void breakFrame(Instance instance) {
        replaceFrameContents(instance, true, pos -> {
            instance.setBlock(pos, Block.AIR);

            // play break animation for each portal block
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            ParticlePacket particlePacket = ParticleCreator.createParticlePacket(Particle.BLOCK, false,
                    x + 0.5f, y, z + 0.5f,
                    0.4f, 0.5f, 0.4f,
                    0.3f, 10, writer -> {
                        writer.writeVarInt(Block.NETHER_PORTAL.getBlockId());
                    });

            EffectPacket effectPacket = new EffectPacket();
            effectPacket.effectId = Effects.BLOCK_BREAK.getId();
            effectPacket.position = pos;
            effectPacket.data = Block.NETHER_PORTAL.getBlockId();

            Chunk chunk = instance.getChunkAt(pos);
            if(chunk != null) {
                chunk.sendPacketToViewers(particlePacket);
                chunk.sendPacketToViewers(effectPacket);
            }
        });
    }

    public boolean tryFillFrame(Instance instance) {
        if(instance.getDimensionType() == VanillaDimensionTypes.END)
            return false;
        if(!VanillaBlocks.NETHER_PORTAL.isRegistered()) {
            return false;
        }

        NetherPortalBlock portalBlock = (NetherPortalBlock) VanillaBlocks.NETHER_PORTAL.getInstance();
        return replaceFrameContents(instance, true, pos -> {
            NetherPortalBlockEntity data = (NetherPortalBlockEntity) portalBlock.createData(instance, pos, null);
            data.setRelatedPortal(this);
            instance.setSeparateBlocks(pos.getX(), pos.getY(), pos.getZ(), portalBlock.getBaseBlockState().with("axis", axis.toString()).getBlockId(), portalBlock.getCustomBlockId(), data);
        });
    }

    /**
     *
     * @param instance
     * @param checkPreviousBlocks should check if frame is full of air/portal/fire
     * @param blockPlacer
     * @return
     */
    private boolean replaceFrameContents(Instance instance, boolean checkPreviousBlocks, Consumer<BlockPosition> blockPlacer) {
        int minX = Math.min(frameTopLeftCorner.getX(), frameBottomRightCorner.getX());
        int minY = frameBottomRightCorner.getY();
        int minZ = Math.min(frameTopLeftCorner.getZ(), frameBottomRightCorner.getZ());

        int maxX = Math.max(frameTopLeftCorner.getX(), frameBottomRightCorner.getX());
        int maxY = frameTopLeftCorner.getY();
        int maxZ = Math.max(frameTopLeftCorner.getZ(), frameBottomRightCorner.getZ());

        int width = computeWidth()-1; // encompasses frame blocks

        if(checkPreviousBlocks) {
            if(!checkInsideFrameForAir(instance, minX, maxX, minY, maxY, minZ, maxZ, axis)) {
                return false;
            }
        }

        BlockPosition pos = new BlockPosition(0,0,0);
        // fill nether portal
        for (int i = 1; i <= width-1; i++) {
            for(int y = minY+1; y <= maxY-1; y++) {
                int x = minX;
                int z = minZ;
                if(axis == Axis.X) {
                    x += i;
                } else {
                    z += i;
                }
                pos.setX(x);
                pos.setY(y);
                pos.setZ(z);
                blockPlacer.accept(pos);
            }
        }
        return true;
    }


    /**
     * Register necessary data types to the given DataManager
     */
    public static void registerData(DataManager dataManager) {
        dataManager.registerType(NetherPortal.class, new NetherPortalDataType());
        dataManager.registerType(NetherPortalList.class, new NetherPortalList.DataType());
    }

    /**
     * Gets a {@link NetherPortal} frame description from a block that would be contained inside the frame.
     * @param instance the instance to draw blocks from
     * @param frameBlock the position of the potential future frame block
     * @return null if no valid frame was found, a new {@link NetherPortal} instance with detailed info otherwise
     */
    public static NetherPortal findPortalFrameFromFrameBlock(Instance instance, BlockPosition frameBlock) {
        NetherPortal alongAxisX = findPortalFrameFromFrameBlock(instance, frameBlock, Axis.X);
        if(alongAxisX != null)
            return alongAxisX;
        return findPortalFrameFromFrameBlock(instance, frameBlock, Axis.Z);
    }


    private static NetherPortal findPortalFrameFromFrameBlock(Instance instance, BlockPosition frameBlock, Axis axis) {
        List<BlockPosition> insideFrame = new LinkedList<>();
        List<BlockPosition> considered = new LinkedList<>();
        Queue<BlockPosition> neighbors = new LinkedBlockingDeque<>();
        neighbors.add(frameBlock);

        while(!neighbors.isEmpty()) {
            BlockPosition position = neighbors.poll();
            considered.add(position);

            int xDistance = Math.abs(position.getX() - frameBlock.getX());
            int zDistance = Math.abs(position.getZ() - frameBlock.getZ());

            int height = Math.abs(position.getY()-frameBlock.getY());
            int width = xDistance * axis.xMultiplier + zDistance * axis.zMultiplier;
            if(width >= MAXIMUM_WIDTH) {
                continue;
            }
            if(height >= MAXIMUM_HEIGHT) {
                continue;
            }

            Block block = Block.fromStateId(instance.getBlockStateId(position.getX(), position.getY(), position.getZ()));
            if(!block.isAir() && block != Block.FIRE && block != Block.NETHER_PORTAL)
                continue;

            insideFrame.add(position);

            BlockPosition above = position.copy().add(0, +1, 0);
            BlockPosition below = position.copy().add(0, -1, 0);
            BlockPosition left = position.copy().add(-1*axis.xMultiplier, 0, -1*axis.zMultiplier);
            BlockPosition right = position.copy().add(1*axis.xMultiplier, 0, 1*axis.zMultiplier);

            if(!considered.contains(above) && !neighbors.contains(above)) {
                neighbors.add(above);
            }
            if(!considered.contains(below) && !neighbors.contains(below)) {
                neighbors.add(below);
            }
            if(!considered.contains(left) && !neighbors.contains(left)) {
                neighbors.add(left);
            }
            if(!considered.contains(right) && !neighbors.contains(right)) {
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

        for(BlockPosition framePosition : insideFrame) {
            int x = framePosition.getX();
            int y = framePosition.getY();
            int z = framePosition.getZ();

            if(x < minX) {
                minX = x;
            }
            if(y < minY) {
                minY = y;
            }
            if(z < minZ) {
                minZ = z;
            }

            if(x > maxX) {
                maxX = x;
            }
            if(y > maxY) {
                maxY = y;
            }
            if(z > maxZ) {
                maxZ = z;
            }
        }

        boolean isRectangleOfAir = checkInsideFrameForAir(instance, minX, maxX, minY, maxY, minZ, maxZ, axis);
        if(!isRectangleOfAir)
            return null;

        int width = (maxX-minX)*axis.xMultiplier + (maxZ-minZ)*axis.zMultiplier +1; // does not encompass frame
        int height = maxY-minY +1;

        if(width < MINIMUM_WIDTH) { // too narrow
            return null;
        }

        if(height < MINIMUM_HEIGHT) { // too small
            return null;
        }

        BlockPosition bottomRight = new BlockPosition(0, minY-1, 0);
        BlockPosition topLeft = new BlockPosition(0, maxY+1, 0);
        switch (axis) {
            case X:
                bottomRight.setX(maxX+1);
                bottomRight.setZ(minZ);
                topLeft.setX(minX-1);
                topLeft.setZ(minZ);
                break;
            case Z:
                bottomRight.setZ(maxZ+1);
                bottomRight.setX(minX);
                topLeft.setZ(minZ-1);
                topLeft.setX(minX);
                break;
        }

        // TODO: check that frame is obsidian
        if(!checkFrameIsObsidian(instance, axis, bottomRight, topLeft)) {
            return null;
        }

        return new NetherPortal(axis, bottomRight, topLeft);
    }

    private static boolean checkFrameIsObsidian(Instance instance, Axis axis, BlockPosition bottomRightCorner, BlockPosition topLeftCorner) {
        int minX = Math.min(topLeftCorner.getX(), bottomRightCorner.getX());
        int minY = bottomRightCorner.getY();
        int minZ = Math.min(topLeftCorner.getZ(), bottomRightCorner.getZ());

        int maxX = Math.max(topLeftCorner.getX(), bottomRightCorner.getX());
        int maxY = topLeftCorner.getY();
        int maxZ = Math.max(topLeftCorner.getZ(), bottomRightCorner.getZ());

        int width = (maxX-minX)*axis.xMultiplier + (maxZ-minZ)*axis.zMultiplier +1; // encompasses frame blocks
        int height = maxY - minY +1;

        // offsets by one are used to ignore portal corners

        // top and bottom
        for (int i = 1; i < width-1; i++) {
            int x = minX;
            int z = minZ;
            if(axis == Axis.X) {
                x += i;
            } else {
                z += i;
            }

            // bottom
            Block frameBlock = Block.fromStateId(instance.getBlockStateId(x, minY, z));
            if(frameBlock != Block.OBSIDIAN)
                return false;

            // top
            frameBlock = Block.fromStateId(instance.getBlockStateId(x, maxY, z));
            if(frameBlock != Block.OBSIDIAN)
                return false;
        }

        // left and right
        for (int j = 1; j < height-1; j++) {
            int x = minX;
            int z = minZ;

            // left
            Block frameBlock = Block.fromStateId(instance.getBlockStateId(x, minY+j, z));
            if(frameBlock != Block.OBSIDIAN)
                return false;

            if(axis == Axis.X) {
                x += width-1;
            } else {
                z += width-1;
            }

            // right
            frameBlock = Block.fromStateId(instance.getBlockStateId(x, minY+j, z));
            if(frameBlock != Block.OBSIDIAN)
                return false;
        }
        return true;
    }


    private static boolean checkInsideFrameForAir(Instance instance, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Axis axis) {
        int width = (maxX-minX)*axis.xMultiplier + (maxZ-minZ)*axis.zMultiplier;
        for (int i = 1; i <= width-1; i++) {
            for(int y = minY+1; y <= maxY-1; y++) {
                int x = minX;
                int z = minZ;
                if(axis == Axis.X) {
                    x += i;
                } else {
                    z += i;
                }
                Block currentBlock = Block.fromStateId(instance.getBlockStateId(x, y, z));
                if(!currentBlock.isAir() && currentBlock != Block.FIRE && currentBlock != Block.NETHER_PORTAL) {
                    return false;
                }
            }
        }
        return true;
    }

    public void unregister(Instance instance) {
        if(instance.getData() != null) {
            Data data = instance.getData();
            NetherPortalList list = data.getOrDefault(LIST_KEY, null);
            if(list != null) {
                list.remove(this);
            }
        }
    }

    public void register(Instance instance) {
        if(instance.getData() != null) {
            Data data = instance.getData();
            NetherPortalList list = data.getOrDefault(LIST_KEY, null);
            if(list == null) {
                NetherPortalList newList = new NetherPortalList();
                data.set(LIST_KEY, newList, NetherPortalList.class);
                list = newList;
            }

            list.add(this);
        }
    }

    public void generate(Instance instance) {
        generating = true;
        NetherPortalBlock portalBlock = (NetherPortalBlock) VanillaBlocks.NETHER_PORTAL.getInstance();
        loadAround(instance, frameTopLeftCorner);
        loadAround(instance, frameBottomRightCorner);
        createFrame(instance);
        replaceFrameContents(instance, false, pos -> {
            NetherPortalBlockEntity data = (NetherPortalBlockEntity) portalBlock.createData(instance, pos, null);
            data.setRelatedPortal(this);
            instance.setSeparateBlocks(pos.getX(), pos.getY(), pos.getZ(), portalBlock.getBaseBlockState().with("axis", axis.toString()).getBlockId(), portalBlock.getCustomBlockId(), data);
        });
        register(instance);
        generating = false;
    }

    /**
     * Ensure chunks around the portal corner are loaded (3x3 area centered on chunk containing frame corner)
     * @param instance
     * @param corner
     */
    private void loadAround(Instance instance, BlockPosition corner) {
        int chunkX = corner.getX() >> 4;
        int chunkZ = corner.getZ() >> 4;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                instance.loadChunk(chunkX+x, chunkZ+z);
            }
        }
    }

    private void createFrame(Instance instance) {
        int minX = Math.min(frameTopLeftCorner.getX(), frameBottomRightCorner.getX());
        int minY = frameBottomRightCorner.getY();
        int minZ = Math.min(frameTopLeftCorner.getZ(), frameBottomRightCorner.getZ());

        int maxY = frameTopLeftCorner.getY();

        int width = computeWidth(); // encompasses frame blocks
        int height = computeHeight();

        // top and bottom
        for (int i = 0; i < width; i++) {
            int x = minX;
            int z = minZ;
            if(axis == Axis.X) {
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
            instance.setBlock(x, minY+j, z, Block.OBSIDIAN);

            if(axis == Axis.X) {
                x += width-1;
            } else {
                z += width-1;
            }

            // right
            instance.setBlock(x, minY+j, z, Block.OBSIDIAN);
        }
    }

    public int computeWidth() {
        int minX = Math.min(frameTopLeftCorner.getX(), frameBottomRightCorner.getX());
        int minZ = Math.min(frameTopLeftCorner.getZ(), frameBottomRightCorner.getZ());

        int maxX = Math.max(frameTopLeftCorner.getX(), frameBottomRightCorner.getX());
        int maxZ = Math.max(frameTopLeftCorner.getZ(), frameBottomRightCorner.getZ());
        return (maxX-minX)*axis.xMultiplier + (maxZ-minZ)*axis.zMultiplier +1;
    }

    public int computeHeight() {
        int minY = frameBottomRightCorner.getY();
        int maxY = frameTopLeftCorner.getY();
        return maxY-minY+1;
    }

    public enum Axis {
        X(1,0),
        Z(0,1);

        public final int xMultiplier;
        public final int zMultiplier;

        private Axis(int xMultiplier, int zMultiplier) {
            this.xMultiplier = xMultiplier;
            this.zMultiplier = zMultiplier;
        }


        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
