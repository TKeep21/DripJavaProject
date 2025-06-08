package org.example.service;

import org.example.model.Vacancy;
import org.example.parser.VacancyParser;
import org.example.repository.VacancyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для VacancyParserService.
 * Проверяет основные сценарии: добавление новых вакансий, деактивация старых и обработка ошибок.
 */
class VacancyParserServiceTest {

    @Mock
    private VacancyRepository repository;

    @Mock
    private VacancyParser parser;

    private VacancyParserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new VacancyParserService(repository, Arrays.asList(parser));
    }

    @Test
    void parseAndSaveAll_ShouldSaveNewVacancies() throws IOException, InterruptedException, SQLException {
        Vacancy vacancy = new Vacancy();
        vacancy.setUrl("http://example.com/new");
        vacancy.setTitle("New Vacancy");
        
        when(parser.fetchVacancies()).thenReturn(Arrays.asList(vacancy));
        when(repository.findIdByUrl(anyString())).thenReturn(null);

        service.parseAndSaveAll();

        verify(repository, times(1)).logChange(
            isNull(),
            anyString(),
            eq("ADDED"),
            any(LocalDateTime.class),
            anyString(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void parseAndSaveAll_ShouldDeactivateOldVacancies() throws IOException, InterruptedException, SQLException {
        List<String> oldUrls = Arrays.asList("http://example.com/old1", "http://example.com/old2");
        when(repository.findAllActiveUrls()).thenReturn(oldUrls);
        when(parser.fetchVacancies()).thenReturn(Arrays.asList());
        Vacancy oldVac = new Vacancy();
        oldVac.setTitle("Old Vacancy");
        when(repository.findById(any())).thenReturn(oldVac);

        service.parseAndSaveAll();

        verify(repository, times(oldUrls.size())).markAsRemoved(anyLong(), any(LocalDateTime.class));
        verify(repository, times(oldUrls.size()))
                .logChange(anyLong(), anyString(), eq("REMOVED"), any(LocalDateTime.class),
                        any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void parseAndSaveAll_ShouldHandleParserErrors() throws IOException, InterruptedException, SQLException {
        when(parser.fetchVacancies()).thenThrow(new IOException("Test error"));

        service.parseAndSaveAll();

        verify(repository, never()).save(any(Vacancy.class));
    }
} 