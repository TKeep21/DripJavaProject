package org.example.parser;

import org.example.model.Vacancy;

import java.io.IOException;
import java.util.List;

/**
 * Интерфейс для парсеров вакансий с различных ресурсов (HH.ru, SuperJob и др.).
 * Каждая реализация должна возвращать список вакансий с конкретного ресурса.
 */
public interface VacancyParser {
    /**
     * Получает вакансии с ресурса и преобразует их в объекты Vacancy.
     *
     * @return список вакансий с заполненными полями
     * @throws IOException при ошибках сети
     * @throws InterruptedException при прерывании запроса
     */
    List<Vacancy> fetchVacancies() throws IOException, InterruptedException;
}
