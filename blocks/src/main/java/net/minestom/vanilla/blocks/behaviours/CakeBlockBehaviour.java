package net.minestom.vanilla.blocks.behaviours;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.vanilla.blocks.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.VanillaBlocks;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static java.util.Map.entry;

public class CakeBlockBehaviour extends VanillaBlockBehaviour {

    private static final Map<Block, Material> candleCakes = Map.ofEntries(
            entry(Block.CANDLE_CAKE, Material.CANDLE),
            entry(Block.WHITE_CANDLE_CAKE, Material.WHITE_CANDLE),
            entry(Block.ORANGE_CANDLE_CAKE, Material.ORANGE_CANDLE),
            entry(Block.MAGENTA_CANDLE_CAKE, Material.MAGENTA_CANDLE),
            entry(Block.LIGHT_BLUE_CANDLE_CAKE, Material.LIGHT_BLUE_CANDLE),
            entry(Block.YELLOW_CANDLE_CAKE, Material.YELLOW_CANDLE),
            entry(Block.LIME_CANDLE_CAKE, Material.LIME_CANDLE),
            entry(Block.PINK_CANDLE_CAKE, Material.PINK_CANDLE),
            entry(Block.GRAY_CANDLE_CAKE, Material.GRAY_CANDLE),
            entry(Block.LIGHT_GRAY_CANDLE_CAKE, Material.LIGHT_GRAY_CANDLE),
            entry(Block.CYAN_CANDLE_CAKE, Material.CYAN_CANDLE),
            entry(Block.PURPLE_CANDLE_CAKE, Material.PURPLE_CANDLE),
            entry(Block.BLUE_CANDLE_CAKE, Material.BLUE_CANDLE),
            entry(Block.BROWN_CANDLE_CAKE, Material.BROWN_CANDLE),
            entry(Block.GREEN_CANDLE_CAKE, Material.GREEN_CANDLE),
            entry(Block.BLACK_CANDLE_CAKE, Material.BLACK_CANDLE)
    );

    private static final ItemStack flint_and_steel = ItemStack.of(Material.FLINT_AND_STEEL);

    public CakeBlockBehaviour(VanillaBlocks.@NotNull BlockContext context) {
        super(context);
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        Player player = interaction.getPlayer();
        Block block = interaction.getBlock();
        Point point = interaction.getBlockPosition();
        Instance instance = interaction.getInstance();

        int food = player.getFood();
        float saturation = player.getFoodSaturation();
        ItemStack item = player.getItemInMainHand();

        // Player is trying to light candle cake
        if (item.isSimilar(flint_and_steel) && candleCakes.containsKey(block) &&
                !Boolean.parseBoolean(block.getProperty("lit"))) {
            instance.setBlock(point, block.withProperty("lit", "true"));

            // TODO: Handle tool durability
            return true;
        }

        // Player is eating cake
        if (food < 20) {
            tryDropCandle(block, instance, point);

            // Update hunger values
            int newFood = Math.max(20, food + 2);
            float newSaturation = Math.max(20f, saturation + 0.4f);
            player.setFood(newFood);
            player.setFoodSaturation(newSaturation);
        }
        return true;
    }

    @Override
    public void onDestroy(@NotNull Destroy destroy) {
        Block block = destroy.getBlock();
        Instance instance = destroy.getInstance();
        Point point = destroy.getBlockPosition();

        tryDropCandle(block, instance, point);
    }

    private void tryDropCandle(Block block, Instance instance, Point point) {
        if (block != Block.CAKE) {
            instance.setBlock(point, Block.CAKE.withProperty("bites", "1"));
            ItemStack candle = ItemStack.of(candleCakes.get(block));
            new ItemEntity(candle).setInstance(instance);
        }
    }
}