package com.hospitalcrud.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Getter
@Setter
@AllArgsConstructor

public class Patient {
    private int id;
    private String name;
    private LocalDate birthDate;
    private String phone;
    private Credential credential;

    public Patient(LocalDate birthDate, Credential credential, int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.credential = credential;
        this.phone = phone;
    }


}
