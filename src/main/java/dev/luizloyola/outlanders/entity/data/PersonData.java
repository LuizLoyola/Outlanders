package dev.luizloyola.outlanders.entity.data;


import com.google.gson.Gson;

public record PersonData(Gender gender, String name, String skinName) {
    private static final Gson gson = new Gson();

    public static PersonData fromJson(final String json) {
        return gson.fromJson(json, PersonData.class);
    }


    public String toJson() {
        return gson.toJson(this);
    }
}