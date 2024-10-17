package com.hospitalcrud.dao.mappers;

import com.hospitalcrud.dao.model.Credential;
import com.hospitalcrud.dao.model.Patient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PatientRowMapper {
  /*  public Patient mapRow(String s) {
        //Split the line of the file
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return null;
    }*/

    private DateTimeFormatter formatter;

    public Patient mapRow(String s) {
        String[] parts = s.split(";");
        if (parts.length >= 5) {
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();

            LocalDate birthDate = LocalDate.parse(parts[2].trim(), formatter);
            String phoneNumber = parts[3].trim();
            Credential credential = new Credential(parts[4].trim(), parts[5].trim(), id);
            return new Patient(id, name, birthDate, phoneNumber, credential);
        }
        return null;
    }

    public int toStringTextFile(Patient patient) {
        return 0;
    }
}
