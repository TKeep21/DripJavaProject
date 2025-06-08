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
 * Парсер вакансий с HH.ru через их публичный API.
 */
public class HhVacancyParser implements VacancyParser {

    private static final String BASE_URL = "https://api.hh.ru/vacancies";
    private static final int PER_PAGE = 100;
    private static final String SEARCH_TEXT = "java";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HhVacancyParser() {
        this(HttpClient.newHttpClient(), new ObjectMapper());
    }

    public HhVacancyParser(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Vacancy> fetchVacancies() throws IOException, InterruptedException {
        List<Vacancy> result = new ArrayList<>();
        int page = 0;

        while (true) {
            String url = String.format(
                    "%s?text=%s&per_page=%d&page=%d",
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
                System.err.println("[HhVacancyParser] non-200 response: " + response.statusCode() + " for page " + page);
            }

            String body = response.body();
            JsonNode root = objectMapper.readTree(body);
            JsonNode items = root.get("items");
            if (items == null || !items.isArray() || items.size() == 0) {
                break;
            }

            for (JsonNode item : items) {
                try {
                    Vacancy vacancy = mapNodeToVacancy(item);
                    result.add(vacancy);
                } catch (Exception ex) {
                    System.err.println("[HhVacancyParser] error mapping item: " + ex.getMessage());
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
     * Преобразует JSON-объект вакансии из API HH.ru в модель Vacancy.
     * Обрабатывает все поля, включая вложенные объекты и nullable значения.
     */
    private Vacancy mapNodeToVacancy(JsonNode item) {
        Vacancy v = new Vacancy();

        v.setTitle(getText(item, "name"));
        v.setCompany(getText(item, "employer", "name"));
        v.setCity(getText(item, "area", "name"));

        JsonNode salaryNode = item.get("salary");
        if (salaryNode != null && !salaryNode.isNull()) {
            if (salaryNode.hasNonNull("from")) {
                v.setSalaryFrom(salaryNode.get("from").asInt());
            }
            if (salaryNode.hasNonNull("to")) {
                v.setSalaryTo(salaryNode.get("to").asInt());
            }
            if (salaryNode.hasNonNull("currency")) {
                v.setCurrency(salaryNode.get("currency").asText());
            }
        }

        v.setDescription(getText(item, "description"));

        JsonNode snippet = item.get("snippet");
        if (snippet != null && !snippet.isNull()) {
            StringBuilder req = new StringBuilder();
            JsonNode requirement = snippet.get("requirement");
            if (requirement != null && !requirement.isNull()) {
                req.append(requirement.asText());
            }
            JsonNode responsibility = snippet.get("responsibility");
            if (responsibility != null && !responsibility.isNull()) {
                if (req.length() > 0) req.append(" | ");
                req.append(responsibility.asText());
            }
            v.setRequirements(req.toString());
        }

        String vacancyHtmlUrl = getText(item, "alternate_url");
        v.setUrl(vacancyHtmlUrl);
        v.setSource("hh.ru");
        v.setSourceUrl(vacancyHtmlUrl);

        String publishedAt = getText(item, "published_at");
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

        JsonNode scheduleNode = item.get("schedule");
        if (scheduleNode != null && !scheduleNode.isNull()) {
            v.setWorkSchedule(scheduleNode.get("name").asText());
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
