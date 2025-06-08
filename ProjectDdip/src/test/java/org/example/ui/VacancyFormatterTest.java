package org.example.ui;

import org.example.model.Vacancy;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VacancyFormatterTest {

    @Test
    void testPrintVacanciesTable() {
        // Arrange
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        VacancyFormatter formatter = new VacancyFormatter();
        List<Vacancy> vacancies = Arrays.asList(
            createTestVacancy("Java Developer", "Company A", "Moscow", 100000, 150000, "RUR"),
            createTestVacancy("Python Developer", "Company B", "St. Petersburg", 120000, 180000, "RUR")
        );

        // Act
        formatter.printVacanciesTable(vacancies);
        String output = outContent.toString();

        // Assert
        assertNotNull(output);
        assertTrue(output.contains("Java Developer"));
        assertTrue(output.contains("Python Developer"));
        assertTrue(output.contains("Company A"));
        assertTrue(output.contains("Company B"));
        
        // Restore System.out
        System.setOut(System.out);
    }

    @Test
    void testPrintVacanciesTableWithNullValues() {
        // Подготовка
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        VacancyFormatter formatter = new VacancyFormatter();
        List<Vacancy> vacancies = Arrays.asList(
                createTestVacancy("Java Developer", "Company A", "Москва", null, null, null),
                createTestVacancy("Python Developer", "Company B", "Санкт-Петербург", 120000, null, "RUR")
        );

        // Выполнение
        formatter.printVacanciesTable(vacancies);

        // Проверка
        String output = removeAnsiColors(outContent.toString());
        assertTrue(output.contains("не указана"), "Должно выводить «не указана» для null-зарплаты");
        assertTrue(output.contains("120000RUR"), "Должно конкатенировать salaryFrom и currency");

        // Восстановление
        System.setOut(System.out);
    }


    private Vacancy createTestVacancy(String title, String company, String city, 
                                    Integer salaryFrom, Integer salaryTo, String currency) {
        Vacancy vacancy = new Vacancy();
        vacancy.setTitle(title);
        vacancy.setCompany(company);
        vacancy.setCity(city);
        vacancy.setSalaryFrom(salaryFrom);
        vacancy.setSalaryTo(salaryTo);
        vacancy.setCurrency(currency);
        vacancy.setPublishedDate(LocalDateTime.now());
        vacancy.setCreatedAt(LocalDateTime.now());
        return vacancy;
    }

    private String removeAnsiColors(String text) {
        return text.replaceAll("\\u001B\\[[;\\d]*m", "");
    }
} 