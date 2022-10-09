package net.minestom.vanilla.blockupdatesystem;

public interface BlockUpdateInfo {

    static DestroyBlock DESTROY_BLOCK() {
        return new DestroyBlock();
    }

    static PlaceBlock PLACE_BLOCK() {
        return new PlaceBlock();
    }

    static ChunkLoad CHUNK_LOAD() {
        return new ChunkLoad();
    }

    static MoveBlock MOVE_BLOCK() {
        return new MoveBlock();
    }

    record DestroyBlock() implements BlockUpdateInfo {
    }

    record PlaceBlock() implements BlockUpdateInfo {
    }

    record ChunkLoad() implements BlockUpdateInfo {
    }

    record MoveBlock() implements BlockUpdateInfo {
    }
}
