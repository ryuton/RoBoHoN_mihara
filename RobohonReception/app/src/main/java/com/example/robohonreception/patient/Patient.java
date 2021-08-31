package com.example.robohonreception.patient;

import com.google.gson.annotations.SerializedName;

public class Patient {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("kana_name")
    public String kanaName;

    @SerializedName("birth_date")
    public String birthDate;

    public Patient(String id, String name, String kanaName, String birthDate) {
        this.id = id;
        this.name = name;
        this.kanaName = kanaName;
        this.birthDate = birthDate;
    }
}
