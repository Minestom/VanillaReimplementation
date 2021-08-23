package net.minestom.vanilla.blocks.update.info;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

public class BlockUpdateInfo {

    public static DestroyBlock DESTROY_BLOCK(Point destroyedBlock) {
        return new DestroyBlock(destroyedBlock);
    }

    public static PlaceBlock PLACE_BLOCK(Point placedBlock) {
        return new PlaceBlock(placedBlock);
    }

    public static MoveBlock MOVE_BLOCK(Point movedBlock) {
        return new MoveBlock(movedBlock);
    }

    // TODO: Records
    public static class DestroyBlock extends BlockUpdateInfo {
        private Point destroyedBlock;

        private DestroyBlock(Point destroyedBlock) {
            this.destroyedBlock = destroyedBlock;
        }

        public Point destroyedBlock() {
            return destroyedBlock;
        }
    }

    // TODO: Records
    public static class PlaceBlock extends BlockUpdateInfo {
        private Point placedBlock;

        private PlaceBlock(Point placedBlock) {
            this.placedBlock = placedBlock;
        }

        public Point placedBlock() {
            return placedBlock;
        }
    }

    // TODO: Records
    public static class MoveBlock extends BlockUpdateInfo {
        private Point movingBlock;

        private MoveBlock(Point movingBlock) {
            this.movingBlock = movingBlock;
        }

        public Point movingBlock() {
            return movingBlock;
        }
    }
}
