package net.minestom.vanilla.instance;

import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Vector;

import java.util.*;

public class VanillaExplosion extends Explosion {

    public static final String DROP_EVERYTHING_KEY = "minestom:drop_everything";
    public static final String IS_FLAMING_KEY = "minestom:is_flaming";
    private static final Random explosionRNG = new Random();

    private final boolean startsFires;
    private final boolean dropsEverything;

    public VanillaExplosion(float centerX, float centerY, float centerZ, float strength, boolean startsFires, boolean dropsEverything) {
        super(centerX, centerY, centerZ, strength);
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

                        float blastResistance = 0.05f; // TODO: custom blast resistances
                        intensity -= (blastResistance+stepLength)*stepLength;
                        if(intensity < 0f) {
                            break;
                        }

                        if(!positions.contains(blockPos)) {
                            // TODO: Drop item entities
                            // TODO: call onExplode callback on custom blocks
                            positions.add(new BlockPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                            System.out.println(blockPos);
                        }

                        position.add(ray.getX(), ray.getY(), ray.getZ());
                    }
                }
            }
        }

        // TODO: entities

        return new LinkedList<>(positions);
    }
}
