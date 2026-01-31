package dev.luizloyola.outlanders.mixin.client;

import dev.luizloyola.outlanders.client.renderer.OutlandersEntityRendererFactories;
import dev.luizloyola.outlanders.client.renderer.PersonEntityRenderer;
import dev.luizloyola.outlanders.entity.PersonEntity;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(EntityRenderManager.class)
public class EntityRenderManagerMixin {
    @Unique
    private Map<PlayerSkinType, PersonEntityRenderer> personRenderers;

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void outlanders$reload(ResourceManager manager, CallbackInfo ci, EntityRendererFactory.Context context) {
        this.personRenderers = OutlandersEntityRendererFactories.reloadPersonRenderers(context);
    }

    @Inject(
            method = "getRenderer(Lnet/minecraft/entity/Entity;)Lnet/minecraft/client/render/entity/EntityRenderer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private <T extends Entity> void outlanders$getRenderer(T entity, CallbackInfoReturnable<EntityRenderer<? super T, ?>> cir) {
        if (entity instanceof PersonEntity person) {
            var personData = person.getPersonData();
            var gender = personData != null ? personData.gender() : null;
            var playerSkinType = gender != null ? gender.choose(PlayerSkinType.WIDE, PlayerSkinType.SLIM) : PlayerSkinType.SLIM;
            //noinspection unchecked
            cir.setReturnValue((EntityRenderer<? super T, ?>) this.personRenderers.get(playerSkinType));
        }
    }
}
