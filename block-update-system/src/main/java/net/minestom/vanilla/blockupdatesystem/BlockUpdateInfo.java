package net.minestom.vanilla.blockupdatesystem;

import net.minestom.server.coordinate.Point;

public interface BlockUpdateInfo {

    static DestroyBlock DESTROY_BLOCK(Point destroyedBlock) {
        return new DestroyBlock(destroyedBlock);
    }

    static PlaceBlock PLACE_BLOCK(Point placedBlock) {
        return new PlaceBlock(placedBlock);
    }

    static MoveBlock MOVE_BLOCK(Point movedBlock) {
        return new MoveBlock(movedBlock);
    }

    record DestroyBlock(Point destroyedBlock) implements BlockUpdateInfo {}

    record PlaceBlock(Point placedBlock) implements BlockUpdateInfo {}

    record MoveBlock(Point movingBlock) implements BlockUpdateInfo {}
}
