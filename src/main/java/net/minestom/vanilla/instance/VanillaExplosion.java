package net.minestom.vanilla.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
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
import net.minestom.vanilla.damage.DamageTypes;
import net.minestom.vanilla.math.RayCast;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VanillaExplosion extends Explosion {

    public static final String DROP_EVERYTHING_KEY = "minestom:drop_everything";
    public static final String IS_FLAMING_KEY = "minestom:is_flaming";
    private static final Random explosionRNG = new Random();

    private final boolean startsFires;
    private final boolean dropsEverything;

    public static final String THREAD_POOL_NAME = "MSVanilla-Explosion";
    public static final int THREAD_POOL_COUNT = 2;
    private static final MinestomThread threadPool = new MinestomThread(THREAD_POOL_COUNT, THREAD_POOL_NAME);
    private final Position center;

    public VanillaExplosion(float centerX, float centerY, float centerZ, float strength, boolean startsFires, boolean dropsEverything) {
        super(centerX, centerY, centerZ, strength);
        this.center = new Position(centerX, centerY, centerZ);
        this.startsFires = startsFires;
        this.dropsEverything = dropsEverything;
    }

    @Override
    protected List<BlockPosition> prepare(Instance instance) {
        Set<BlockPosition> positions = new HashSet<>();

        float stepLength = 0.3f;
        float maximumBlastRadius = (float) Math.floor(1.3f*getStrength()/(stepLength*0.75))*stepLength;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if(!(x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15)) { // must be on outer edge of 16x16x16 cube
                        continue;
                    }
                    Vector ray = new Vector(x-8.5f, y-8.5f, z-8.5f);
                    ray.normalize().multiply(stepLength);
                    float intensity = (0.7f + explosionRNG.nextFloat() * 0.6f) * getStrength();

                    Vector position = new Vector(getCenterX(), getCenterY(), getCenterZ());
                    BlockPosition blockPos = new BlockPosition(position);
                    for (float step = 0f; step < maximumBlastRadius; step += stepLength) {
                        intensity -= 0.225f; // air attenuation

                        blockPos.setX((int) Math.floor(position.getX()));
                        blockPos.setY((int) Math.floor(position.getY()));
                        blockPos.setZ((int) Math.floor(position.getZ()));

                        if(blockPos.getY() < 0 || blockPos.getY() >= 255) { // out of bounds
                            break;
                        }

                        Block block = Block.fromId(instance.getBlockId(blockPos));
                        CustomBlock customBlock = instance.getCustomBlock(blockPos);

                        float blastResistance = 0.05f; // TODO: custom blast resistances
                        intensity -= (blastResistance+stepLength)*stepLength;
                        if(intensity < 0f) {
                            break;
                        }


                        if(!positions.contains(blockPos)) {
                            positions.add(new BlockPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                        }

                        position.add(ray.getX(), ray.getY(), ray.getZ());
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

        for(BlockPosition position : positions) {
            Block block = Block.fromId(instance.getBlockId(position));
            CustomBlock customBlock = instance.getCustomBlock(position);

            if(block.isAir())
                continue;
            Data lootTableArguments = new Data();
            if(!dropsEverything) {
                lootTableArguments.set("explosionPower", (double)getStrength(), Double.class);
            }
            if(customBlock != null) {
                if(!customBlock.onExplode(instance, position, lootTableArguments)) {
                    continue;
                }
            }
            double p = explosionRNG.nextDouble();
            boolean shouldDropItem = p <= 1/getStrength();
            if(shouldDropItem || dropsEverything) {
                LootTableManager lootTableManager = MinecraftServer.getLootTableManager();
                try {
                    LootTable table = null;
                    if(customBlock != null) {
                        table = customBlock.getLootTable(lootTableManager);
                    }
                    if(table == null) {
                        table = lootTableManager.load(NamespaceID.from("blocks/"+block.name().toLowerCase()));
                    }
                    List<ItemStack> output = table.generate(lootTableArguments);
                    for (ItemStack out : output) {
                        ItemEntity itemEntity = new ItemEntity(out);
                        itemEntity.getPosition().setX(position.getX()+explosionRNG.nextFloat());
                        itemEntity.getPosition().setY(position.getY()+explosionRNG.nextFloat());
                        itemEntity.getPosition().setZ(position.getZ()+explosionRNG.nextFloat());
                        itemEntity.setPickupDelay(500L);
                        itemEntity.setInstance(instance);
                    }
                } catch (FileNotFoundException e) {
                    // loot table does not exist, ignore
                }
            }
        }

        return new LinkedList<>(positions);
    }

    private void affect(Entity e, final float damageRadius) {
        float exposure = calculateExposure(e, damageRadius);
        float distance = e.getPosition().getDistance(center);
        double impact = (1.0-distance/damageRadius)*exposure;
        double damage = Math.floor((impact*impact+impact)*7*getStrength()+1);
        if(e instanceof LivingEntity) {
            ((LivingEntity) e).damage(DamageTypes.EXPLOSION, (float)damage);
        } else {
            // TODO: different entities will react differently (items despawn, boats, minecarts drop as items, etc.)
        }

        float blastProtection = 0f; // TODO
        exposure -= exposure * 0.15f * blastProtection;
        Vector velocityBoost = e.getPosition().toVector().add(0f, e.getEyeHeight(), 0f).subtract(center.toVector());
        velocityBoost.normalize().multiply(exposure*MinecraftServer.TICK_PER_SECOND);
        e.setVelocity(e.getVelocity().clone().add(velocityBoost));
    }

    private float calculateExposure(Entity e, final float damageRadius) {
        int w = (int) (Math.floor(e.getBoundingBox().getWidth()*2))+1;
        int h = (int) (Math.floor(e.getBoundingBox().getHeight()*2))+1;
        int d = (int) (Math.floor(e.getBoundingBox().getDepth()*2))+1;

        Instance instance = e.getInstance();
        Position pos = e.getPosition();
        float entX = pos.getX();
        float entY = pos.getY();
        float entZ = pos.getZ();
        int hits = 0;
        int rays = w*h*d;
        for(int dx = (int) -Math.ceil(w/2); dx<Math.floor(w/2); dx++) {
            for(int dy = 0; dy<h; dy++) {
                for(int dz = (int) -Math.ceil(d/2); dz<Math.floor(d/2); dz++) {
                    float deltaX = entX+dx-getCenterX();
                    float deltaY = entY+dy-getCenterY();
                    float deltaZ = entZ+dz-getCenterZ();
                    RayCast.Result result = RayCast.rayCastBlocks(instance, getCenterX(), getCenterY(), getCenterZ(),
                            deltaX, deltaY, deltaZ,
                            (float) Math.sqrt(deltaX*deltaX+deltaY*deltaY+deltaZ*deltaZ), 0.3f,
                            position -> instance.getBlockId(position) == Block.AIR.getBlockId(),
                            _pos -> {});
                    if(result.getHitType() != RayCast.HitType.BLOCK) {
                        hits++;
                    }
                }
            }
        }
        return (float)hits / rays;
    }

    private List<Entity> getEntitiesAround(Instance instance, double damageRadius) {
        int intRadius = (int) Math.ceil(damageRadius);
        List<Entity> affected = new LinkedList<>();
        double radiusSq = damageRadius*damageRadius;
        for (int x = -intRadius; x <= intRadius; x++) {
            for (int z = -intRadius; z <= intRadius; z++) {
                int posX = (int) Math.floor(getCenterX()+x);
                int posZ = (int) Math.floor(getCenterZ()+z);
                var list = instance.getChunkEntities(instance.getChunk(posX >> 4, posZ >> 4));
                if(list != null) {
                    for(Entity e : list) {
                        float dx = e.getPosition().getX()-getCenterX();
                        float dy = e.getPosition().getY()-getCenterY();
                        float dz = e.getPosition().getZ()-getCenterZ();
                        if(dx*dx+dy*dy+dz*dz <= radiusSq) {
                            if(!affected.contains(e)) {
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
