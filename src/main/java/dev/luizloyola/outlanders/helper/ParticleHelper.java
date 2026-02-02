package dev.luizloyola.outlanders.helper;

import net.minecraft.particle.SimpleParticleType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ParticleHelper {
    @SuppressWarnings({"DuplicatedCode", "PointlessArithmeticExpression"})
    public static void particleBox(ServerWorld world, SimpleParticleType particleType, BlockPos pos) {
        var x = pos.getX();
        var y = pos.getY();
        var z = pos.getZ();

        var pointsBetween = 2;

        var vertex1 = new Vec3d(x + 0, y + 0, z + 0);
        var vertex2 = new Vec3d(x + 1, y + 0, z + 0);
        var vertex3 = new Vec3d(x + 1, y + 1, z + 0);
        var vertex4 = new Vec3d(x + 0, y + 1, z + 0);
        var vertex5 = new Vec3d(x + 0, y + 0, z + 1);
        var vertex6 = new Vec3d(x + 1, y + 0, z + 1);
        var vertex7 = new Vec3d(x + 1, y + 1, z + 1);
        var vertex8 = new Vec3d(x + 0, y + 1, z + 1);

        particleLine(world, particleType, vertex1, vertex2, pointsBetween);
        particleLine(world, particleType, vertex2, vertex3, pointsBetween);
        particleLine(world, particleType, vertex3, vertex4, pointsBetween);
        particleLine(world, particleType, vertex4, vertex1, pointsBetween);

        particleLine(world, particleType, vertex5, vertex6, pointsBetween);
        particleLine(world, particleType, vertex6, vertex7, pointsBetween);
        particleLine(world, particleType, vertex7, vertex8, pointsBetween);
        particleLine(world, particleType, vertex8, vertex5, pointsBetween);

        particleLine(world, particleType, vertex1, vertex5, pointsBetween);
        particleLine(world, particleType, vertex2, vertex6, pointsBetween);
        particleLine(world, particleType, vertex3, vertex7, pointsBetween);
        particleLine(world, particleType, vertex4, vertex8, pointsBetween);
    }

    public static void particleLine(ServerWorld world, SimpleParticleType particleType, Vec3d start, Vec3d end, int pointsBetween) {
        world.spawnParticles(particleType, start.getX(), start.getY(), start.getZ(), 1, 0, 0, 0, 0);
        for (int i = 1; i <= pointsBetween; i++) {
            double t = (double) i / (pointsBetween + 1);
            double x = start.getX() + t * (end.getX() - start.getX());
            double y = start.getY() + t * (end.getY() - start.getY());
            double z = start.getZ() + t * (end.getZ() - start.getZ());
            world.spawnParticles(particleType, x, y, z, 1, 0, 0, 0, 0);
        }
        world.spawnParticles(particleType, end.getX(), end.getY(), end.getZ(), 1, 0, 0, 0, 0);
    }
}
