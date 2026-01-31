package dev.luizloyola.outlanders.mixin;

import dev.luizloyola.outlanders.mixinInterfaces.ServerWorldExt;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ActiveTargetGoal.class)
public abstract class ActiveTargetGoalMixin {
    @Shadow
    @Final
    protected Class<?> targetClass;
    
    @Shadow
    protected LivingEntity targetEntity;

    @Shadow
    protected abstract TargetPredicate getAndUpdateTargetPredicate();

    @Inject(method = "findClosestTarget", at = @At("RETURN"))
    private void outlander$alsoTargetOutlanders(CallbackInfo ci) {
        if (this.targetClass != PlayerEntity.class && this.targetClass != ServerPlayerEntity.class) {
            return;
        }
        var mob = ((TrackTargetGoalAccessor) this).outlanders$getMob();

        if (!(mob.getEntityWorld() instanceof ServerWorld world)) return;

        this.targetEntity = ((ServerWorldExt) world)
                .outlanders$getClosestPlayerOrPerson(
                        this.getAndUpdateTargetPredicate(),
                        mob,
                        mob.getX(),
                        mob.getEyeY(),
                        mob.getZ()
                );
    }
}