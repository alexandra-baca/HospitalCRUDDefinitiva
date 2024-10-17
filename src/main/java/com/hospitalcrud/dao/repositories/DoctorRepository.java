package com.hospitalcrud.dao.repositories;

import com.hospitalcrud.dao.model.Doctor;

import java.util.List;

public interface DoctorRepository {
    public List<Doctor> getAll();

    int add(Doctor doctor);

    void update(Doctor updatedDoctor);

    void delete(int idToDelete, boolean confirm);
}
