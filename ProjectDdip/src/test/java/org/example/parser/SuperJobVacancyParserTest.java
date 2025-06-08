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
 * Тесты для SuperJobVacancyParser.
 * Проверяет корректность парсинга вакансий с SuperJob, обработку ошибок и граничные случаи.
 */
class SuperJobVacancyParserTest {

    private SuperJobVacancyParser parser;
    private ObjectMapper objectMapper;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<Object> httpResponse;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        parser = new SuperJobVacancyParser(httpClient, objectMapper);
        
        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode itemsNode = rootNode.putArray("objects");
        
        ObjectNode vacancyNode = itemsNode.addObject();
        vacancyNode.put("profession", "Java Developer");
        vacancyNode.putObject("client").put("title", "Test Company");
        vacancyNode.putObject("town").put("title", "Moscow");
        ObjectNode salaryNode = vacancyNode.putObject("payment");
        salaryNode.put("from", 100000);
        salaryNode.put("to", 150000);
        salaryNode.put("currency", "rub");
        vacancyNode.put("link", "https://superjob.ru/vacancy/123");
        vacancyNode.put("date_published", "2024-03-20T10:00:00");
        
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
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.putArray("objects");
        when(httpResponse.body()).thenReturn(rootNode.toString());
        List<Vacancy> vacancies = parser.fetchVacancies();
        assertTrue(vacancies.isEmpty(), "Ожидаем пустой список при пустом ответе");
    }
} 