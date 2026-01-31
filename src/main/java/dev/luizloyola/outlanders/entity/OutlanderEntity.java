package dev.luizloyola.outlanders.entity;

import dev.luizloyola.outlanders.init.OutlandersRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.MannequinEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class OutlanderEntity extends MannequinEntity {
    public static EntityType.EntityFactory<OutlanderEntity> outlanderFactory = OutlanderEntity::new;
    private final OutlanderState state = new OutlanderState();

    public OutlanderEntity(EntityType<? extends MannequinEntity> entityType, World world) {
        //noinspection unchecked
        super((EntityType<MannequinEntity>) entityType, world);
    }
    protected OutlanderEntity(World world) {
        this(OutlandersRegistry.OUTLANDER, world);
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
    public static OutlanderEntity createOutlander(EntityType<OutlanderEntity> type, World world) {
        return outlanderFactory.create(type, world);
    }

    public ParrotEntity.@Nullable Variant getShoulderParrotVariant(boolean leftShoulder) {
        return null;
    }

    public @Nullable Text getOutlanderName() {
        return Text.of("Outlander");
    }

    public boolean hasExtraEars() {
        return false;
    }

    public OutlanderState getState() {
        return this.state;
    }
}
