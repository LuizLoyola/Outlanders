package dev.luizloyola.outlanders.entity.data;

import net.minecraft.util.Formatting;

public enum Gender {
    MALE, FEMALE;

    public static Gender random() {
        return Math.random() < 0.5 ? MALE : FEMALE;
    }

    public static Gender fromChar(String c) {
        return switch (c) {
            case "M" -> MALE;
            case "F" -> FEMALE;
            default -> throw new IllegalArgumentException("Invalid gender character: " + c);
        };
    }

    public char asChar() {
        return switch (this) {
            case MALE -> 'M';
            case FEMALE -> 'F';
        };
    }

    public Formatting colorFormatting() {
        return switch (this) {
            case MALE -> Formatting.BLUE;
            case FEMALE -> Formatting.LIGHT_PURPLE;
        };
    }

    public <T> T choose(T ifMale, T ifFemale) {
        return this == MALE ? ifMale : ifFemale;
    }
}
