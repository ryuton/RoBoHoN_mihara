package com.example.robohonreception.patient;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PatientController {
    private final String BASE_URL = "http://192.168.1.102:8080/";
    private final String PATIENT_URL = BASE_URL + "patient/";
    private final String APPOINT_URL = BASE_URL + "appoint/";
    private final String GPIO_URL = BASE_URL + "gpio/";

    public com.example.robohonreception.patient.Response<Patient> getPatient(int id) {
        return getTClass(Patient.class, PATIENT_URL + id);
    }

    public com.example.robohonreception.patient.Response<Integer> getGPIO() {
        return getTClass(Integer.class, GPIO_URL);
    }

    public com.example.robohonreception.patient.Response<Patient> getPatient(String id) {
        return getTClass(Patient.class, PATIENT_URL + id);
    }

    public void getAppoint(int id) {
        Request request = new Request.Builder()
                .url(APPOINT_URL + id)
                .get()
                .build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("PatientController", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                ParameterizedType type = new GenericOf<>(com.example.robohonreception.patient.Response.class, Appoint.class);
                com.example.robohonreception.patient.Response<Appoint> res = null;
                res = gson.fromJson(response.body().string(), type);
                Log.d("PatientController", res.toString());
            }
        });
    }

    public com.example.robohonreception.patient.Response<Appoint> getAppoint(String id) {
        return getTClass(Appoint.class, APPOINT_URL + id);
    }

    private String httpGet(String url) throws IOException {

        final Request request = new Request.Builder().url(url).build();
        final OkHttpClient client = new OkHttpClient.Builder().build();
        Response response = client.newCall(request).execute();
        //同期呼び出し
        return Objects.requireNonNull(response.body()).string();
    }

    private <T> com.example.robohonreception.patient.Response<T> getTClass(Class<T> tClass, String url) {
        Gson gson = new Gson();
        ParameterizedType type = new GenericOf<>(com.example.robohonreception.patient.Response.class, tClass);
        com.example.robohonreception.patient.Response<T> response = null;
        try {
            String res = httpGet(url);
            response = gson.fromJson(res, type);

        } catch (Exception e) {
            Log.d("PatientController", e.toString());
        }

        return response;
    }

    class GenericOf<X, Y> implements ParameterizedType {

        private final Class<X> container;
        private final Class<Y> wrapped;

        GenericOf(Class<X> container, Class<Y> wrapped) {
            this.container = container;
            this.wrapped = wrapped;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{wrapped};
        }

        @Override
        public Type getRawType() {
            return container;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}


