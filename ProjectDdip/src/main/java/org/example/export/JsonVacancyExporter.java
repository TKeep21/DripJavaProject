package org.example.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.model.Vacancy;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonVacancyExporter implements VacancyExporter {
    private final ObjectMapper objectMapper;

    public JsonVacancyExporter() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void export(List<Vacancy> vacancies, String filename) throws IOException {
        objectMapper.writeValue(new File(filename + ".json"), vacancies);
    }
} 