package com.example.robohonreception.patient;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PatientController {
    private final String BASE_URL = "http://rasp0.local:8080/";
    private final String PATIENT_URL = BASE_URL + "patient/";
    private final String APPOINT_URL = BASE_URL + "appoint/";

    com.example.robohonreception.patient.Response<Patient> getPatient(int id) {
        return getTClass(Patient.class, PATIENT_URL + id);
    }

    com.example.robohonreception.patient.Response<Patient> getPatient(String id) {
        return getTClass(Patient.class, PATIENT_URL + id);
    }

    com.example.robohonreception.patient.Response<Appoint> getAppoint(int id) {
        return getTClass(Appoint.class, APPOINT_URL + id);
    }

    com.example.robohonreception.patient.Response<Appoint> getAppoint(String id) {
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


