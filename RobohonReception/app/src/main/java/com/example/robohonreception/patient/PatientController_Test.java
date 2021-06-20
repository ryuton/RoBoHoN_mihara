package com.example.robohonreception.patient;

public class PatientController_Test {

    public static void main(String[] args) {

        PatientController patientController = new PatientController();

        try {
            Response response = patientController.getPatient(1);
            Response test = patientController.getAppoint("1");
            System.out.print(response.message);
        }catch (Exception e) {
            System.out.print(e);
        }
    }
}
