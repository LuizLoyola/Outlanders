package dev.luizloyola.outlanders.entity;

import net.minecraft.util.math.random.Random;

import java.util.List;

public record PersonIdentity(Gender gender, String name, String skinName) {
    public static PersonIdentity random() {
        var gender = Gender.random();

        var maleSkins = List.of("kai", "noor", "steve", "sunny", "zuri");
        var femaleSkins = List.of("alex", "ari", "efe", "makena");

        var skinPool = gender.choose(maleSkins, femaleSkins);
        var random = Random.create();
        var randomSkinName = skinPool.get(random.nextInt(skinPool.size()));

        var capitalizedName = randomSkinName.substring(0, 1).toUpperCase() + randomSkinName.substring(1);

        return new PersonIdentity(gender, capitalizedName, randomSkinName);
    }
}