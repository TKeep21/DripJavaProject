package org.example.service;

import org.example.model.Vacancy;
import org.example.parser.VacancyParser;
import org.example.repository.VacancyRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для управления парсерами вакансий и их синхронизацией с БД.
 * Обрабатывает добавление новых, обновление существующих и удаление устаревших вакансий.
 */
public class VacancyParserService {
    private final List<VacancyParser> parsers;
    private final VacancyRepository vacancyRepository;

    public VacancyParserService(VacancyRepository vacancyRepository, List<VacancyParser> parsers) {
        this.vacancyRepository = vacancyRepository;
        this.parsers = parsers;
    }

    public void parseAndSaveAll() {
        LocalDateTime now = LocalDateTime.now();

        List<Vacancy> allFetched = new ArrayList<>();
        for (VacancyParser parser : parsers) {
            try {
                allFetched.addAll(parser.fetchVacancies());
            } catch (Exception e) {
                System.err.printf("[VacancyParserService] Ошибка парсера %s: %s%n",
                        parser.getClass().getSimpleName(), e.getMessage());
            }
        }

        Set<String> existingActiveUrls;
        try {
            existingActiveUrls = new HashSet<>(vacancyRepository.findAllActiveUrls());
        } catch (SQLException e) {
            System.err.println("Не удалось получить список active URLs: " + e.getMessage());
            return;
        }

        Set<String> fetchedUrls = allFetched.stream()
                .map(Vacancy::getUrl)
                .collect(Collectors.toSet());

        List<Vacancy> toAddOrReactivate = new ArrayList<>();
        for (Vacancy v : allFetched) {
            String url = v.getUrl();
            try {
                if (!vacancyRepository.existsByUrl(url)) {
                    toAddOrReactivate.add(v);
                }
                else if (!existingActiveUrls.contains(url)) {
                    Long oldId = vacancyRepository.findIdByUrl(url);
                    v.setId(oldId);
                    toAddOrReactivate.add(v);
                }
            } catch (SQLException ex) {
                System.err.println("Ошибка проверки existsByUrl для " + url + ": " + ex.getMessage());
            }
        }

        List<String> toRemoveUrls = existingActiveUrls.stream()
                .filter(url -> !fetchedUrls.contains(url))
                .collect(Collectors.toList());

        // Обработка новых и обновленных вакансий
        for (Vacancy v : toAddOrReactivate) {
            try {
                String url = v.getUrl();
                if (!vacancyRepository.existsByUrl(url)) {
                    v.setIsActive(true);
                    v.setClosedAt(null);
                    vacancyRepository.save(v);

                    vacancyRepository.logChange(
                            v.getId(), url, "ADDED", now,
                            v.getTitle(), v.getCompany(), v.getCity(),
                            v.getSalaryFrom(), v.getSalaryTo(), v.getCurrency(),
                            v.getPublishedDate(), v.getWorkSchedule()
                    );
                }
                else {
                    Long existingId = vacancyRepository.findIdByUrl(url);
                    v.setId(existingId);
                    vacancyRepository.reactivate(existingId, now);

                    vacancyRepository.logChange(
                            existingId, url, "UPDATED", now,
                            v.getTitle(), v.getCompany(), v.getCity(),
                            v.getSalaryFrom(), v.getSalaryTo(), v.getCurrency(),
                            v.getPublishedDate(), v.getWorkSchedule()
                    );
                }
            } catch (SQLException ex) {
                System.err.println("Ошибка при сохранении/обновлении вакансии "
                        + v.getUrl() + ": " + ex.getMessage());
            }
        }

        // Обработка удаленных вакансий
        for (String url : toRemoveUrls) {
            try {
                Long oldId = vacancyRepository.findIdByUrl(url);
                vacancyRepository.markAsRemoved(oldId, now);

                Vacancy oldVac = vacancyRepository.findById(oldId);
                vacancyRepository.logChange(
                        oldId,
                        url,
                        "REMOVED",
                        now,
                        oldVac.getTitle(),
                        oldVac.getCompany(),
                        oldVac.getCity(),
                        oldVac.getSalaryFrom(),
                        oldVac.getSalaryTo(),
                        oldVac.getCurrency(),
                        oldVac.getPublishedDate(),
                        oldVac.getWorkSchedule()
                );
            } catch (SQLException ex) {
                System.err.println("Ошибка при пометке вакансии как удалённой: " + ex.getMessage());
            }
        }
    }
}
