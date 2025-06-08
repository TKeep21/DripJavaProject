package org.example;

import org.example.config.Config;
import org.example.db.DatabaseManager;
import org.example.parser.HhVacancyParser;
import org.example.parser.SuperJobVacancyParser;
import org.example.parser.VacancyParser;
import org.example.repository.VacancyRepository;
import org.example.service.VacancyParserService;
import org.example.ui.ConsoleUI;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Консольное приложение, реализующее:
 * 1) Автопарсинг вакансий из HH.ru и SuperJob.ru
 * 2) Хранение в PostgreSQL
 * 3) Просмотр, фильтрацию, сортировку
 * 4) Экспорт в CSV, JSON, HTML
 */
public class Main {
    public static void main(String[] args) {
        VacancyRepository repo = new VacancyRepository();
        
        String parserKeys = Config.get("parsers");
        List<VacancyParser> parsers = new ArrayList<>();
        if (parserKeys != null) {
            for (String key : parserKeys.split(",")) {
                switch (key.trim()) {
                    case "hh" -> parsers.add(new HhVacancyParser());
                    case "superjob" -> parsers.add(new SuperJobVacancyParser());
                    default -> System.err.println("Unknown parser key: " + key);
                }
            }
        }
        
        VacancyParserService parserService = new VacancyParserService(repo, parsers);
        parserService.parseAndSaveAll();

        // Планировщик для автоматического обновления вакансий каждый час
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("[" + LocalDateTime.now() + "] [Scheduler] Запуск автоматического парсинга...");
            try {
                parserService.parseAndSaveAll();
            } catch (Exception e) {
                System.err.println("[Scheduler] Ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.HOURS);

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            ConsoleUI ui = new ConsoleUI(repo, parserService);
            ui.start();
            
            scheduler.shutdownNow();
            if (!scheduler.awaitTermination(20, TimeUnit.SECONDS)) {
                System.err.println("Планировщик не завершился за 20 секунд");
            }
        } catch (SQLException e) {
            System.err.println("Не удалось подключиться к БД: " + e.getMessage());
            scheduler.shutdownNow();
        } catch (InterruptedException e) {
            System.err.println("Ожидание завершения планировщика прервано: " + e.getMessage());
            scheduler.shutdownNow();
        }
    }
}
