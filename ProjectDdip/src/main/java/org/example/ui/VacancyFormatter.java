package org.example.ui;

import org.example.model.Vacancy;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VacancyFormatter {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");

    public static void printVacanciesTable(List<Vacancy> vacancies) {
        if (vacancies.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Вакансий не найдено." + ANSI_RESET);
            return;
        }

        System.out.println("\nНайдено вакансий: " + vacancies.size());
        System.out.println("┌───────────────────────────────────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ " + ANSI_CYAN + String.format("%-40s %-20s %-15s %-15s %-15s %-10s",
                "Должность", "Компания", "Город", "Зарплата", "График работы", "Дата") + ANSI_RESET + "│");
        System.out.println("├───────────────────────────────────────────────────────────────────────────────────────────────────────────┤");

        for (Vacancy v : vacancies) {
            String salaryStr = formatSalary(v);
            String dateStr = v.getPublishedDate() != null ? v.getPublishedDate().format(DATE_FORMATTER) : "—";

            System.out.println("│ " + String.format("%-40s %-20s %-15s %-15s %-15s %-10s",
                    truncate(v.getTitle(), 37),
                    truncate(v.getCompany(), 17),
                    truncate(v.getCity(), 12),
                    truncate(salaryStr, 12),
                    truncate(v.getWorkSchedule(), 12),
                    dateStr) + "│");
            System.out.println("│ " + ANSI_GREEN + "Ссылка: " + ANSI_RESET + v.getUrl() + " │");
            System.out.println("├───────────────────────────────────────────────────────────────────────────────────────────────────────────┤");
        }
        System.out.println("└───────────────────────────────────────────────────────────────────────────────────────────────────────────┘");
    }

    private static String formatSalary(Vacancy v) {
        if (v.getSalaryFrom() == null && v.getSalaryTo() == null) {
            return "не указана";
        }
        StringBuilder sb = new StringBuilder();
        if (v.getSalaryFrom() != null) {
            sb.append(v.getSalaryFrom());
        }
        if (v.getSalaryFrom() != null && v.getSalaryTo() != null) {
            sb.append(" - ");
        }
        if (v.getSalaryTo() != null) {
            sb.append(v.getSalaryTo());
        }
        if (v.getCurrency() != null) {
            sb.append(v.getCurrency());
        }
        return sb.toString();
    }

    private static String truncate(String str, int maxLength) {
        if (str == null) return "—";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
} 