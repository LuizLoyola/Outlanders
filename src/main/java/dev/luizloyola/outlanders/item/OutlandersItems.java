package dev.luizloyola.outlanders.item;

import dev.luizloyola.outlanders.component.OutlandersComponents;
import dev.luizloyola.outlanders.component.UuidComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

import java.util.function.Function;

import static dev.luizloyola.outlanders.util.IdentifierUtils.idOf;

public class OutlandersItems {
    public static final Item COMMAND_STICK = register("command_stick", CommandStickItem::new, new Item.Settings()
            .maxCount(1)
            .rarity(Rarity.EPIC)
            .component(OutlandersComponents.BOUND_PERSON, UuidComponent.empty())
    );

    public static <TItem extends Item> TItem register(String name, Function<Item.Settings, TItem> itemFactory, Item.Settings settings) {
        var identifier = idOf(name);
        var key = RegistryKey.of(RegistryKeys.ITEM, identifier);
        var item = itemFactory.apply(settings.registryKey(key));

        if (item instanceof BlockItem blockItem) {
            blockItem.appendBlocks(Item.BLOCK_ITEMS, item);
        }

        return Registry.register(Registries.ITEM, identifier, item);
    }

    public static void init() {
        // Method intentionally left blank
    }
}
