package dev.luizloyola.outlanders.entity;

import dev.luizloyola.outlanders.mixin.client.ClientWorldAccessor;
import dev.luizloyola.outlanders.registry.OutlandersComponents;
import dev.luizloyola.outlanders.registry.OutlandersItems;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ClientPersonEntity extends PersonEntity {
    public ClientPersonEntity(World world) {
        super(world);
    }

    public static void setFactory() {
        PersonEntity.personFactory = (type, world) -> (PersonEntity) (world instanceof ClientWorld
                ? new ClientPersonEntity(world)
                : new PersonEntity(type, world));
    }

    @Override
    public void tick() {
        super.tick();

        var world = (ClientWorldAccessor) this.getEntityWorld();
        @SuppressWarnings("resource")
        var thisPlayer = world.outlanders$getClient().player;

        var shouldGlow = false;

        if (thisPlayer != null) {
            if (this.isCommandStickBoundToMe(thisPlayer.getMainHandStack())) {
                shouldGlow = true;
            } else if (this.isCommandStickBoundToMe(thisPlayer.getOffHandStack())) {
                shouldGlow = true;
            }
        }

        this.setFlag(GLOWING_FLAG_INDEX, shouldGlow);
    }

    private boolean isCommandStickBoundToMe(ItemStack itemStack) {
        if (!itemStack.isOf(OutlandersItems.COMMAND_STICK)) {
            return false;
        }

        if (itemStack.getCount() != 1) {
            return false;
        }

        var boundPersonUuidComponent = itemStack.get(OutlandersComponents.BOUND_PERSON);
        if (boundPersonUuidComponent == null) {
            return false;
        }

        var uuid = boundPersonUuidComponent.asUuid();
        if (uuid == null) {
            return false;
        }

        return uuid.equals(this.getUuid());
    }
}
