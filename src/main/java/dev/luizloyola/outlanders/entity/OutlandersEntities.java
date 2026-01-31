package dev.luizloyola.outlanders.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.HashMap;
import java.util.function.Function;

import static dev.luizloyola.outlanders.util.IdentifierUtils.idOf;

public class OutlandersEntities {
    public static final HashMap<EntityType<? extends LivingEntity>, DefaultAttributeContainer.Builder> defaultAttributesMap = new HashMap<>();

    public static final EntityType<PersonEntity> PERSON = register("person", PersonEntity::createPerson, SpawnGroup.MISC, b -> b
                    .dimensions(0.6F, 1.8F)
                    .eyeHeight(1.62F)
                    .vehicleAttachment(PlayerLikeEntity.VEHICLE_ATTACHMENT)
                    .maxTrackingRange(32)
                    .trackingTickInterval(2),
            PersonEntity.createPersonAttributes()
    );

    public static <TEntity extends LivingEntity> EntityType<TEntity> register(String name, EntityType.EntityFactory<TEntity> factory, SpawnGroup spawnGroup, Function<EntityType.Builder<TEntity>, EntityType.Builder<TEntity>> builderFunction, DefaultAttributeContainer.Builder defaultAttributes) {
        var identifier = idOf(name);
        var key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, identifier);
        EntityType.Builder<TEntity> builder = EntityType.Builder.create(factory, spawnGroup);
        builder = builderFunction.apply(builder);
        var entityType = Registry.register(Registries.ENTITY_TYPE, identifier, builder.build(key));

        defaultAttributesMap.put(entityType, defaultAttributes);

        return entityType;
    }

    public static void init() {
        // Entities
        for (EntityType<? extends LivingEntity> entityType : defaultAttributesMap.keySet()) {
            DefaultAttributeContainer.Builder attributesBuilder = defaultAttributesMap.get(entityType);
            //noinspection DataFlowIssue
            FabricDefaultAttributeRegistry.register(entityType, attributesBuilder);
        }
    }
}
