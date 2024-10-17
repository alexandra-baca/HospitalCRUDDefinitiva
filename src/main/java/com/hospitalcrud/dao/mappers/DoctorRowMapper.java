package com.hospitalcrud.dao.mappers;

import com.hospitalcrud.dao.model.Doctor;

public class DoctorRowMapper {

    public Doctor map(String line) {
        String[] fields = line.split(";");
        int id = Integer.parseInt(fields[0].trim());
        String name = fields[1].trim();


        return new Doctor(id, name);
    }

    public String toStringTextFile(Doctor doctor) {
        return String.format(
                "%d;%s;%s",
                doctor.getId(),
                doctor.getName()
        );
    }
}
