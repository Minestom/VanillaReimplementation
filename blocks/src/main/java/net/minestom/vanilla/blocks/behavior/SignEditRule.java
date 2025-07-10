package net.minestom.vanilla.blocks.behavior;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.play.OpenSignEditorPacket;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import net.minestom.server.tag.Tag;
import net.minestom.vanilla.blocks.event.PlayerOpenSignEditorEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class SignEditRule implements BlockHandler {
    private final Block block;
    private final @Nullable RegistryTag<Block> wallSigns = Block.staticRegistry().getTag(TagKey.ofHash("#minecraft:wall_signs"));

    public SignEditRule(Block block) {
        this.block = block;
    }

    @Override
    public Key getKey() {
        return block.key();
    }

    @Override
    public Collection<Tag<?>> getBlockEntityTags() {
        return Arrays.asList(
            Tag.NBT("front_text"),
            Tag.NBT("back_text"),
            Tag.Boolean("is_waxed")
        );
    }

    @Override
    public void onPlace(Placement placement) {
        if (!(placement instanceof PlayerPlacement)) return;
        PlayerPlacement playerPlacement = (PlayerPlacement) placement;
        if (playerPlacement.getPlayer().isSneaking()) return;

        Player player = playerPlacement.getPlayer();
        if (wallSigns.contains(playerPlacement.getBlock())) {
            openEditor(playerPlacement.getBlock(), playerPlacement.getBlockPosition(), player, true);
        } else {
            Point position = playerPlacement.getBlockPosition();
            String rotationProperty = playerPlacement.getBlock().getProperty("rotation");
            int rotation = rotationProperty != null ? Integer.parseInt(rotationProperty) : 0;
            boolean front = getSide(player, position, rotation);
            openEditor(playerPlacement.getBlock(), position, player, front);
        }
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        if (interaction.getPlayer().isSneaking()) return BlockHandler.super.onInteract(interaction);

        Block block = interaction.getBlock();
        Point position = interaction.getBlockPosition();

        if (wallSigns.contains(block)) {
            String rotationProperty = block.getProperty("rotation");
            int rotation = rotationProperty != null ? Integer.parseInt(rotationProperty) : 0;
            boolean front = getSide(interaction.getPlayer(), position, rotation);
            openEditor(block, position, interaction.getPlayer(), front);
        } else {
            String rotationProperty = block.getProperty("rotation");
            int rotation = rotationProperty != null ? Integer.parseInt(rotationProperty) : 0;
            boolean side = getSide(interaction.getPlayer(), position, rotation);
            openEditor(block, position, interaction.getPlayer(), side);
        }

        return false;
    }

    private boolean getSide(Player player, Point position, int rotation) {
        double angleInRadians = Math.atan2(
            player.getPosition().x() - position.x(),
            position.z() - player.getPosition().z()
        );
        int playerAngle = (int) Math.toDegrees(angleInRadians);
        double signAngle = rotation * 22.5;
        int relativeDegrees = (playerAngle - (int)signAngle + 360) % 360;
        return relativeDegrees >= 0 && relativeDegrees <= 180;
    }

    private void openEditor(Block block, Point position, Player player, boolean front) {
        EventDispatcher.callCancellable(
            new PlayerOpenSignEditorEvent(player, new BlockVec(position), block),
            () -> player.sendPacket(new OpenSignEditorPacket(position, front))
        );
    }

    // TODO: Write back updated signs
}
