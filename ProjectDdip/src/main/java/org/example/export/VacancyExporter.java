package org.example.export;

import org.example.model.Vacancy;
import java.io.IOException;
import java.util.List;

public interface VacancyExporter {
    void export(List<Vacancy> vacancies, String filename) throws IOException;
} 