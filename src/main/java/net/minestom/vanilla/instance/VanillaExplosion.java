package net.minestom.vanilla.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataImpl;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.thread.MinestomThread;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.damage.DamageTypes;
import net.minestom.vanilla.math.RayCast;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VanillaExplosion extends Explosion {

    public static final String DROP_EVERYTHING_KEY = "minestom:drop_everything";
    public static final String IS_FLAMING_KEY = "minestom:is_flaming";
    public static final String DONT_DESTROY_BLOCKS_KEY = "minestom:no_block_damage";
    private static final Random explosionRNG = new Random();

    private final boolean startsFires;
    private final boolean dropsEverything;

    public static final String THREAD_POOL_NAME = "MSVanilla-Explosion";
    public static final int THREAD_POOL_COUNT = 2;
    private static final MinestomThread threadPool = new MinestomThread(THREAD_POOL_COUNT, THREAD_POOL_NAME);
    private final Position center;
    private final boolean blockDamage;

    public VanillaExplosion(float centerX, float centerY, float centerZ, float strength, boolean startsFires, boolean dropsEverything, boolean blockDamage) {
        super(centerX, centerY, centerZ, strength);
        this.blockDamage = blockDamage;
        this.center = new Position(centerX, centerY, centerZ);
        this.startsFires = startsFires;
        this.dropsEverything = dropsEverything;
    }

    @Override
    protected List<BlockPosition> prepare(Instance instance) {
        float stepLength = 0.3f;
        float maximumBlastRadius = (float) Math.floor(1.3f * getStrength() / (stepLength * 0.75)) * stepLength;
        Set<BlockPosition> positions = new HashSet<>();
        if (blockDamage) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (!(x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15)) { // must be on outer edge of 16x16x16 cube
                            continue;
                        }
                        Vector ray = new Vector(x - 8.5f, y - 8.5f, z - 8.5f);
                        ray.normalize().multiply(stepLength);

                        Predicate<BlockPosition> shouldContinue = new Predicate<BlockPosition>() {
                            private float intensity = (0.7f + explosionRNG.nextFloat() * 0.6f) * getStrength();

                            @Override
                            public boolean test(BlockPosition position) {
                                intensity -= 0.225f; // air attenuation

                                Block block = Block.fromStateId(instance.getBlockStateId(position));
                                CustomBlock customBlock = instance.getCustomBlock(position);

                                double blastResistance = block.getResistance(); // TODO: custom blast resistances
                                intensity -= (blastResistance + stepLength) * stepLength;
                                return intensity > 0f;
                            }
                        };

                        RayCast.rayCastBlocks(instance, getCenterX(), getCenterY(), getCenterZ(),
                                x - 8.5f, y - 8.5f, z - 8.5f, maximumBlastRadius, stepLength,
                                shouldContinue, blockPos -> {
                                    positions.add(new BlockPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                                }
                        );
                    }
                }
            }
        }

        final float damageRadius = maximumBlastRadius; // TODO: should be different from blast radius
        List<Entity> potentiallyDamagedEntities = getEntitiesAround(instance, damageRadius);

        try {
            threadPool.invokeAll(potentiallyDamagedEntities.stream().map(ent -> (Callable<Void>) () -> {
                affect(ent, damageRadius);
                return null;
            }).collect(Collectors.toList()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (blockDamage) {
            for (BlockPosition position : positions) {
                Block block = Block.fromStateId(instance.getBlockStateId(position));
                CustomBlock customBlock = instance.getCustomBlock(position);

                if (block.isAir()) {
                    continue;
                }
                Data lootTableArguments = new DataImpl();
                if (!dropsEverything) {
                    lootTableArguments.set("explosionPower", (double) getStrength(), Double.class);
                }
                if (customBlock != null) {
                    if (!customBlock.onExplode(instance, position, lootTableArguments)) {
                        continue;
                    }
                }
                double p = explosionRNG.nextDouble();
                boolean shouldDropItem = p <= 1 / getStrength();
                if (shouldDropItem || dropsEverything) {
                    LootTableManager lootTableManager = MinecraftServer.getLootTableManager();
                    try {
                        LootTable table = null;
                        if (customBlock != null) {
                            table = customBlock.getLootTable(lootTableManager);
                        }
                        if (table == null) {
                            table = lootTableManager.load(NamespaceID.from("blocks/" + block.name().toLowerCase()));
                        }
                        List<ItemStack> output = table.generate(lootTableArguments);
                        for (ItemStack out : output) {
                            ItemEntity itemEntity = new ItemEntity(out, new Position(position.getX() + explosionRNG.nextFloat(), position.getY() + explosionRNG.nextFloat(), position.getZ() + explosionRNG.nextFloat()));
                            itemEntity.setPickupDelay(500L, TimeUnit.MILLISECOND);
                            itemEntity.setInstance(instance);
                        }
                    } catch (FileNotFoundException e) {
                        // loot table does not exist, ignore
                    }
                }
            }
        }

        return new LinkedList<>(positions);
    }

    @Override
    protected void postSend(Instance instance, List<BlockPosition> blocks) {
        if (!startsFires) {
            return;
        }
        BlockPosition belowPos = new BlockPosition(0, 0, 0);
        for (BlockPosition position : blocks) {
            Block block = Block.fromStateId(instance.getBlockStateId(position));

            if (block.isAir() && position.getY() > 0) {
                if (explosionRNG.nextFloat() < 1 / 3f) {
                    belowPos.setX(position.getX());
                    belowPos.setY(position.getY() - 1);
                    belowPos.setZ(position.getZ());
                    // check that block below is solid
                    Block below = Block.fromStateId(instance.getBlockStateId(belowPos));
                    if (below.isSolid()) {
                        instance.setSeparateBlocks(position.getX(), position.getY(), position.getZ(), Block.FIRE.getBlockId(), VanillaBlocks.FIRE.getInstance().getCustomBlockId());
                    }
                }
            }
        }
    }

    private void affect(Entity e, final float damageRadius) {
        float exposure = calculateExposure(e, damageRadius);
        float distance = e.getPosition().getDistance(center);
        double impact = (1.0 - distance / damageRadius) * exposure;
        double damage = Math.floor((impact * impact + impact) * 7 * getStrength() + 1);
        if (e instanceof LivingEntity) {
            ((LivingEntity) e).damage(DamageTypes.EXPLOSION, (float) damage);
        } else {
            if (e instanceof ItemEntity) {
                e.scheduleRemove(1L, TimeUnit.TICK);
            }
            // TODO: different entities will react differently (items despawn, boats, minecarts drop as items, etc.)
        }

        float blastProtection = 0f; // TODO: apply enchantments
        exposure -= exposure * 0.15f * blastProtection;
        Vector velocityBoost = e.getPosition().toVector().add(0f, e.getEyeHeight(), 0f).subtract(center.toVector());
        velocityBoost.normalize().multiply(exposure * MinecraftServer.TICK_PER_SECOND);
        e.setVelocity(e.getVelocity().copy().add(velocityBoost));
    }

    private float calculateExposure(Entity e, final float damageRadius) {
        int w = (int) (Math.floor(e.getBoundingBox().getWidth() * 2)) + 1;
        int h = (int) (Math.floor(e.getBoundingBox().getHeight() * 2)) + 1;
        int d = (int) (Math.floor(e.getBoundingBox().getDepth() * 2)) + 1;

        Instance instance = e.getInstance();
        Position pos = e.getPosition();
        float entX = pos.getX();
        float entY = pos.getY();
        float entZ = pos.getZ();
        int hits = 0;
        int rays = w * h * d;
        for (int dx = (int) -Math.ceil(w / 2); dx < Math.floor(w / 2); dx++) {
            for (int dy = 0; dy < h; dy++) {
                for (int dz = (int) -Math.ceil(d / 2); dz < Math.floor(d / 2); dz++) {
                    float deltaX = entX + dx - getCenterX();
                    float deltaY = entY + dy - getCenterY();
                    float deltaZ = entZ + dz - getCenterZ();
                    RayCast.Result result = RayCast.rayCastBlocks(instance, getCenterX(), getCenterY(), getCenterZ(),
                            deltaX, deltaY, deltaZ,
                            (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ), 0.3f,
                            position -> instance.getBlockStateId(position) == Block.AIR.getBlockId(),
                            _pos -> {
                            });
                    if (result.getHitType() != RayCast.HitType.BLOCK) {
                        hits++;
                    }
                }
            }
        }
        return (float) hits / rays;
    }

    private List<Entity> getEntitiesAround(Instance instance, double damageRadius) {
        int intRadius = (int) Math.ceil(damageRadius);
        List<Entity> affected = new LinkedList<>();
        double radiusSq = damageRadius * damageRadius;
        for (int x = -intRadius; x <= intRadius; x++) {
            for (int z = -intRadius; z <= intRadius; z++) {
                int posX = (int) Math.floor(getCenterX() + x);
                int posZ = (int) Math.floor(getCenterZ() + z);
                var list = instance.getChunkEntities(instance.getChunk(posX >> 4, posZ >> 4));
                if (list != null) {
                    for (Entity e : list) {
                        float dx = e.getPosition().getX() - getCenterX();
                        float dy = e.getPosition().getY() - getCenterY();
                        float dz = e.getPosition().getZ() - getCenterZ();
                        if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                            if (!affected.contains(e)) {
                                affected.add(e);
                            }
                        }
                    }
                }
            }
        }
        return affected;
    }

}
