package com.example.robohonreception.patient;

import com.google.gson.annotations.SerializedName;

public class Appoint {
    @SerializedName("appoint_time")
    public String appointTime;

    @SerializedName("patient_id1")
    public String patientID1;

    @SerializedName("patient_id2")
    public String patientID2;

    @SerializedName("patient_id3")
    public String patientID3;

    @SerializedName("patient_id4")
    public String patientID4;

    @SerializedName("patient_name")
    public String patientName;

    @SerializedName("patient_kana_name")
    public String patientKanaName;

    public Appoint(String appointTime, String patientID1, String patientID2, String patientID3, String patientID4, String patientName, String patientKanaName) {
        this.appointTime = appointTime;
        this.patientID1 = patientID1;
        this.patientID2 = patientID2;
        this.patientID3 = patientID3;
        this.patientID4 = patientID4;
        this.patientName = patientName;
        this.patientKanaName = patientKanaName;
    }
}
