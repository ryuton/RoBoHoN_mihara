package com.example.robohonreception.patient;

import com.google.gson.annotations.SerializedName;

public class Response<T> {
    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    @SerializedName("result")
    public T result;

    public Response(String status, String message, T result) {
        this.status = status;
        this.message = message;
        this.result = result;
    }
}
