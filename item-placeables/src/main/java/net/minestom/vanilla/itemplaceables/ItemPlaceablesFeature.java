package net.minestom.vanilla.itemplaceables;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemPlaceablesFeature implements VanillaReimplementation.Feature {
    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {


        // TODO: Flesh this out and make it configurable
        Map<Material, Block> itemPlaceables = new ConcurrentHashMap<>();

        itemPlaceables.put(Material.WATER_BUCKET, Block.WATER);
        itemPlaceables.put(Material.LAVA_BUCKET, Block.LAVA);

        vri.process().eventHandler().addListener(PlayerUseItemOnBlockEvent.class, event -> {
            Point position = event.getPosition();
            var face = event.getBlockFace();
            ItemStack item = event.getItemStack();

            Block block = itemPlaceables.get(item.material());
            if (block == null) return;
            position = position.relative(face);
            event.getInstance().setBlock(position, block);
        });
    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("vri:item-placeables");
    }
}
