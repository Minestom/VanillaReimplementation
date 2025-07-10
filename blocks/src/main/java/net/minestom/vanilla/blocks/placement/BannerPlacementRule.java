package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.utils.DirectionUtils;

public class BannerPlacementRule extends BlockPlacementRule {

    public BannerPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (placementState.blockFace() == null || placementState.blockFace() == BlockFace.BOTTOM)
            return null;

        if (placementState.blockFace() == BlockFace.TOP) {
            int rotation = (DirectionUtils.sixteenStepRotation(placementState) + 8) % 16;
            return block.withProperty("rotation", String.valueOf(rotation));
        }

        Block wallBanner;
        var material = placementState.block().registry().material();

        if (material.equals(Block.ORANGE_BANNER.registry().material())) {
            wallBanner = Block.ORANGE_WALL_BANNER;
        } else if (material.equals(Block.MAGENTA_BANNER.registry().material())) {
            wallBanner = Block.MAGENTA_WALL_BANNER;
        } else if (material.equals(Block.LIGHT_BLUE_BANNER.registry().material())) {
            wallBanner = Block.LIGHT_BLUE_WALL_BANNER;
        } else if (material.equals(Block.YELLOW_BANNER.registry().material())) {
            wallBanner = Block.YELLOW_WALL_BANNER;
        } else if (material.equals(Block.LIME_BANNER.registry().material())) {
            wallBanner = Block.LIME_WALL_BANNER;
        } else if (material.equals(Block.PINK_BANNER.registry().material())) {
            wallBanner = Block.PINK_WALL_BANNER;
        } else if (material.equals(Block.GRAY_BANNER.registry().material())) {
            wallBanner = Block.GRAY_WALL_BANNER;
        } else if (material.equals(Block.LIGHT_GRAY_BANNER.registry().material())) {
            wallBanner = Block.LIGHT_GRAY_WALL_BANNER;
        } else if (material.equals(Block.CYAN_BANNER.registry().material())) {
            wallBanner = Block.CYAN_WALL_BANNER;
        } else if (material.equals(Block.PURPLE_BANNER.registry().material())) {
            wallBanner = Block.PURPLE_WALL_BANNER;
        } else if (material.equals(Block.BLUE_BANNER.registry().material())) {
            wallBanner = Block.BLUE_WALL_BANNER;
        } else if (material.equals(Block.BROWN_BANNER.registry().material())) {
            wallBanner = Block.BROWN_WALL_BANNER;
        } else if (material.equals(Block.GREEN_BANNER.registry().material())) {
            wallBanner = Block.GREEN_WALL_BANNER;
        } else if (material.equals(Block.RED_BANNER.registry().material())) {
            wallBanner = Block.RED_WALL_BANNER;
        } else if (material.equals(Block.BLACK_BANNER.registry().material())) {
            wallBanner = Block.BLACK_WALL_BANNER;
        } else if (material.equals(Block.WHITE_BANNER.registry().material())) {
            wallBanner = Block.WHITE_WALL_BANNER;
        } else {
            return null;
        }

        return wallBanner.withNbt(placementState.block().nbtOrEmpty())
            .withProperty("facing", placementState.blockFace().name().toLowerCase());
    }
}
