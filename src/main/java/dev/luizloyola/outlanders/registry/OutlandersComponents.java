package dev.luizloyola.outlanders.registry;

import com.mojang.serialization.Codec;
import dev.luizloyola.outlanders.component.UuidComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static dev.luizloyola.outlanders.util.IdentifierUtils.idOf;

public class OutlandersComponents {
    public static final ComponentType<UuidComponent> BOUND_PERSON = register("bound_person", UuidComponent.CODEC);

    private static <T> ComponentType<T> register(String name, Codec<T> codec) {
        var identifier = idOf(name);
        return Registry.register(Registries.DATA_COMPONENT_TYPE, identifier, ComponentType.<T>builder().codec(codec).build());
    }

    public static void init() {
        // Method intentionally left blank
    }
}
