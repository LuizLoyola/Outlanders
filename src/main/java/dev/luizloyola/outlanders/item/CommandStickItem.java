package dev.luizloyola.outlanders.item;

import dev.luizloyola.outlanders.component.OutlandersComponents;
import dev.luizloyola.outlanders.component.UuidComponent;
import dev.luizloyola.outlanders.entity.PersonEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CommandStickItem extends Item {
    private static final Logger LOGGER = LogManager.getLogger();

    public CommandStickItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.CONSUME;
        }

        var itemStack = user.getStackInHand(hand);

        var boundPerson = this.getBoundPerson(itemStack, serverWorld);
        if (boundPerson == null) {
            return ActionResult.CONSUME;
        }

        LOGGER.info("Cleared bound person {} from command stick held by {}", boundPerson.getUuid(), user.getName().getString());

        if (user.isSneaking()) {
            this.setBoundPerson(itemStack, null);

            // Inform player
            user.sendMessage(Text.literal("Cleared bound person from command stick"), false);

            // Write the modified stack back to the player's hand so the change is synced to the client
            user.setStackInHand(hand, itemStack);
            if (user.getInventory() != null) {
                user.getInventory().markDirty();
            }
        }

        return ActionResult.CONSUME;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var world = context.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.CONSUME;
        }

        var blockPos = context.getBlockPos();
        var blockState = context.getWorld().getBlockState(blockPos);
        if (blockState.isAir()) {
            return ActionResult.PASS;
        }

        var itemStack = context.getStack();

        var boundPerson = this.getBoundPerson(itemStack, serverWorld);
        if (boundPerson == null) {
            return ActionResult.CONSUME;
        }


        return super.useOnBlock(context);
    }

    @Override
    public ActionResult useOnEntity(ItemStack itemStack, PlayerEntity user, LivingEntity entity, Hand hand) {
        var world = user.getEntityWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.CONSUME;
        }

        var clickedPerson = entity instanceof PersonEntity person ? person : null;

        var boundPerson = this.getBoundPerson(itemStack, serverWorld);

        if (clickedPerson != null && user.isSneaking()) {
            this.setBoundPerson(itemStack, clickedPerson);

            // Inform player
            user.sendMessage(Text.literal("Bound person " + clickedPerson.getName().getString()), false);

            // Ensure the modified ItemStack is written back to the player's hand and inventory is marked dirty
            user.setStackInHand(hand, itemStack);
            if (user.getInventory() != null) {
                user.getInventory().markDirty();
            }

            return ActionResult.CONSUME;
        }


        return super.useOnEntity(itemStack, user, entity, hand);
    }


    private void setBoundPerson(ItemStack itemStack, PersonEntity person) {
        UUID uuid = null;
        if (person != null) {
            uuid = person.getUuid();
        }

        // Keep the runtime data component up-to-date
        itemStack.set(OutlandersComponents.BOUND_PERSON, UuidComponent.fromUuid(uuid));

        // Log the change so we can confirm it ran on the server and what UUID was stored
        if (uuid != null) {
            LOGGER.info("Bound person {} to command stick (uuid={})", person.getName().getString(), uuid);
        } else {
            LOGGER.info("Cleared bound person from command stick");
        }
    }

    private @Nullable PersonEntity getBoundPerson(ItemStack itemStack, ServerWorld world) {
        var uuid = this.getBoundPersonUuid(itemStack);
        if (uuid == null) {
            return null;
        }

        if (!(world.getEntity(uuid) instanceof PersonEntity person)) {
            this.setBoundPerson(itemStack, null);
            return null;
        } else {
            return person;
        }

    }

    private UUID getBoundPersonUuid(ItemStack stack) {
        var uuidComponent = stack.get(OutlandersComponents.BOUND_PERSON);
        if (uuidComponent == null) {
            return null;
        }

        return uuidComponent.asUuid();
    }

    private boolean hasPersonBound(ItemStack stack) {
        var uuidComponent = stack.get(OutlandersComponents.BOUND_PERSON);
        return uuidComponent != null && !uuidComponent.isEmpty();
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return this.hasPersonBound(stack);
    }

}
