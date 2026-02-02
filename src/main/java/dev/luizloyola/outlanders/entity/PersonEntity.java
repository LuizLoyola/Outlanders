package dev.luizloyola.outlanders.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.MannequinEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class PersonEntity extends MannequinEntity {
    private static final String PERSON_BRAIN_JSON_INIT_PLACEHOLDER = "--init--";
    public static EntityType.EntityFactory<PersonEntity> personFactory = PersonEntity::new;
    private final PersonState state = new PersonState();

    private static final TrackedData<String> PERSON_BRAIN_JSON = DataTracker.registerData(PersonEntity.class, TrackedDataHandlerRegistry.STRING);

    public PersonEntity(EntityType<? extends MannequinEntity> entityType, World world) {
        //noinspection unchecked
        super((EntityType<MannequinEntity>) entityType, world);
    }

    protected PersonEntity(World world) {
        this(OutlandersEntities.PERSON, world);
    }

    public static DefaultAttributeContainer.Builder createPersonAttributes() {
        return createLivingAttributes();
    }

    public void tick() {
        super.tick();

        this.state.tick(this.getEntityPos(), this.getVelocity());

        if (!this.getEntityWorld().isClient()) {
            this.getPersonBrain().tick();
        }

//        if (this.skinLookup != null && this.skinLookup.isDone()) {
//            try {
//                ((Optional)this.skinLookup.get()).ifPresent(this::setSkin);
//                this.skinLookup = null;
//            } catch (Exception var2) {
//                LOGGER.error("Error when trying to look up skin", (Throwable)var2);
//            }
//        }
    }

    private PersonBrain personBrain;

    public PersonBrain getPersonBrain() {
        if (this.personBrain != null) {
            return this.personBrain;
        }

        var personBrainJson = this.dataTracker.get(PERSON_BRAIN_JSON);
        this.personBrain = PersonBrain.fromJson(this, personBrainJson);
        return this.personBrain;
    }

    @Nullable
    public static PersonEntity createPerson(EntityType<PersonEntity> type, World world) {
        return personFactory.create(type, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);

        builder.add(PERSON_BRAIN_JSON, PERSON_BRAIN_JSON_INIT_PLACEHOLDER);
    }

    @Override
    public boolean shouldRenderName() {
        return true;
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public Text getName() {
        // Name to be displayed above the entity
        var text = Text.literal(this.getPersonName());
        var gender = this.getIdentity().gender();
        if (gender != null) {
            text.setStyle(Style.EMPTY.withColor(gender.colorFormatting()));
        }
        return text;
    }

    public PersonIdentity getIdentity() {
        return this.getPersonBrain().getIdentity();
    }

    public ParrotEntity.@Nullable Variant getShoulderParrotVariant(boolean leftShoulder) {
        return null;
    }

    public String getPersonName() {
        var name = this.getIdentity().name();
        return name != null ? name : "NULL";
    }

    public boolean hasExtraEars() {
        return false;
    }

    public PersonState getState() {
        return this.state;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);

        view.putString("person_brain", this.getPersonBrain().toJson());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);

        var personBrainJson = view.getString("person_brain", PERSON_BRAIN_JSON_INIT_PLACEHOLDER);
        if (personBrainJson == null || personBrainJson.equals(PERSON_BRAIN_JSON_INIT_PLACEHOLDER)) {
            var newBrain = new PersonBrain(this);
            personBrainJson = newBrain.toJson();
        }

        this.dataTracker.set(PERSON_BRAIN_JSON, personBrainJson);
    }


}