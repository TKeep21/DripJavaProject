package org.example.export;

import org.example.model.Vacancy;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Экспортер вакансий в CSV формат.
 * Поддерживает экранирование специальных символов и форматирование дат.
 */
public class CsvVacancyExporter implements VacancyExporter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void export(List<Vacancy> vacancies, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename + ".csv"))) {
            // Заголовки
            writer.println("ID,Должность,Компания,Город,Зарплата от,Зарплата до,Валюта,График работы,Дата публикации,Ссылка,Источник");

            // Данные
            for (Vacancy v : vacancies) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    v.getId() != null ? v.getId() : "",
                    escapeCsv(v.getTitle()),
                    escapeCsv(v.getCompany()),
                    escapeCsv(v.getCity()),
                    v.getSalaryFrom() != null ? v.getSalaryFrom() : "",
                    v.getSalaryTo() != null ? v.getSalaryTo() : "",
                    escapeCsv(v.getCurrency()),
                    escapeCsv(v.getWorkSchedule()),
                    v.getPublishedDate() != null ? v.getPublishedDate().format(DATE_FORMATTER) : "",
                    escapeCsv(v.getUrl()),
                    escapeCsv(v.getSource())
                );
            }
        }
    }

    /**
     * Обрабатывает запятые, кавычки и переносы строк.
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
} 