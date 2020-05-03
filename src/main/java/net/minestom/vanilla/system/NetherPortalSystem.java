package net.minestom.vanilla.system;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.world.Dimension;
import net.minestom.vanilla.blocks.VanillaBlocks;

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
public final class NetherPortalSystem {

    private static final int MINIMUM_WIDTH = 2;
    private static final int MINIMUM_HEIGHT = 3;
    private static final int MAXIMUM_HEIGHT = 22;
    private static final int MAXIMUM_WIDTH = 22;

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
          //  System.out.println("considering "+position);
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

            Block block = Block.fromId(instance.getBlockId(position.getX(), position.getY(), position.getZ()));
            if(!block.isAir() && block != Block.FIRE && block != Block.NETHER_PORTAL)
                continue;

            insideFrame.add(position);

            BlockPosition above = position.clone().add(0, +1, 0);
            BlockPosition below = position.clone().add(0, -1, 0);
            BlockPosition left = position.clone().add(-1*axis.xMultiplier, 0, -1*axis.zMultiplier);
            BlockPosition right = position.clone().add(1*axis.xMultiplier, 0, 1*axis.zMultiplier);

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
            Block frameBlock = Block.fromId(instance.getBlockId(x, minY, z));
            if(frameBlock != Block.OBSIDIAN)
                return false;

            // top
            frameBlock = Block.fromId(instance.getBlockId(x, maxY, z));
            if(frameBlock != Block.OBSIDIAN)
                return false;
        }

        // left and right
        for (int j = 1; j < height-1; j++) {
            int x = minX;
            int z = minZ;

            // left
            Block frameBlock = Block.fromId(instance.getBlockId(x, minY+j, z));
            if(frameBlock != Block.OBSIDIAN)
                return false;

            if(axis == Axis.X) {
                x += width-1;
            } else {
                z += width-1;
            }

            // right
            frameBlock = Block.fromId(instance.getBlockId(x, minY+j, z));
            if(frameBlock != Block.OBSIDIAN)
                return false;
        }
        return true;
    }

    /**
     * Detailed informations about a nether portal
     * One should not hold such an instance for too long, this does not check that the obsidian is still present when
     * trying to fill the frame (only checks against fire and air blocks)
     */
    public static class NetherPortal {

        /**
         * Only NORTH and WEST are valid
         */
        private Axis axis;
        private BlockPosition frameTopLeftCorner;
        private BlockPosition frameBottomRightCorner;

        public NetherPortal(Axis axis, BlockPosition frameBottomRightCorner, BlockPosition frameTopLeftCorner) {
            this.axis = axis;
            this.frameBottomRightCorner = frameBottomRightCorner;
            this.frameTopLeftCorner = frameTopLeftCorner;
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

        public boolean isStillValid(Instance instance) {
            return checkFrameIsObsidian(instance, axis, frameBottomRightCorner, frameTopLeftCorner);
        }

        public void breakFrame(Instance instance) {
            replaceFrameContents(instance, pos -> {
                instance.setBlock(pos, Block.AIR);
            });
        }

        public boolean tryFillFrame(Instance instance) {
            if(instance.getDimension() == Dimension.END)
                return false;
            if(!VanillaBlocks.NETHER_PORTAL.isRegistered()) {
                return false;
            }

            return replaceFrameContents(instance, pos -> {
                instance.setCustomBlock(pos.getX(), pos.getY(), pos.getZ(), VanillaBlocks.NETHER_PORTAL.getInstance().getBaseBlockState().with("axis", axis.toString()).getBlockId());
            });
        }

        private boolean replaceFrameContents(Instance instance, Consumer<BlockPosition> blockPlacer) {
            int minX = Math.min(frameTopLeftCorner.getX(), frameBottomRightCorner.getX());
            int minY = frameBottomRightCorner.getY();
            int minZ = Math.min(frameTopLeftCorner.getZ(), frameBottomRightCorner.getZ());

            int maxX = Math.max(frameTopLeftCorner.getX(), frameBottomRightCorner.getX());
            int maxY = frameTopLeftCorner.getY();
            int maxZ = Math.max(frameTopLeftCorner.getZ(), frameBottomRightCorner.getZ());

            int width = (maxX-minX)*axis.xMultiplier + (maxZ-minZ)*axis.zMultiplier; // encompasses frame blocks

            if(!checkInsideFrameForAir(instance, minX, maxX, minY, maxY, minZ, maxZ, axis)) {
                return false;
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
                Block currentBlock = Block.fromId(instance.getBlockId(x, y, z));
                if(!currentBlock.isAir() && currentBlock != Block.FIRE && currentBlock != Block.NETHER_PORTAL) {
                    return false;
                }
            }
        }
        return true;
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
