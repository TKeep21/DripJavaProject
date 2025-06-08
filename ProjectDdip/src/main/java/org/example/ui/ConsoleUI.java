package org.example.ui;

import org.example.export.CsvVacancyExporter;
import org.example.export.HtmlVacancyExporter;
import org.example.export.JsonVacancyExporter;
import org.example.export.VacancyExporter;
import org.example.model.Vacancy;
import org.example.repository.VacancyRepository;
import org.example.service.VacancyParserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class ConsoleUI {
    private final VacancyRepository repository;
    private final VacancyParserService parserService;
    private final Scanner scanner;
    private final Map<String, VacancyExporter> exporters;
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";

    public ConsoleUI(VacancyRepository repository, VacancyParserService parserService) {
        this.repository = repository;
        this.parserService = parserService;
        this.scanner = new Scanner(System.in);
        this.exporters = new HashMap<>();
        exporters.put("1", new CsvVacancyExporter());
        exporters.put("2", new JsonVacancyExporter());
        exporters.put("3", new HtmlVacancyExporter());
    }

    public void start() {
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                if (!processChoice(choice)) {
                    break;
                }
            } catch (SQLException | IOException e) {
                System.err.println(ANSI_YELLOW + "Ошибка: " + e.getMessage() + ANSI_RESET);
            }
        }
    }

    private void printMenu() {
        System.out.println("\n" + ANSI_CYAN + "=== МЕНЮ ===" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "1) Показать все вакансии" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "2) Поиск по ключевому слову" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "3) Поиск по городу" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "4) Поиск по компании" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "5) Поиск по зарплате" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "6) Экспортировать вакансии" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "7) Запустить парсер вручную" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "8) Аналитика и статистика вакансий" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "0) Выход" + ANSI_RESET);
        System.out.print(ANSI_GREEN + "Выберите пункт: " + ANSI_RESET);
    }

    private boolean processChoice(String choice) throws SQLException, IOException {
        switch (choice) {
            case "1" -> showAllVacancies();
            case "2" -> searchByKeyword();
            case "3" -> searchByCity();
            case "4" -> searchByCompany();
            case "5" -> searchBySalary();
            case "6" -> exportVacancies();
            case "7" -> runParser();
            case "8" -> showAnalytics();
            case "0" -> {
                return false;
            }
            default -> System.out.println(ANSI_YELLOW + "Неверный пункт меню" + ANSI_RESET);
        }
        return true;
    }

    private void showAllVacancies() throws SQLException {
        List<Vacancy> vacancies = repository.findAll();
        if (vacancies.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Вакансий не найдено." + ANSI_RESET);
            return;
        }
        VacancyFormatter.printVacanciesTable(vacancies);
    }

    private void searchByKeyword() throws SQLException {
        System.out.print(ANSI_GREEN + "Введите ключевое слово: " + ANSI_RESET);
        String keyword = scanner.nextLine().trim();
        if (keyword.isBlank()) {
            System.out.println(ANSI_YELLOW + "Ключевое слово не может быть пустым." + ANSI_RESET);
            return;
        }
        List<Vacancy> vacancies = repository.searchByKeyword(keyword);
        if (vacancies.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Вакансий не найдено по ключевому слову." + ANSI_RESET);
            return;
        }
        VacancyFormatter.printVacanciesTable(vacancies);
    }

    private void searchByCity() throws SQLException {
        System.out.print(ANSI_GREEN + "Введите город: " + ANSI_RESET);
        String city = scanner.nextLine().trim();
        if (city.isBlank()) {
            System.out.println(ANSI_YELLOW + "Город не может быть пустым." + ANSI_RESET);
            return;
        }
        List<Vacancy> vacancies = repository.findByCity(city);
        if (vacancies.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Вакансий не найдено в указанном городе." + ANSI_RESET);
            return;
        }
        VacancyFormatter.printVacanciesTable(vacancies);
    }

    private void searchByCompany() throws SQLException {
        System.out.print(ANSI_GREEN + "Введите компанию: " + ANSI_RESET);
        String company = scanner.nextLine().trim();
        if (company.isBlank()) {
            System.out.println(ANSI_YELLOW + "Компания не может быть пустой." + ANSI_RESET);
            return;
        }
        List<Vacancy> vacancies = repository.findByCompany(company);
        if (vacancies.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Вакансий не найдено для указанной компании." + ANSI_RESET);
            return;
        }
        VacancyFormatter.printVacanciesTable(vacancies);
    }

    private void searchBySalary() throws SQLException {
        System.out.print(ANSI_GREEN + "Введите минимальную зарплату (или Enter, чтобы не указывать): " + ANSI_RESET);
        String minStr = scanner.nextLine().trim();
        System.out.print(ANSI_GREEN + "Введите максимальную зарплату (или Enter, чтобы не указывать): " + ANSI_RESET);
        String maxStr = scanner.nextLine().trim();

        boolean hasMin = !minStr.isEmpty();
        boolean hasMax = !maxStr.isEmpty();
        if (!hasMin && !hasMax) {
            System.out.println(ANSI_YELLOW + "Нужно указать хотя бы минимальную или максимальную зарплату." + ANSI_RESET);
            return;
        }

        try {
            List<Vacancy> vacancies;
            if (hasMin && hasMax) {
                int minSalary = Integer.parseInt(minStr);
                int maxSalary = Integer.parseInt(maxStr);
                vacancies = repository.findBySalaryBetween(minSalary, maxSalary);
            } else if (hasMin) {
                int minSalary = Integer.parseInt(minStr);
                vacancies = repository.findBySalaryFromGreaterEqual(minSalary);
            } else {
                int maxSalary = Integer.parseInt(maxStr);
                vacancies = repository.findBySalaryToLessEqual(maxSalary);
            }

            if (vacancies.isEmpty()) {
                System.out.println(ANSI_YELLOW + "Вакансий не найдено по указанному диапазону зарплат." + ANSI_RESET);
                return;
            }
            VacancyFormatter.printVacanciesTable(vacancies);
        } catch (NumberFormatException ex) {
            System.out.println(ANSI_YELLOW + "Значения зарплаты должны быть корректными целыми числами." + ANSI_RESET);
        }
    }

    private void exportVacancies() throws SQLException, IOException {
        List<Vacancy> vacancies = repository.findAll();
        if (vacancies.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Вакансий нет для экспорта." + ANSI_RESET);
            return;
        }

        System.out.println(ANSI_CYAN + "\nВыберите формат экспорта:" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "1) CSV" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "2) JSON" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "3) HTML" + ANSI_RESET);
        System.out.print(ANSI_GREEN + "Пункт: " + ANSI_RESET);

        String fmt = scanner.nextLine().trim();
        System.out.print(ANSI_GREEN + "Укажите имя файла (без расширения): " + ANSI_RESET);
        String baseName = scanner.nextLine().trim();

        if (baseName.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Имя файла не может быть пустым." + ANSI_RESET);
            return;
        }

        VacancyExporter exporter = exporters.get(fmt);
        if (exporter != null) {
            exporter.export(vacancies, baseName);
            String extension = fmt.equals("1") ? "csv" : fmt.equals("2") ? "json" : "html";
            System.out.println(ANSI_GREEN + "Вакансии успешно экспортированы в " + extension.toUpperCase() + " файл: " + baseName + "." + extension + ANSI_RESET);
        } else {
            System.out.println(ANSI_YELLOW + "Неверный формат экспорта" + ANSI_RESET);
        }
    }

    private void runParser() {
        System.out.println(ANSI_CYAN + "\nЗапуск парсера..." + ANSI_RESET);
        parserService.parseAndSaveAll();
        System.out.println(ANSI_GREEN + "Парсинг завершён." + ANSI_RESET);
    }

    private void showAnalytics() throws SQLException {
        while (true) {
            System.out.println(ANSI_CYAN + "\n=== Аналитика и статистика ===" + ANSI_RESET);
            System.out.println(ANSI_BLUE + "1) Количество вакансий по городам" + ANSI_RESET);
            System.out.println(ANSI_BLUE + "2) Средняя минимальная зарплата по городам" + ANSI_RESET);
            System.out.println(ANSI_BLUE + "3) Средняя максимальная зарплата по городам" + ANSI_RESET);
            System.out.println(ANSI_BLUE + "4) Сортировка вакансий" + ANSI_RESET);
            System.out.println(ANSI_YELLOW + "0) Назад в главное меню" + ANSI_RESET);
            System.out.print(ANSI_GREEN + "Выберите пункт: " + ANSI_RESET);

            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1" -> {
                    Map<String, Integer> byCity = repository.countByCity();
                    System.out.println(ANSI_CYAN + "\nКоличество активных вакансий по городам:" + ANSI_RESET);
                    byCity.forEach((city, count) ->
                            System.out.printf(ANSI_BLUE + "  %s: %d%n" + ANSI_RESET, city, count)
                    );
                }
                case "2" -> {
                    Map<String, Double> avgFromCity = repository.avgSalaryFromByCity();
                    System.out.println(ANSI_CYAN + "\nСредняя минимальная зарплата по городам:" + ANSI_RESET);
                    avgFromCity.forEach((city, avg) ->
                            System.out.printf(ANSI_BLUE + "  %s: %.2f%n" + ANSI_RESET, city, avg)
                    );
                }
                case "3" -> {
                    Map<String, Double> avgToCity = repository.avgSalaryToByCity();
                    System.out.println(ANSI_CYAN + "\nСредняя максимальная зарплата по городам:" + ANSI_RESET);
                    avgToCity.forEach((city, avg) ->
                            System.out.printf(ANSI_BLUE + "  %s: %.2f%n" + ANSI_RESET, city, avg)
                    );
                }
                case "4" -> showSortingOptions();
                case "0" -> {
                    return;
                }
                default -> System.out.println(ANSI_YELLOW + "Неверный пункт. Возврат в меню аналитики." + ANSI_RESET);
            }
        }
    }

    private void showSortingOptions() throws SQLException {
        System.out.println(ANSI_CYAN + "\nВыберите критерий сортировки:" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "1) Дата публикации (сначала новые)" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "2) Дата публикации (сначала старые)" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "3) Зарплата (сначала высокая)" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "4) Зарплата (сначала низкая)" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "5) Компания (по алфавиту)" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "6) Город (по алфавиту)" + ANSI_RESET);
        System.out.print(ANSI_GREEN + "Пункт: " + ANSI_RESET);

        String opt = scanner.nextLine().trim();
        List<Vacancy> vacancies;
        
        switch (opt) {
            case "1" -> {
                vacancies = repository.findAllOrderByPublishedDateDesc();
                System.out.println(ANSI_CYAN + "\n=== Сортировка по дате (сначала новые) ===" + ANSI_RESET);
            }
            case "2" -> {
                vacancies = repository.findAllOrderByPublishedDateAsc();
                System.out.println(ANSI_CYAN + "\n=== Сортировка по дате (сначала старые) ===" + ANSI_RESET);
            }
            case "3" -> {
                vacancies = repository.findAllOrderBySalaryDesc();
                System.out.println(ANSI_CYAN + "\n=== Сортировка по зарплате (сначала высокая) ===" + ANSI_RESET);
            }
            case "4" -> {
                vacancies = repository.findAllOrderBySalaryAsc();
                System.out.println(ANSI_CYAN + "\n=== Сортировка по зарплате (сначала низкая) ===" + ANSI_RESET);
            }
            case "5" -> {
                vacancies = repository.findAllOrderByCompany();
                System.out.println(ANSI_CYAN + "\n=== Сортировка по компании (по алфавиту) ===" + ANSI_RESET);
            }
            case "6" -> {
                vacancies = repository.findAllOrderByCity();
                System.out.println(ANSI_CYAN + "\n=== Сортировка по городу (по алфавиту) ===" + ANSI_RESET);
            }
            default -> {
                System.out.println(ANSI_YELLOW + "Неверный пункт. Возврат в меню аналитики." + ANSI_RESET);
                return;
            }
        }
        
        if (vacancies.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Вакансий нет для сортировки." + ANSI_RESET);
            return;
        }
        
        VacancyFormatter.printVacanciesTable(vacancies);
    }
} 