package com.tameno.simpleclimbing;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import java.util.List;

public class Utils {
    public static boolean isTouchingWall(Entity entity) {
        Box boundingBox = entity.getBoundingBox();
        World world = entity.getWorld();
        double testMovementAmount = 0.0001;

        Vec3d testMovement = new Vec3d(testMovementAmount, 0.0, testMovementAmount);
        List<VoxelShape> collisions = world.getEntityCollisions(entity, boundingBox.stretch(testMovement));
        Vec3d result = Entity.adjustMovementForCollisions(entity, testMovement, boundingBox, world, collisions);
        if (!result.equals(testMovement)) {
            return true;
        }

        testMovement = new Vec3d(-testMovementAmount, 0.0, -testMovementAmount);
        collisions = world.getEntityCollisions(entity, boundingBox.stretch(testMovement));
        result = Entity.adjustMovementForCollisions(entity, testMovement, boundingBox, world, collisions);
        if (!result.equals(testMovement)) {
            return true;
        }

        return false;
    }
}
