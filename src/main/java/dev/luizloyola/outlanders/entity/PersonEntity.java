package dev.luizloyola.outlanders.entity;

import dev.luizloyola.outlanders.entity.data.Gender;
import dev.luizloyola.outlanders.entity.data.PersonData;
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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class PersonEntity extends MannequinEntity {
    private static final String PERSON_DATA_JSON_INIT_PLACEHOLDER = "--init--";
    public static EntityType.EntityFactory<PersonEntity> personFactory = PersonEntity::new;
    private final PersonState state = new PersonState();

    private static final TrackedData<String> PERSON_DATA_JSON = DataTracker.registerData(PersonEntity.class, TrackedDataHandlerRegistry.STRING);

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

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);

        builder.add(PERSON_DATA_JSON, PERSON_DATA_JSON_INIT_PLACEHOLDER);
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
        var gender = this.getPersonData().gender();
        if (gender != null) {
            text.setStyle(Style.EMPTY.withColor(gender.colorFormatting()));
        }
        return text;
    }

    private static PersonData createPersonData() {
        var gender = Gender.random();

        var maleSkins = List.of("kai", "noor", "steve", "sunny", "zuri");
        var femaleSkins = List.of("alex", "ari", "efe", "makena");

        var skinPool = gender.choose(maleSkins, femaleSkins);
        var random = Random.create();
        var randomSkinName = skinPool.get(random.nextInt(skinPool.size()));

        var capitalizedName = randomSkinName.substring(0, 1).toUpperCase() + randomSkinName.substring(1);

        return new PersonData(gender, capitalizedName, randomSkinName);
    }

    public ParrotEntity.@Nullable Variant getShoulderParrotVariant(boolean leftShoulder) {
        return null;
    }

    public String getPersonName() {
        var name = this.getPersonData().name();
        return name != null ? name : "NULL";
    }

    public PersonData getPersonData() {
        return PersonData.fromJson(this.dataTracker.get(PERSON_DATA_JSON));
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

        view.putString("person_data", this.getPersonData().toJson());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);

        var personDataJson = view.getString("person_data", PERSON_DATA_JSON_INIT_PLACEHOLDER);
        if (personDataJson == null || personDataJson.equals(PERSON_DATA_JSON_INIT_PLACEHOLDER)) {
            var personData = createPersonData();
            personDataJson = personData.toJson();
        }

        this.dataTracker.set(PERSON_DATA_JSON, personDataJson);
    }


}