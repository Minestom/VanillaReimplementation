package net.minestom.vanilla.instance;

import dev.emortal.rayfast.area.Intersection;
import dev.emortal.rayfast.area.area3d.Area3d;
import dev.emortal.rayfast.casting.grid.GridCast;
import dev.emortal.rayfast.vector.Vector3d;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.vanilla.dimensions.damage.DamageTypes;

import java.util.*;

public class VanillaExplosion extends Explosion {

    public static final String DROP_EVERYTHING_KEY = "minestom:drop_everything";
    public static final String IS_FLAMING_KEY = "minestom:is_flaming";
    public static final String DONT_DESTROY_BLOCKS_KEY = "minestom:no_block_damage";
    private static final Random explosionRNG = new Random();

    private final boolean startsFires;
    private final boolean dropsEverything;

    public static final String THREAD_POOL_NAME = "MSVanilla-Explosion";
    public static final int THREAD_POOL_COUNT = 2;
    private final Point center;
    private final boolean blockDamage;

    protected VanillaExplosion(Point center, float strength, boolean dropEverything, boolean isFlaming, boolean dontDestroyBlocks) {
        super((float) center.x(), (float) center.y(), (float) center.z(), strength);
        this.center = center;
        this.blockDamage = dropEverything;
        this.startsFires = isFlaming;
        this.dropsEverything = dontDestroyBlocks;
    }

    public static Builder builder(Point center, float strength) {
        return new Builder(center, strength);
    }

    @Override
    protected List<Point> prepare(Instance instance) {
        float maximumBlastRadius = getStrength();
        Set<Point> positions = new HashSet<>();

        if (blockDamage) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (!(x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15)) { // must be on outer edge of 16x16x16 cube
                            continue;
                        }

                        Vec dir = new Vec(x - 8.5f, y - 8.5f, z - 8.5f).normalize();

                        Iterator<Vector3d> gridIterator = GridCast.createGridIterator(getCenterX(), getCenterY(),
                                getCenterZ(), dir.x(), dir.y(), dir.z(), 1.0, maximumBlastRadius);

                        double intensity = (0.7f + explosionRNG.nextFloat() * 0.6f) * getStrength();

                        while (gridIterator.hasNext()) {
                            Vector3d vec = gridIterator.next();
                            Point pos = new Vec(vec.x(), vec.y(), vec.z());

                            intensity -= 0.225;

                            Block block = instance.loadOptionalChunk(pos).join().getBlock(pos);

                            double explosionResistance = block.registry().explosionResistance();
                            intensity -= (explosionResistance / 5.0);

                            if (intensity < 0) {
                                break;
                            }

                            positions.add(pos);
                        }
                    }
                }
            }
        }

        final float damageRadius = maximumBlastRadius; // TODO: should be different from blast radius
        List<Entity> potentiallyDamagedEntities = getEntitiesAround(instance, damageRadius);

        for (Entity entity : potentiallyDamagedEntities) {
            affect(entity, damageRadius);
        }

        if (blockDamage) {
            for (Point position : positions) {
                Block block = instance.getBlock(position);

                if (block.isAir()) {
                    continue;
                }

//                if (block.compare(Block.TNT)) {
//                    spawnPrimedTNT(instance, position, new Pos(getCenterX(), getCenterY(), getCenterZ()));
//                    continue;
//                }

//                if (customBlock != null) {
//                    if (!customBlock.onExplode(instance, position, lootTableArguments)) {
//                        continue;
//                    }
//                }

                double p = explosionRNG.nextDouble();
                boolean shouldDropItem = p <= 1 / getStrength();

                if (dropsEverything || shouldDropItem) {
//                    LootTableManager lootTableManager = MinecraftServer.getLootTableManager();
//                    try {
//                        LootTable table = null;
//                        if (customBlock != null) {
//                            table = customBlock.getLootTable(lootTableManager);
//                        }
//                        if (table == null) {
//                            table = lootTableManager.load(NamespaceID.from("blocks/" + block.name().toLowerCase()));
//                        }
//                        List<ItemStack> output = table.generate(lootTableArguments);
//                        for (ItemStack out : output) {
//                            ItemEntity itemEntity = new ItemEntity(out, new Position(position.getX() + explosionRNG.nextFloat(), position.getY() + explosionRNG.nextFloat(), position.getZ() + explosionRNG.nextFloat()));
//                            itemEntity.setPickupDelay(500L, TimeUnit.MILLISECOND);
//                            itemEntity.setInstance(instance);
//                        }
//                    } catch (FileNotFoundException e) {
//                        // loot table does not exist, ignore
//                    }
                }
            }
        }

        return new LinkedList<>(positions);
    }

//    private void spawnPrimedTNT(Instance instance, Point blockPosition, Point explosionSource) {
//        Pos initialPosition = new Pos(blockPosition.blockX() + 0.5f, blockPosition.blockY() + 0f, blockPosition.blockZ() + 0.5f);
//
//        PrimedTNT primedTNT = new PrimedTNT(10 + (TNTBlockHandler.TNT_RANDOM.nextInt(5) - 2));
//        primedTNT.setInstance(instance);
//        primedTNT.teleport(initialPosition);
//
//        Point direction = blockPosition.sub(explosionSource);
//        double distance = explosionSource.distanceSquared(blockPosition);
//        Vec vec = new Vec(direction.x(), direction.y(), direction.z());
//        vec = vec.div(distance);
//
//        primedTNT.setVelocity(vec.mul(15));
//    }

    @Override
    protected void postSend(Instance instance, List<Point> blocks) {
        if (!startsFires) {
            return;
        }

        AbsoluteBlockBatch batch = new AbsoluteBlockBatch();

        for (Point position : blocks) {
            Block block = instance.getBlock(position);

            if (block.isAir() && position.y() > 0) {
                if (explosionRNG.nextFloat() < 1 / 3f) {

                    Point belowPos = position.add(0, -1, 0);

                    // check that block below is solid
                    Block below = instance.getBlock(belowPos);

                    if (below.isSolid()) {
                        batch.setBlock(position, Block.FIRE);
                    }
                }
            }
        }

        batch.apply(instance, null);
    }

    private void affect(Entity e, final float damageRadius) {
        double exposure = calculateExposure(e, damageRadius);
        double distance = e.getPosition().distance(center);
        double impact = (1.0 - distance / damageRadius) * exposure;
        double damage = Math.floor((impact * impact + impact) * 7 * getStrength() + 1);

        if (e instanceof LivingEntity) {
            ((LivingEntity) e).damage(DamageTypes.EXPLOSION, (float) damage);
        } else {
            if (e instanceof ItemEntity) {
                e.scheduleRemove(1L, TimeUnit.SERVER_TICK);
            }
            // TODO: different entities will react differently (items despawn, boats, minecarts drop as items, etc.)
        }

        float blastProtection = 0f; // TODO: apply enchantments

        exposure -= exposure * 0.15f * blastProtection;

        Vec velocityBoost = e.getPosition().asVec().add(0f, e.getEyeHeight(), 0f).sub(center);

        velocityBoost = velocityBoost.normalize().mul(exposure * MinecraftServer.TICK_PER_SECOND);
        e.setVelocity(e.getVelocity().add(velocityBoost));
    }

    private float calculateExposure(Entity e, final float damageRadius) {
        int w = (int) (Math.floor(e.getBoundingBox().width() * 2)) + 1;
        int h = (int) (Math.floor(e.getBoundingBox().height() * 2)) + 1;
        int d = (int) (Math.floor(e.getBoundingBox().depth() * 2)) + 1;

        Instance instance = e.getInstance();
        Pos pos = e.getPosition();
        double entX = pos.x();
        double entY = pos.y();
        double entZ = pos.z();

        // Generate entity hitbox
        Area3d area3d = Area3d.CONVERTER.from(e);

        int hits = 0;
        int rays = w * h * d;

        int wd2 = w / 2;
        int dd2 = d / 2;

        for (int dx = (int) -Math.ceil(wd2); dx < Math.floor(wd2); dx++) {
            for (int dy = 0; dy < h; dy++) {
                for (int dz = (int) -Math.ceil(dd2); dz < Math.floor(dd2); dz++) {
                    double deltaX = entX + dx - getCenterX();
                    double deltaY = entY + dy - getCenterY();
                    double deltaZ = entZ + dz - getCenterZ();

                    // TODO: Check for distance
                    Vector3d intersection = area3d.lineIntersection(getCenterX(), getCenterY(), getCenterZ(),
                            deltaX, deltaY, deltaZ, Intersection.ANY_3D);

                    if (intersection != null) {
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

                for (Entity e : list) {
                    Pos pos = e.getPosition();
                    double dx = pos.x() - getCenterX();
                    double dy = pos.y() - getCenterY();
                    double dz = pos.z() - getCenterZ();

                    if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                        if (!affected.contains(e)) {
                            affected.add(e);
                        }
                    }
                }
            }
        }

        return affected;
    }

    public void trigger(Instance instance) {
        this.apply(instance);
    }

    public static class Builder {

        private final Point center;
        private final float strength;

        private boolean dropEverything = true;
        private boolean isFlaming = false;
        private boolean dontDestroyBlocks = false;

        protected Builder(Point center, float strength) {
            this.center = center;
            this.strength = strength;
        }

        public Builder dropEverything(boolean dropEverything) {
            this.dropEverything = dropEverything;
            return this;
        }

        public Builder isFlaming(boolean isFlaming) {
            this.isFlaming = isFlaming;
            return this;
        }

        public Builder destroyBlocks(boolean dontDestroyBlocks) {
            this.dontDestroyBlocks = !dontDestroyBlocks;
            return this;
        }

        public VanillaExplosion build() {
            return new VanillaExplosion(center, strength, dropEverything, isFlaming, dontDestroyBlocks);
        }
    }
}