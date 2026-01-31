package dev.luizloyola.outlanders.mixin;

import dev.luizloyola.outlanders.entity.PersonEntity;
import dev.luizloyola.outlanders.mixinInterfaces.ServerWorldExt;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements ServerWorldExt {
    @Override
    public LivingEntity outlanders$getClosestPlayerOrPerson(TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z) {
        ServerWorld world = (ServerWorld) (Object) this;
        List<LivingEntity> livingEntities = new ArrayList<>();

        var maxOutlanderRange = 128.0;

        var players = world.getPlayers();
        var outlanders = world.getEntitiesByType(TypeFilter.instanceOf(PersonEntity.class), entity.getBoundingBox().expand(maxOutlanderRange), EntityPredicates.VALID_LIVING_ENTITY);

        livingEntities.addAll(players);
        livingEntities.addAll(outlanders);

        return world.getClosestEntity(livingEntities, targetPredicate, entity, x, y, z);
    }
}
