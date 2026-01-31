package dev.luizloyola.outlanders.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.MannequinEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class PersonEntity extends MannequinEntity {
    public static EntityType.EntityFactory<PersonEntity> personFactory = PersonEntity::new;
    private final PersonState state = new PersonState();

    public PersonEntity(EntityType<? extends MannequinEntity> entityType, World world) {
        //noinspection unchecked
        super((EntityType<MannequinEntity>) entityType, world);
    }

    public void tick() {
        super.tick();
        this.state.tick(this.getEntityPos(), this.getVelocity());
//        if (this.skinLookup != null && this.skinLookup.isDone()) {
//            try {
//                ((Optional)this.skinLookup.get()).ifPresent(this::setSkin);
//                this.skinLookup = null;
//            } catch (Exception var2) {
//                LOGGER.error("Error when trying to look up skin", (Throwable)var2);
//            }
//        }
    }
    @Nullable
    public static PersonEntity createPerson(EntityType<PersonEntity> type, World world) {
        return personFactory.create(type, world);
    }

    public ParrotEntity.@Nullable Variant getShoulderParrotVariant(boolean leftShoulder) {
        return null;
    }

    public @Nullable Text getPersonName() {
        return Text.of("Person");
    }

    public boolean hasExtraEars() {
        return false;
    }

    public PersonState getState() {
        return this.state;
    }
}
