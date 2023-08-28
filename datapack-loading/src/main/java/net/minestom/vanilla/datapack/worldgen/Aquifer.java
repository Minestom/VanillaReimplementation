package net.minestom.vanilla.datapack.worldgen;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.datapack.worldgen.noise.NoiseChunk;
import net.minestom.vanilla.generation.noise.NoiseRouter;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

public interface Aquifer {

    @Nullable Block compute(DensityFunction.Context context, double density);

    record FluidStatus(int level, Block type) {
        public Block at(int level) {
            return level < this.level ? this.type : Block.AIR;
        }
    }

    interface FluidPicker {
        FluidStatus pickFluid(int x, int y, int z);
    }

    static Aquifer createDisabled(FluidPicker fluidPicker) {
        return (context, density) -> {
            if (density > 0) {
                return null;
            }
            return fluidPicker.pickFluid(context.blockX(), context.blockY(), context.blockZ()).at(context.blockY());
        };
    }

    class NoiseAquifer implements Aquifer {
        private static final int X_SPACING = 16;
        private static final int Y_SPACING = 12;
        private static final int Z_SPACING = 16;

        private static final int[][] SURFACE_SAMPLING = new int[][]{
                {-2, -1}, {-1, -1}, {0, -1},
                {1, -1}, {-3, 0}, {-2, 0},
                {-1, 0}, {0, 0}, {1, 0},
                {-2, 1}, {-1, 1}, {0, 1},
                {1, 1}};

        private final int minGridX;
        private final int minGridY;
        private final int minGridZ;
        private final int gridSizeX;
        private final int gridSizeZ;
        private final int gridSize;

        private final Map<Integer, FluidStatus> aquiferCache;
        private final Map<Integer, Point> aquiferLocationCache;

        private final NoiseChunk noiseChunk;
        private final NoiseRouter router;
        private final WorldgenRandom.Positional random;
        private final FluidPicker globalFluidPicker;

        public NoiseAquifer(
                NoiseChunk noiseChunk,
                Point chunkPos,
                NoiseRouter router,
                WorldgenRandom.Positional random,
                int minY,
                int height,
                FluidPicker globalFluidPicker) {
            this.noiseChunk = noiseChunk;
            this.router = router;
            this.random = random;
            this.globalFluidPicker = globalFluidPicker;
            this.minGridX = this.gridX(Util.chunkMinX(chunkPos)) - 1;
            this.gridSizeX = this.gridX(Util.chunkMaxX(chunkPos)) + 1 - this.minGridX + 1;
            this.minGridY = this.gridY(minY) - 1;
            this.minGridZ = this.gridZ(Util.chunkMinZ(chunkPos)) - 1;
            this.gridSizeZ = this.gridZ(Util.chunkMaxZ(chunkPos)) + 1 - this.minGridZ + 1;
            int gridSizeY = this.gridY(minY + height) + 1 - this.minGridY + 1;
            this.gridSize = this.gridSizeX * gridSizeY * this.gridSizeZ;
            this.aquiferCache = new HashMap<>();
            this.aquiferLocationCache = new HashMap<>();
        }

        public Block compute(DensityFunction.Context context, double density) {
            int x = context.blockX();
            int y = context.blockY();
            int z = context.blockZ();
            if (density <= 0) {
                if (this.globalFluidPicker.pickFluid(x, y, z).at(y).compare(Block.LAVA)) {
                    return Block.LAVA;
                }

                int gridX = this.gridX(x - 5);
                int gridY = this.gridY(y + 1);
                int gridZ = this.gridZ(z - 5);
                double mag1 = Integer.MAX_VALUE;
                double mag2 = Integer.MAX_VALUE;
                double mag3 = Integer.MAX_VALUE;
                Point loc1 = Vec.ZERO;
                Point loc2 = Vec.ZERO;
                Point loc3 = Vec.ZERO;

                for (int xOffset = 0; xOffset <= 1; xOffset += 1) {
                    for (int yOffset = -1; yOffset <= 1; yOffset += 1) {
                        for (int zOffset = 0; zOffset <= 1; zOffset += 1) {
                            Point location = this.getLocation(gridX + xOffset, gridY + yOffset, gridZ + zOffset);
                            double magnitude = location.distanceSquared(Vec.ZERO);
                            if (mag1 >= magnitude) {
                                loc3 = loc2;
                                loc2 = loc1;
                                loc1 = location;
                                mag3 = mag2;
                                mag2 = mag1;
                                mag1 = magnitude;
                            } else if (mag2 >= magnitude) {
                                loc3 = loc2;
                                loc2 = location;
                                mag3 = mag2;
                                mag2 = magnitude;
                            } else if (mag3 >= magnitude) {
                                loc3 = location;
                                mag3 = magnitude;
                            }
                        }
                    }
                }
                FluidStatus status1 = this.getStatus(loc1);
                FluidStatus status2 = this.getStatus(loc2);
                FluidStatus status3 = this.getStatus(loc3);
                double similarity12 = NoiseAquifer.similarity(mag1, mag2);
                double similarity13 = NoiseAquifer.similarity(mag1, mag3);
                double similarity23 = NoiseAquifer.similarity(mag2, mag3);

                double pressure;
                if (status1.at(y).compare(Block.WATER) && this.globalFluidPicker.pickFluid(x, y - 1, z).at(y - 1).compare(Block.LAVA)) {
                    pressure = 1;
                } else if (similarity12 > -1) {
                    DoubleSupplier barrier = Util.lazyDouble(() -> this.router.barrier().compute(DensityFunctions.context(x, y * 0.5, z)));
                    double pressure12 = this.calculatePressure(y, status1, status2, barrier);
                    double pressure13 = this.calculatePressure(y, status1, status3, barrier);
                    double pressure23 = this.calculatePressure(y, status2, status3, barrier);
                    double n = Math.max(Math.max(pressure12, pressure13 * Math.max(0, similarity13)), pressure23 * similarity23);
                    pressure = Math.max(0, 2 * Math.max(0, similarity12) * n);
                } else {
                    pressure = 0;
                }

                if (density + pressure <= 0) {
                    return status1.at(y);
                }
            }
            return null;
        }

        private static double similarity(double a, double b) {
            return 1 - Math.abs(b - a) / 25;
        }

        private double calculatePressure(int y, FluidStatus status1, FluidStatus status2, DoubleSupplier barrier) {
            Block fluid1 = status1.at(y);
            Block fluid2 = status2.at(y);
            if ((fluid1.compare(Block.LAVA) && fluid2.compare(Block.WATER)) || (fluid1.compare(Block.WATER) && fluid2.compare(Block.LAVA))) {
                return 1;
            }
            int levelDiff = Math.abs(status1.level - status2.level);
            if (levelDiff == 0) {
                return 0;
            }
            double levelAvg = (status1.level + status2.level) / 2.0;
            double levelAvgDiff = y + 0.5 - levelAvg;
            double p = levelDiff / 2.0 - Math.abs(levelAvgDiff);
            double pressure = levelAvgDiff > 0
                    ? p > 0 ? p / 1.5 : p / 2.5
                    : p > -3 ? (p + 3) / 3 : (p + 3) / 10;
            if (pressure < -2 || pressure > 2) {
                return pressure;
            }
            return pressure + barrier.getAsDouble();
        }

        private FluidStatus getStatus(Point location) {
            int x = location.blockX();
            int y = location.blockY();
            int z = location.blockZ();
            int index = this.getIndex(this.gridX(x), this.gridY(y), this.gridZ(z));
            FluidStatus cachedStatus = this.aquiferCache.get(index);
            if (cachedStatus != null) {
                return cachedStatus;
            }
            FluidStatus status = this.computeStatus(x, y, z);
            this.aquiferCache.put(index, status);
            return status;
        }

        private FluidStatus computeStatus(int x, int y, int z) {
            FluidStatus globalStatus = this.globalFluidPicker.pickFluid(x, y, z);
            int minPreliminarySurface = Integer.MIN_VALUE;
            boolean isAquifer = false;
            for (int[] offset : NoiseAquifer.SURFACE_SAMPLING) {
                int xOffset = offset[0];
                int zOffset = offset[1];
                int blockX = x + (xOffset << 4);
                int blockZ = z + (zOffset << 4);
                int preliminarySurface = this.noiseChunk.getPreliminarySurfaceLevel(blockX, blockZ);
                minPreliminarySurface = Math.min(minPreliminarySurface, preliminarySurface);
                boolean noOffset = xOffset == 0 && zOffset == 0;
                if (noOffset && y - 12 > preliminarySurface + 8) {
                    return globalStatus;
                }
                if ((noOffset || y + 12 > preliminarySurface + 8)) {
                    FluidStatus newStatus = this.globalFluidPicker.pickFluid(blockX, preliminarySurface + 8, blockZ);
                    if (!newStatus.at(preliminarySurface + 8).compare(Block.AIR)) {
                        if (noOffset) {
                            return newStatus;
                        } else {
                            isAquifer = true;
                        }
                    }
                }
            }

            double allowedFloodedness = isAquifer ? Util.clampedMap(minPreliminarySurface + 8 - y, 0, 64, 1, 0) : 0;
            double floodedness = Util.clamp(this.router.fluidLevelFloodedness().compute(DensityFunctions.context(x, y * 0.67, z)), -1, 1);
            if (floodedness > Util.map(allowedFloodedness, 1, 0, -0.3, 0.8)) {
                return globalStatus;
            }
            if (floodedness <= Util.map(allowedFloodedness, 1, 0, -0.8, 0.4)) {
                return new FluidStatus(Integer.MIN_VALUE, globalStatus.type);
            }

            int gridY = (int) Math.floor(y / 40);
            double spread = this.router.fluidLevelSpread().compute(DensityFunctions.context(Math.floor(x / 16), gridY, Math.floor(z / 16)));
            int level = gridY * 40 + 20 + (int) Math.floor(spread / 3) * 3;
            int statusLevel = Math.min(minPreliminarySurface, level);
            Block fluid = this.getFluidType(x, y, z, globalStatus.type, level);
            return new FluidStatus(statusLevel, fluid);
        }

        private Block getFluidType(double x, double y, double z, Block global, int level) {
            if (level <= -10) {
                double lava = this.router.lava().compute(DensityFunctions.context(Math.floor(x / 64), Math.floor(y / 40), Math.floor(z / 64)));
                if (Math.abs(lava) > 0.3) {
                    return Block.LAVA;
                }
            }
            return global;
        }

        private Point getLocation(int x, int y, int z) {
            int index = this.getIndex(x, y, z);
            Point cachedLocation = this.aquiferLocationCache.get(index);
            if (Vec.ZERO.equals(cachedLocation)) {
                return cachedLocation;
            }
            WorldgenRandom random = this.random.at(x, y, z);
            Point location = new Vec(
                    x * NoiseAquifer.X_SPACING + random.nextInt(10),
                    y * NoiseAquifer.Y_SPACING + random.nextInt(9),
                    z * NoiseAquifer.Z_SPACING + random.nextInt(10));
            this.aquiferLocationCache.put(index, location);
            return location;
        }

        private int getIndex(int x, int y, int z) {
            int gridX = x - this.minGridX;
            int gridY = y - this.minGridY;
            int gridZ = z - this.minGridZ;
            int index = (gridY * this.gridSizeZ + gridZ) * this.gridSizeX + gridX;
            if (index < 0 || index >= this.gridSize) {
                throw new Error("Invalid aquifer index at (" + x + ", " + y + ", " + z + ") : 0 <= " + index + " < " + gridSize);
            }
            return index;
        }

        private int gridX(int x) {
            return (int) Math.floor(x / NoiseAquifer.X_SPACING);
        }

        private int gridY(int y) {
            return (int) Math.floor(y / NoiseAquifer.Y_SPACING);
        }

        private int gridZ(int z) {
            return (int) Math.floor(z / NoiseAquifer.Z_SPACING);
        }
    }
}
