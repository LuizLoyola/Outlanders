package dev.luizloyola.outlanders.client.renderer;

import dev.luizloyola.outlanders.entity.PersonEntity;
import dev.luizloyola.outlanders.entity.PersonState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EquipmentModelData;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.SwingAnimationComponent;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.SwingAnimationType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class PersonEntityRenderer extends LivingEntityRenderer<PersonEntity, PlayerEntityRenderState, PlayerEntityModel> {
    public PersonEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PlayerEntityModel(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5F);
        this.addFeature(
                new ArmorFeatureRenderer<>(
                        this,
                        EquipmentModelData.mapToEntityModel(                                EntityModelLayers.PLAYER_EQUIPMENT, ctx.getEntityModels(), root -> new PlayerEntityModel(root, false)                        ),
                        ctx.getEquipmentRenderer()
                )
        );
        this.addFeature(new PlayerHeldItemFeatureRenderer<>(this));
        this.addFeature(new StuckArrowsFeatureRenderer<>(this, ctx));
        this.addFeature(new Deadmau5FeatureRenderer(this, ctx.getEntityModels()));
        this.addFeature(new CapeFeatureRenderer(this, ctx.getEntityModels(), ctx.getEquipmentModelLoader()));
        this.addFeature(new HeadFeatureRenderer<>(this, ctx.getEntityModels(), ctx.getPlayerSkinCache()));
        this.addFeature(new ElytraFeatureRenderer<>(this, ctx.getEntityModels(), ctx.getEquipmentRenderer()));
        this.addFeature(new ShoulderParrotFeatureRenderer(this, ctx.getEntityModels()));
        this.addFeature(new TridentRiptideFeatureRenderer(this, ctx.getEntityModels()));
        this.addFeature(new StuckStingersFeatureRenderer<>(this, ctx));
    }

    protected boolean shouldRenderFeatures(PlayerEntityRenderState playerEntityRenderState) {
        return !playerEntityRenderState.spectator;
    }

    public Vec3d getPositionOffset(PlayerEntityRenderState playerEntityRenderState) {
        Vec3d vec3d = super.getPositionOffset(playerEntityRenderState);
        return playerEntityRenderState.isInSneakingPose ? vec3d.add(0.0, playerEntityRenderState.baseScale * -2.0F / 16.0, 0.0) : vec3d;
    }

    private static BipedEntityModel.ArmPose getArmPose(PlayerLikeEntity player, Arm arm) {
        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack itemStack2 = player.getStackInHand(Hand.OFF_HAND);
        BipedEntityModel.ArmPose armPose = getArmPose(player, itemStack, Hand.MAIN_HAND);
        BipedEntityModel.ArmPose armPose2 = getArmPose(player, itemStack2, Hand.OFF_HAND);
        if (armPose.isTwoHanded()) {
            armPose2 = itemStack2.isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;
        }

        return player.getMainArm() == arm ? armPose : armPose2;
    }

    private static BipedEntityModel.ArmPose getArmPose(PlayerLikeEntity player, ItemStack stack, Hand hand) {
        if (stack.isEmpty()) {
            return BipedEntityModel.ArmPose.EMPTY;
        } else if (!player.handSwinging && stack.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(stack)) {
            return BipedEntityModel.ArmPose.CROSSBOW_HOLD;
        } else {
            if (player.getActiveHand() == hand && player.getItemUseTimeLeft() > 0) {
                UseAction useAction = stack.getUseAction();
                if (useAction == UseAction.BLOCK) {
                    return BipedEntityModel.ArmPose.BLOCK;
                }

                if (useAction == UseAction.BOW) {
                    return BipedEntityModel.ArmPose.BOW_AND_ARROW;
                }

                if (useAction == UseAction.TRIDENT) {
                    return BipedEntityModel.ArmPose.THROW_TRIDENT;
                }

                if (useAction == UseAction.CROSSBOW) {
                    return BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (useAction == UseAction.SPYGLASS) {
                    return BipedEntityModel.ArmPose.SPYGLASS;
                }

                if (useAction == UseAction.TOOT_HORN) {
                    return BipedEntityModel.ArmPose.TOOT_HORN;
                }

                if (useAction == UseAction.BRUSH) {
                    return BipedEntityModel.ArmPose.BRUSH;
                }

                if (useAction == UseAction.SPEAR) {
                    return BipedEntityModel.ArmPose.SPEAR;
                }
            }

            SwingAnimationComponent swingAnimationComponent = (SwingAnimationComponent)stack.get(DataComponentTypes.SWING_ANIMATION);
            if (swingAnimationComponent != null && swingAnimationComponent.type() == SwingAnimationType.STAB && player.handSwinging) {
                return BipedEntityModel.ArmPose.SPEAR;
            } else {
                return stack.isIn(ItemTags.SPEARS) ? BipedEntityModel.ArmPose.SPEAR : BipedEntityModel.ArmPose.ITEM;
            }
        }
    }

    public Identifier getTexture(PlayerEntityRenderState playerEntityRenderState) {
        return playerEntityRenderState.skinTextures.body().texturePath();
    }

    protected void scale(PlayerEntityRenderState playerEntityRenderState, MatrixStack matrixStack) {
        float f = 0.9375F;
        matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    protected void renderLabelIfPresent(
            PlayerEntityRenderState playerEntityRenderState,
            MatrixStack matrixStack,
            OrderedRenderCommandQueue orderedRenderCommandQueue,
            CameraRenderState cameraRenderState
    ) {
        matrixStack.push();
        int i = playerEntityRenderState.extraEars ? -10 : 0;
        if (playerEntityRenderState.playerName != null) {
            orderedRenderCommandQueue.submitLabel(
                    matrixStack,
                    playerEntityRenderState.nameLabelPos,
                    i,
                    playerEntityRenderState.playerName,
                    !playerEntityRenderState.sneaking,
                    playerEntityRenderState.light,
                    playerEntityRenderState.squaredDistanceToCamera,
                    cameraRenderState
            );
            matrixStack.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
        }

        if (playerEntityRenderState.displayName != null) {
            orderedRenderCommandQueue.submitLabel(
                    matrixStack,
                    playerEntityRenderState.nameLabelPos,
                    i,
                    playerEntityRenderState.displayName,
                    !playerEntityRenderState.sneaking,
                    playerEntityRenderState.light,
                    playerEntityRenderState.squaredDistanceToCamera,
                    cameraRenderState
            );
        }

        matrixStack.pop();
    }

    public PlayerEntityRenderState createRenderState() {
        return new PlayerEntityRenderState();
    }

    public void updateRenderState(PersonEntity personEntity, PlayerEntityRenderState playerEntityRenderState, float f) {
        super.updateRenderState(personEntity, playerEntityRenderState, f);
        BipedEntityRenderer.updateBipedRenderState(personEntity, playerEntityRenderState, f, this.itemModelResolver);
        playerEntityRenderState.leftArmPose = getArmPose(personEntity, Arm.LEFT);
        playerEntityRenderState.rightArmPose = getArmPose(personEntity, Arm.RIGHT);
//        playerEntityRenderState.skinTextures = outlanderEntity.getSkin();
//        playerEntityRenderState.skinTextures = DefaultSkinHelper.getSteve();
        playerEntityRenderState.stuckArrowCount = personEntity.getStuckArrowCount();
        playerEntityRenderState.stingerCount = personEntity.getStingerCount();
        playerEntityRenderState.spectator = personEntity.isSpectator();
        playerEntityRenderState.hatVisible = personEntity.isModelPartVisible(PlayerModelPart.HAT);
        playerEntityRenderState.jacketVisible = personEntity.isModelPartVisible(PlayerModelPart.JACKET);
        playerEntityRenderState.leftPantsLegVisible = personEntity.isModelPartVisible(PlayerModelPart.LEFT_PANTS_LEG);
        playerEntityRenderState.rightPantsLegVisible = personEntity.isModelPartVisible(PlayerModelPart.RIGHT_PANTS_LEG);
        playerEntityRenderState.leftSleeveVisible = personEntity.isModelPartVisible(PlayerModelPart.LEFT_SLEEVE);
        playerEntityRenderState.rightSleeveVisible = personEntity.isModelPartVisible(PlayerModelPart.RIGHT_SLEEVE);
        playerEntityRenderState.capeVisible = personEntity.isModelPartVisible(PlayerModelPart.CAPE);
//        this.updateGliding(outlanderEntity, playerEntityRenderState, f);
//        this.updateCape(outlanderEntity, playerEntityRenderState, f);
        if (playerEntityRenderState.squaredDistanceToCamera < 100.0) {
            playerEntityRenderState.playerName = personEntity.getPersonName();
        } else {
            playerEntityRenderState.playerName = null;
        }

        playerEntityRenderState.leftShoulderParrotVariant = personEntity.getShoulderParrotVariant(true);
        playerEntityRenderState.rightShoulderParrotVariant = personEntity.getShoulderParrotVariant(false);
        playerEntityRenderState.id = personEntity.getId();
        playerEntityRenderState.extraEars = personEntity.hasExtraEars();
        playerEntityRenderState.spyglassState.clear();
        if (playerEntityRenderState.isUsingItem) {
            ItemStack itemStack = personEntity.getStackInHand(playerEntityRenderState.activeHand);
            if (itemStack.isOf(Items.SPYGLASS)) {
                this.itemModelResolver.updateForLivingEntity(playerEntityRenderState.spyglassState, itemStack, ItemDisplayContext.HEAD, personEntity);
            }
        }
    }

    protected boolean hasLabel(PersonEntity personEntity, double d) {
        return super.hasLabel(personEntity, d)
                && (personEntity.shouldRenderName() || personEntity.hasCustomName() && personEntity == this.dispatcher.targetedEntity);
    }

    private void updateGliding(PersonEntity personEntity, PlayerEntityRenderState state, float tickProgress) {
        state.glidingTicks = personEntity.getGlidingTicks() + tickProgress;
        Vec3d vec3d = personEntity.getRotationVec(tickProgress);
        Vec3d vec3d2 = personEntity.getState().getVelocity().lerp(personEntity.getVelocity(), tickProgress);
        if (vec3d2.horizontalLengthSquared() > 1.0E-5F && vec3d.horizontalLengthSquared() > 1.0E-5F) {
            state.applyFlyingRotation = true;
            double d = vec3d2.getHorizontal().normalize().dotProduct(vec3d.getHorizontal().normalize());
            double e = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
            state.flyingRotation = (float)(Math.signum(e) * Math.acos(Math.min(1.0, Math.abs(d))));
        } else {
            state.applyFlyingRotation = false;
            state.flyingRotation = 0.0F;
        }
    }

    private void updateCape(PersonEntity personEntity, PlayerEntityRenderState state, float tickProgress) {
        PersonState personState = personEntity.getState();
        double d = personState.lerpX(tickProgress) - MathHelper.lerp(tickProgress, personEntity.lastX, personEntity.getX());
        double e = personState.lerpY(tickProgress) - MathHelper.lerp(tickProgress, personEntity.lastY, personEntity.getY());
        double f = personState.lerpZ(tickProgress) - MathHelper.lerp(tickProgress, personEntity.lastZ, personEntity.getZ());
        float g = MathHelper.lerpAngleDegrees(tickProgress, personEntity.lastBodyYaw, personEntity.bodyYaw);
        double h = MathHelper.sin(g * (float) (Math.PI / 180.0));
        double i = -MathHelper.cos(g * (float) (Math.PI / 180.0));
        state.field_53536 = (float)e * 10.0F;
        state.field_53536 = MathHelper.clamp(state.field_53536, -6.0F, 32.0F);
        state.field_53537 = (float)(d * h + f * i) * 100.0F;
        state.field_53537 = state.field_53537 * (1.0F - state.getGlidingProgress());
        state.field_53537 = MathHelper.clamp(state.field_53537, 0.0F, 150.0F);
        state.field_53538 = (float)(d * i - f * h) * 100.0F;
        state.field_53538 = MathHelper.clamp(state.field_53538, -20.0F, 20.0F);
        float j = personState.lerpMovement(tickProgress);
        float k = personState.getLerpedDistanceMoved(tickProgress);
        state.field_53536 = state.field_53536 + MathHelper.sin(k * 6.0F) * 32.0F * j;
    }

    public void renderRightArm(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, Identifier skinTexture, boolean sleeveVisible) {
        this.renderArm(matrices, queue, light, skinTexture, this.model.rightArm, sleeveVisible);
    }

    public void renderLeftArm(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, Identifier skinTexture, boolean sleeveVisible) {
        this.renderArm(matrices, queue, light, skinTexture, this.model.leftArm, sleeveVisible);
    }

    private void renderArm(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, Identifier skinTexture, ModelPart arm, boolean sleeveVisible) {
        PlayerEntityModel playerEntityModel = this.getModel();
        arm.resetTransform();
        arm.visible = true;
        playerEntityModel.leftSleeve.visible = sleeveVisible;
        playerEntityModel.rightSleeve.visible = sleeveVisible;
        playerEntityModel.leftArm.roll = -0.1F;
        playerEntityModel.rightArm.roll = 0.1F;
        queue.submitModelPart(arm, matrices, RenderLayers.entityTranslucent(skinTexture), light, OverlayTexture.DEFAULT_UV, null);
    }

    protected void setupTransforms(PlayerEntityRenderState playerEntityRenderState, MatrixStack matrixStack, float f, float g) {
        float h = playerEntityRenderState.leaningPitch;
        float i = playerEntityRenderState.pitch;
        if (playerEntityRenderState.isGliding) {
            super.setupTransforms(playerEntityRenderState, matrixStack, f, g);
            float j = playerEntityRenderState.getGlidingProgress();
            if (!playerEntityRenderState.usingRiptide) {
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(j * (-90.0F - i)));
            }

            if (playerEntityRenderState.applyFlyingRotation) {
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(playerEntityRenderState.flyingRotation));
            }
        } else if (h > 0.0F) {
            super.setupTransforms(playerEntityRenderState, matrixStack, f, g);
            float jx = playerEntityRenderState.touchingWater ? -90.0F - i : -90.0F;
            float k = MathHelper.lerp(h, 0.0F, jx);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k));
            if (playerEntityRenderState.isSwimming) {
                matrixStack.translate(0.0F, -1.0F, 0.3F);
            }
        } else {
            super.setupTransforms(playerEntityRenderState, matrixStack, f, g);
        }
    }

    public boolean shouldFlipUpsideDown(PersonEntity personEntity) {
        if (personEntity.isModelPartVisible(PlayerModelPart.CAPE)) {
            return super.shouldFlipUpsideDown(personEntity);
        } else {
            return false;
        }
    }

    public static boolean shouldFlipUpsideDown(PlayerEntity player) {
        return shouldFlipUpsideDown(player.getGameProfile().name());
    }
}
