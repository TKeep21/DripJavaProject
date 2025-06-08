package org.example.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Vacancy;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Парсер вакансий с SuperJob.ru через их публичный API.

 */
public class SuperJobVacancyParser implements VacancyParser {

    private static final String BASE_URL = "https://api.superjob.ru/2.0/vacancies/";
    private static final int PER_PAGE = 100;
    private static final String SEARCH_TEXT = "java";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SuperJobVacancyParser() {
        this(HttpClient.newHttpClient(), new ObjectMapper());
    }

    public SuperJobVacancyParser(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Vacancy> fetchVacancies() throws IOException, InterruptedException {
        List<Vacancy> result = new ArrayList<>();
        int page = 0;

        while (true) {
            String url = String.format(
                    "%s?keyword=%s&count=%d&page=%d",
                    BASE_URL, SEARCH_TEXT, PER_PAGE, page
            );
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "java-http-client")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 400) {
                break;
            }
            else if (response.statusCode() != 401 && response.statusCode() != 200) {
                System.err.println("[SuperJobVacancyParser] non-200 response: " + response.statusCode() + " for page " + page);
            }

            String body = response.body();
            JsonNode root = objectMapper.readTree(body);
            JsonNode items = root.get("objects");
            if (items == null || !items.isArray() || items.size() == 0) {
                break;
            }

            for (JsonNode item : items) {
                try {
                    Vacancy vacancy = mapNodeToVacancy(item);
                    result.add(vacancy);
                } catch (Exception ex) {
                    System.err.println("[SuperJobVacancyParser] error mapping item: " + ex.getMessage());
                }
            }

            if (items.size() < PER_PAGE) {
                break;
            }

            page++;
        }

        return result;
    }

    /**
     * Преобразует JSON-объект вакансии из API SuperJob.ru в модель Vacancy.
     * Обрабатывает все поля, включая вложенные объекты и nullable значения.
     */
    private Vacancy mapNodeToVacancy(JsonNode item) {
        Vacancy v = new Vacancy();

        v.setTitle(getText(item, "profession"));
        v.setCompany(getText(item, "client", "title"));
        v.setCity(getText(item, "town", "title"));

        JsonNode paymentNode = item.get("payment");
        if (paymentNode != null && !paymentNode.isNull()) {
            if (paymentNode.hasNonNull("from")) {
                v.setSalaryFrom(paymentNode.get("from").asInt());
            }
            if (paymentNode.hasNonNull("to")) {
                v.setSalaryTo(paymentNode.get("to").asInt());
            }
            if (paymentNode.hasNonNull("currency")) {
                v.setCurrency(paymentNode.get("currency").asText());
            }
        }

        v.setDescription(getText(item, "vacancyRichText"));
        v.setRequirements(getText(item, "candidat"));

        String vacancyUrl = getText(item, "link");
        v.setUrl(vacancyUrl);
        v.setSource("superjob.ru");
        v.setSourceUrl(vacancyUrl);

        String publishedAt = getText(item, "date_published");
        if (publishedAt != null) {
            try {
                LocalDateTime pub = LocalDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME);
                v.setPublishedDate(pub);
            } catch (Exception ex) {
                v.setPublishedDate(LocalDateTime.now());
            }
        } else {
            v.setPublishedDate(LocalDateTime.now());
        }

        v.setCreatedAt(LocalDateTime.now());

        JsonNode scheduleNode = item.get("type_of_work");
        if (scheduleNode != null && !scheduleNode.isNull()) {
            v.setWorkSchedule(scheduleNode.get("title").asText());
        }

        return v;
    }

    /**
     * Безопасно извлекает вложенное поле из JsonNode.
     */
    private String getText(JsonNode node, String... path) {
        JsonNode curr = node;
        for (String p : path) {
            if (curr == null) return null;
            curr = curr.get(p);
        }
        return (curr != null && !curr.isNull()) ? curr.asText() : null;
    }
}
