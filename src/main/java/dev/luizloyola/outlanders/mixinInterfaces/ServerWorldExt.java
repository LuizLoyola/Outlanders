package dev.luizloyola.outlanders.mixinInterfaces;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;

public interface ServerWorldExt {
    LivingEntity outlanders$getClosestPlayerOrPerson(TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z);
}
