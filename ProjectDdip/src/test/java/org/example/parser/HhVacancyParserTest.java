package org.example.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.model.Vacancy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты для HhVacancyParser.
 * Проверяет корректность парсинга вакансий, обработку ошибок и граничные случаи.
 */
class HhVacancyParserTest {

    private HhVacancyParser parser;
    private ObjectMapper objectMapper;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<Object> httpResponse;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        parser = new HhVacancyParser(httpClient, objectMapper);

        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode itemsNode = rootNode.putArray("items");
        ObjectNode vacancyNode = itemsNode.addObject();
        vacancyNode.put("name", "Java Developer");
        vacancyNode.putObject("employer").put("name", "Test Company");
        vacancyNode.putObject("area").put("name", "Moscow");
        ObjectNode salaryNode = vacancyNode.putObject("salary");
        salaryNode.put("from", 100000);
        salaryNode.put("to", 150000);
        salaryNode.put("currency", "RUR");
        vacancyNode.put("alternate_url", "https://hh.ru/vacancy/123");
        vacancyNode.put("published_at", "2024-03-20T10:00:00");

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(rootNode.toString());
        when(httpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);
    }

    @Test
    void fetchVacancies_ShouldReturnNonEmptyList() throws IOException, InterruptedException {
        List<Vacancy> vacancies = parser.fetchVacancies();
        assertNotNull(vacancies);
        assertFalse(vacancies.isEmpty());
    }

    @Test
    void fetchVacancies_ShouldReturnValidVacancyObjects() throws IOException, InterruptedException {
        List<Vacancy> vacancies = parser.fetchVacancies();
        for (Vacancy vacancy : vacancies) {
            assertNotNull(vacancy.getTitle());
            assertNotNull(vacancy.getCompany());
            assertNotNull(vacancy.getUrl());
            assertNotNull(vacancy.getSource());
            assertNotNull(vacancy.getSourceUrl());
        }
    }

    @Test
    void fetchVacancies_ShouldHandleNetworkErrors() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any())).thenThrow(new IOException("Network error"));
        assertThrows(IOException.class, () -> parser.fetchVacancies());
    }

    @Test
    void fetchVacancies_ShouldHandleEmptyResponse() throws IOException, InterruptedException {
        ObjectNode emptyRoot = objectMapper.createObjectNode();
        emptyRoot.putArray("items");
        when(httpResponse.body()).thenReturn(emptyRoot.toString());

        List<Vacancy> vacancies = parser.fetchVacancies();

        assertNotNull(vacancies);
        assertTrue(vacancies.isEmpty(), "Ожидаем пустой список при пустом ответе");
    }
}
