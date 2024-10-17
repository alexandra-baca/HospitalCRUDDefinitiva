package com.hospitalcrud.dao.repositories.TextFiles;

import com.hospitalcrud.config.Configuration;
import com.hospitalcrud.dao.mappers.DoctorRowMapper;
import com.hospitalcrud.dao.model.Doctor;
import com.hospitalcrud.dao.repositories.DoctorRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("txtFiles")
public class TxtDoctorRepository implements DoctorRepository {

    private final DoctorRowMapper doctorRowMapper;
    private final List<Doctor> doctors;

    public TxtDoctorRepository(DoctorRowMapper doctorRowMapper) {
        this.doctorRowMapper = doctorRowMapper;
        this.doctors = new ArrayList<>();

        // Calcula el próximo ID y lo guarda en el archivo de propiedades
        int nextId = calculateNextId();
        Configuration.getInstance().setProperty("nextIdDoctor", String.valueOf(nextId));
    }

    private int calculateNextId() {
        List<String> lines = readFile();
        if (lines.isEmpty()) {
            return 1; // Si el archivo está vacío, comenzamos en 1
        }
        String lastLine = lines.get(lines.size() - 1);
        Doctor lastDoctor = doctorRowMapper.map(lastLine);
        return lastDoctor.getId() + 1;
    }

    private void writeDoctorsToFile(List<Doctor> doctorsList) throws IOException {
        Path filePath = Paths.get(Configuration.getInstance().getProperty("doctorsFilePath"));
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Doctor d : doctorsList) {
                writer.write(doctorRowMapper.toStringTextFile(d) + System.lineSeparator());
            }
        }
    }

    private List<String> readFile() {
        List<String> lines = new ArrayList<>();
        Path file = Paths.get(Configuration.getInstance().getProperty("doctorsFilePath"));
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    @Override
    public List<Doctor> getAll() {
        List<Doctor> doctors = new ArrayList<>();
        List<String> lines = readFile();
        for (String line : lines) {
            doctors.add(doctorRowMapper.map(line));
        }
        return doctors;
    }

    @Override
    public int add(Doctor doctor) {
        int newId = Integer.parseInt(Configuration.getInstance().getProperty("nextIdDoctor"));
        doctor.setId(newId);

        try {
            Path filePath = Paths.get(Configuration.getInstance().getProperty("doctorsFilePath"));
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.APPEND)) {
                writer.write(doctorRowMapper.toStringTextFile(doctor) + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Configuration.getInstance().setProperty("nextIdDoctor", String.valueOf(newId + 1));
        return newId;
    }

    @Override
    public void update(Doctor updatedDoctor) {
        List<Doctor> allDoctors = getAll();
        for (int i = 0; i < allDoctors.size(); i++) {
            if (allDoctors.get(i).getId() == updatedDoctor.getId()) {
                allDoctors.set(i, updatedDoctor);
                break;
            }
        }
        try {
            writeDoctorsToFile(allDoctors);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int idToDelete, boolean confirm) {
        if (!confirm) return;

        List<Doctor> allDoctors = getAll();
        allDoctors.removeIf(doctor -> doctor.getId() == idToDelete);
        try {
            writeDoctorsToFile(allDoctors);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
