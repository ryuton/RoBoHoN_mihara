package com.example.robohonreception.patient;

import com.google.gson.annotations.SerializedName;

public class Appoint {
    @SerializedName("appoint_time")
    public String appointTime;

    @SerializedName("patient_id")
    public String patientID;

    @SerializedName("patient_name")
    public String patientName;

    @SerializedName("patient_kana_name")
    public String patientKanaName;

    public Appoint(String appointTime, String patientID, String patientName, String patientKanaName) {
        this.appointTime = appointTime;
        this.patientID = patientID;
        this.patientName = patientName;
        this.patientKanaName = patientKanaName;
    }
}
