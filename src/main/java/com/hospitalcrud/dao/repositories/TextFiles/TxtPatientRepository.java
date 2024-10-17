package com.hospitalcrud.dao.repositories.TextFiles;

import com.hospitalcrud.config.Configuration;
import com.hospitalcrud.dao.mappers.PatientRowMapper;
import com.hospitalcrud.dao.model.Patient;
import com.hospitalcrud.dao.repositories.PatientRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Repository
@Profile("txtFiles")
public class TxtPatientRepository implements PatientRepository {

    private final PatientRowMapper patientRowMapper;
    private final List<Patient> patients;
    private final DateTimeFormatter dateFormatter;

    public TxtPatientRepository(PatientRowMapper patientRowMapper) {
        this.patientRowMapper = patientRowMapper;
        this.patients = new ArrayList<>();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Calcula el próximo ID y lo guarda en el archivo de propiedades
        int nextId = calculateNextId();
        Configuration.getInstance().setProperty("nextId", String.valueOf(nextId));
    }

    private int calculateNextId() {
        List<String> lines = readFile();
        if (lines.isEmpty()) {
            return 1; // Si el archivo está vacío, comenzamos en 1
        }
        String lastLine = lines.get(lines.size() - 1);
        Patient lastPatient = patientRowMapper.mapRow(lastLine);
        return lastPatient.getId() + 1;
    }

    private void writePatientsToFile(List<Patient> patientsList) throws IOException {
        Path filePath = Paths.get(Configuration.getInstance().getProperty("patientsFilePath"));
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Patient p : patientsList) {
                String patientData = String.format(
                        "%d;%s;%s;%s;%s%n",
                        p.getId(),
                        p.getName(),
                        p.getBirthDate().format(dateFormatter),
                        p.getCredential().getUsername(),
                        p.getCredential().getPassword()
                );
                writer.write(patientData);
            }
        }
    }

    private List<String> readFile() {
        List<String> lines = new ArrayList<>();
        Path file = Paths.get(Configuration.getInstance().getProperty("patientsFilePath"));
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
    public List<Patient> getAll() {
        List<Patient> patients = new ArrayList<>();
        List<String> lines = readFile();
        for (String line : lines) {
            patients.add(patientRowMapper.mapRow(line));
        }
        return patients;
    }

    @Override
    public int add(Patient patient) {
        int newId = Integer.parseInt(Configuration.getInstance().getProperty("nextId"));
        patient.setId(newId);

        try {
            Path filePath = Paths.get(Configuration.getInstance().getProperty("patientsFilePath"));
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.APPEND)) {
                writer.write(patientRowMapper.toStringTextFile(patient) + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Actualizamos el próximo ID en las propiedades
        Configuration.getInstance().setProperty("nextId", String.valueOf(newId + 1));
        return newId;
    }

    @Override
    public void update(Patient updatedPatient) {
        List<Patient> allPatients = getAll();
        for (int i = 0; i < allPatients.size(); i++) {
            if (allPatients.get(i).getId() == updatedPatient.getId()) {
                allPatients.set(i, updatedPatient);
                break;
            }
        }
        try {
            writePatientsToFile(allPatients);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int idToDelete, boolean confirm) {
        if (!confirm) return;

        List<Patient> allPatients = getAll();
        allPatients.removeIf(patient -> patient.getId() == idToDelete);
        try {
            writePatientsToFile(allPatients);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
