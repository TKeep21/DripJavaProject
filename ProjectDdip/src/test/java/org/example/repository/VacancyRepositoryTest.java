package org.example.repository;

import org.example.model.Vacancy;
import org.example.db.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Тесты для VacancyRepository.
 * Проверяет корректность работы с базой данных: сохранение, поиск и обновление вакансий.
 */
class VacancyRepositoryTest {

    @Mock
    private DatabaseManager dbManager;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private VacancyRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        repository = new VacancyRepository(dbManager);
    }

    @Test
    void save_ShouldInsertNewVacancy() throws SQLException {
        Vacancy vacancy = new Vacancy();
        vacancy.setUrl("http://example.com/test");
        vacancy.setTitle("Test Vacancy");
        vacancy.setCompany("Test Company");
        vacancy.setCity("Test City");
        vacancy.setSalaryFrom(100000);
        vacancy.setSalaryTo(150000);
        vacancy.setCurrency("RUR");
        vacancy.setIsActive(true);

        repository.save(vacancy);

        verify(preparedStatement).setString(1, vacancy.getTitle());
        verify(preparedStatement).setString(2, vacancy.getCompany());
        verify(preparedStatement).setString(3, vacancy.getCity());
        verify(preparedStatement).setInt(4, vacancy.getSalaryFrom());
        verify(preparedStatement).setInt(5, vacancy.getSalaryTo());
        verify(preparedStatement).setString(6, vacancy.getCurrency());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void findAllActiveUrls_ShouldReturnUrls() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("url")).thenReturn("http://example.com/1", "http://example.com/2");

        List<String> urls = repository.findAllActiveUrls();

        assertEquals(2, urls.size());
        assertEquals("http://example.com/1", urls.get(0));
        assertEquals("http://example.com/2", urls.get(1));
    }

    @Test
    void deactivateByUrls_ShouldUpdateStatus() throws SQLException {
        List<String> urls = List.of("http://example.com/1", "http://example.com/2");

        repository.deactivateByUrls(urls);

        verify(preparedStatement).setObject(eq(1), any(LocalDateTime.class));
        verify(preparedStatement).setArray(eq(2), any());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void logChange_ShouldInsertLog() throws SQLException {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        String url = "http://example.com/test";
        String changeType = "ADDED";
        String title = "Test Vacancy";
        String company = "Test Company";
        String city = "Test City";
        Integer salaryFrom = 100000;
        Integer salaryTo = 150000;
        String currency = "RUR";
        LocalDateTime publishedDate = null;
        String workSchedule = "Полный день";

        // Act
        repository.logChange(
            null,
            url,
            changeType,
            now,
            title,
            company,
            city,
            salaryFrom,
            salaryTo,
            currency,
            publishedDate,
            workSchedule
        );

        // Assert
        verify(preparedStatement).setNull(1, Types.BIGINT);
        verify(preparedStatement).setString(2, url);
        verify(preparedStatement).setString(3, changeType);
        verify(preparedStatement).setObject(4, now);
        verify(preparedStatement).setString(5, title);
        verify(preparedStatement).setString(6, company);
        verify(preparedStatement).setString(7, city);
        verify(preparedStatement).setInt(8, salaryFrom);
        verify(preparedStatement).setInt(9, salaryTo);
        verify(preparedStatement).setString(10, currency);
        verify(preparedStatement).setNull(11, Types.TIMESTAMP);
        verify(preparedStatement).setString(12, workSchedule);
        verify(preparedStatement).executeUpdate();
    }
} 