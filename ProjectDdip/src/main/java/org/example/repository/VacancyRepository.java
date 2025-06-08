package org.example.repository;

import org.example.db.DatabaseManager;
import org.example.model.Vacancy;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Репозиторий для работы с вакансиями в базе данных.
 * Обеспечивает CRUD операции, поиск, фильтрацию и сортировку вакансий.
 */
public class VacancyRepository {
    private final DatabaseManager dbManager;

    public VacancyRepository() {
        try {
            this.dbManager = DatabaseManager.getInstance();
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось инициализировать DatabaseManager", e);
        }
    }

    public VacancyRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Проверяет, существует ли вакансия с данным URL.
     */
    public boolean existsByUrl(String url) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vacancies WHERE url = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, url);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public List<Vacancy> findByCity(String city) throws SQLException {
        return findByField("city", city);
    }


    public List<Vacancy> findByCompany(String company) throws SQLException {
        return findByField("company", company);
    }

    /**
     * Поиск вакансий по ключевому слову в title или description.
     */
    public List<Vacancy> searchByKeyword(String keyword) throws SQLException {
        return search("(title LIKE ? OR description LIKE ?)", "%" + keyword + "%", "%" + keyword + "%");
    }

    /**
     * Сохраняет вакансию в базу данных.
     * Обрабатывает все поля, включая nullable значения.
     */
    public void save(Vacancy v) throws SQLException {
        String sql = """
            INSERT INTO vacancies (
              title,
              company,
              city,
              salary_from,
              salary_to,
              currency,
              description,
              url,
              published_date,
              source,
              source_url,
              requirements,
              work_schedule,
              created_at,
              is_active,closed_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getTitle());
            ps.setString(2, v.getCompany());
            ps.setString(3, v.getCity());

            if (v.getSalaryFrom() != null) {
                ps.setInt(4, v.getSalaryFrom());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (v.getSalaryTo() != null) {
                ps.setInt(5, v.getSalaryTo());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            if (v.getCurrency() != null) {
                ps.setString(6, v.getCurrency());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }

            if (v.getDescription() != null) {
                ps.setString(7, v.getDescription());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }

            ps.setString(8, v.getUrl());

            if (v.getPublishedDate() != null) {
                ps.setObject(9, v.getPublishedDate());
            } else {
                ps.setNull(9, Types.TIMESTAMP);
            }

            ps.setString(10, v.getSource());

            if (v.getSourceUrl() != null) {
                ps.setString(11, v.getSourceUrl());
            } else {
                ps.setNull(11, Types.VARCHAR);
            }

            if (v.getRequirements() != null) {
                ps.setString(12, v.getRequirements());
            } else {
                ps.setNull(12, Types.VARCHAR);
            }

            if (v.getWorkSchedule() != null) {
                ps.setString(13, v.getWorkSchedule());
            } else {
                ps.setNull(13, Types.VARCHAR);
            }

            if (v.getCreatedAt() != null) {
                ps.setObject(14, v.getCreatedAt());
            } else {
                ps.setNull(14, Types.TIMESTAMP);
            }
            ps.setBoolean(15, v.getIsActive());
            if(v.getClosedAt() != null) {
                ps.setObject(16, v.getClosedAt());
            }
            else{
                ps.setNull(16, Types.TIMESTAMP);
            }

            ps.executeUpdate();
        }
    }

    
    public List<Vacancy> findAll() throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE is_active = true";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapResultSet(rs);
        }
    }

    private List<Vacancy> findByField(String fieldName, String value) throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE " + fieldName + " = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }

    /**
     * Находит вакансии, у которых salary_from ≥ заданного minSalary.
     */
    public List<Vacancy> findBySalaryFromGreaterEqual(int minSalary) throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE salary_from >= ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, minSalary);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }

    /**
     * Находит вакансии, у которых salary_to ≤ заданного maxSalary.
     */
    public List<Vacancy> findBySalaryToLessEqual(int maxSalary) throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE salary_to <= ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maxSalary);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }

    /**
     * Находит вакансии, у которых salary_from ≥ minSalary и salary_to ≤ maxSalary.
     */
    public List<Vacancy> findBySalaryBetween(int minSalary, int maxSalary) throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE salary_from >= ? AND salary_to <= ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, minSalary);
            ps.setInt(2, maxSalary);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }

    private List<Vacancy> search(String condition, String... params) throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE " + condition;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }

    /**
     * Преобразует ResultSet в List<Vacancy>. Читает все колонки, включая новые:
     * salary_from, salary_to, currency, work_schedule, specialization.
     */
    private List<Vacancy> mapResultSet(ResultSet rs) throws SQLException {
        List<Vacancy> list = new ArrayList<>();
        while (rs.next()) {
            Vacancy v = new Vacancy();
            v.setId(rs.getLong("id"));
            v.setTitle(rs.getString("title"));
            v.setCompany(rs.getString("company"));
            v.setCity(rs.getString("city"));

            // salaryFrom
            int sf = rs.getInt("salary_from");
            if (!rs.wasNull()) {
                v.setSalaryFrom(sf);
            }

            // salaryTo
            int st = rs.getInt("salary_to");
            if (!rs.wasNull()) {
                v.setSalaryTo(st);
            }

            // currency
            String curr = rs.getString("currency");
            if (curr != null) {
                v.setCurrency(curr);
            }

            // description
            v.setDescription(rs.getString("description"));

            // url
            v.setUrl(rs.getString("url"));

            // publishedDate
            Timestamp pubTs = rs.getTimestamp("published_date");
            if (pubTs != null) {
                v.setPublishedDate(pubTs.toLocalDateTime());
            }

            // source
            v.setSource(rs.getString("source"));

            // sourceUrl
            v.setSourceUrl(rs.getString("source_url"));

            // requirements
            v.setRequirements(rs.getString("requirements"));

            // workSchedule
            v.setWorkSchedule(rs.getString("work_schedule"));


            // createdAt
            Timestamp crtTs = rs.getTimestamp("created_at");
            if (crtTs != null) {
                v.setCreatedAt(crtTs.toLocalDateTime());
            }
            v.setIsActive(rs.getBoolean("is_active"));


            list.add(v);
        }
        return list;
    }
    public List<String> findAllActiveUrls() throws SQLException {
        String sql = "SELECT url FROM vacancies WHERE is_active = true";
        List<String> urls = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                urls.add(rs.getString("url"));
            }
        }
        return urls;
    }
    public Long findIdByUrl(String url) throws SQLException {
        String sql = "SELECT id FROM vacancies WHERE url = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, url);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                } else {
                    return null;
                }
            }
        }
    }
    public Vacancy findById(Long id) throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Воспользоваться mapResultSet, но он ожидает ResultSet с несколькими.
                    Vacancy v = new Vacancy();
                    v.setId(rs.getLong("id"));
                    v.setTitle(rs.getString("title"));
                    v.setCompany(rs.getString("company"));
                    v.setCity(rs.getString("city"));
                    v.setSalaryFrom(rs.getInt("salary_from"));
                    if (rs.wasNull()) v.setSalaryFrom(null);
                    v.setSalaryTo(rs.getInt("salary_to"));
                    if (rs.wasNull()) v.setSalaryTo(null);
                    v.setCurrency(rs.getString("currency"));
                    v.setDescription(rs.getString("description"));
                    v.setUrl(rs.getString("url"));
                    Timestamp pubTs = rs.getTimestamp("published_date");
                    v.setPublishedDate(pubTs != null ? pubTs.toLocalDateTime() : null);
                    v.setSource(rs.getString("source"));
                    v.setSourceUrl(rs.getString("source_url"));
                    v.setRequirements(rs.getString("requirements"));
                    v.setWorkSchedule(rs.getString("work_schedule"));
                    Timestamp crtTs = rs.getTimestamp("created_at");
                    v.setCreatedAt(crtTs != null ? crtTs.toLocalDateTime() : null);
                    v.setIsActive(rs.getBoolean("is_active"));
                    Timestamp closedTs = rs.getTimestamp("closed_at");
                    v.setClosedAt(closedTs != null ? closedTs.toLocalDateTime() : null);
                    return v;
                }
                return null;
            }
        }
    }
    public void markAsRemoved(Long id, LocalDateTime when) throws SQLException {
        String sql = "UPDATE vacancies SET is_active = false, closed_at = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, when);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }
    /**
     * Логирует изменение вакансии в таблицу vacancy_changes.
     */
    public void logChange(
            Long vacancyId,
            String url,
            String changeType,
            LocalDateTime eventTime,
            String title,
            String company,
            String city,
            Integer salaryFrom,
            Integer salaryTo,
            String currency,
            LocalDateTime publishedDate,
            String workSchedule
    ) throws SQLException {
        String sql = """
        INSERT INTO vacancy_changes (
          vacancy_id,
          url,
          change_type,
          event_time,
          title,
          company,
          city,
          salary_from,
          salary_to,
          currency,
          published_date,
          work_schedule
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (vacancyId != null) {
                ps.setLong(1, vacancyId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, url);
            ps.setString(3, changeType);
            ps.setObject(4, eventTime);
            ps.setString(5, title);
            ps.setString(6, company);
            ps.setString(7, city);

            if (salaryFrom != null) {
                ps.setInt(8, salaryFrom);
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            if (salaryTo != null) {
                ps.setInt(9, salaryTo);
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            if (currency != null) {
                ps.setString(10, currency);
            } else {
                ps.setNull(10, Types.VARCHAR);
            }
            if (publishedDate != null) {
                ps.setObject(11, publishedDate);
            } else {
                ps.setNull(11, Types.TIMESTAMP);
            }
            // work_schedule
            if (workSchedule != null) {
                ps.setString(12, workSchedule);
            } else {
                ps.setNull(12, Types.VARCHAR);
            }

            ps.executeUpdate();
        }
    }
    public void reactivate(Long id, LocalDateTime reopenedAt) throws SQLException {
        String sql = "UPDATE vacancies SET is_active = true, closed_at = NULL WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
    public Map<String, Integer> countByCity() throws SQLException {
        String sql = "SELECT city, COUNT(*) AS cnt FROM vacancies WHERE is_active = true GROUP BY city";
        Map<String, Integer> map = new HashMap<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("city"), rs.getInt("cnt"));
            }
        }
        return map;
    }




    public Map<String, Double> avgSalaryFromByCity() throws SQLException {
        String sql = "SELECT city, AVG(salary_from) AS avg_min_sal FROM vacancies " +
                "WHERE is_active = true AND salary_from IS NOT NULL GROUP BY city";
        Map<String, Double> map = new HashMap<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("city"), rs.getDouble("avg_min_sal"));
            }
        }
        return map;
    }

    public Map<String, Double> avgSalaryToByCity() throws SQLException {
        String sql = "SELECT city, AVG(salary_to) AS avg_max_sal FROM vacancies " +
                "WHERE is_active = true AND salary_to IS NOT NULL GROUP BY city";
        Map<String, Double> map = new HashMap<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("city"), rs.getDouble("avg_max_sal"));
            }
        }
        return map;
    }


    public List<Vacancy> findAllOrderByPublishedDateDesc() throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE is_active = true " +
                "ORDER BY published_date DESC NULLS LAST, created_at DESC NULLS LAST";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapResultSet(rs);
        }
    }

    
    public List<Vacancy> findAllOrderBySalaryDesc() throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE is_active = true " +
                "ORDER BY COALESCE(salary_to, salary_from) DESC NULLS LAST, " +
                "salary_from DESC NULLS LAST";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapResultSet(rs);
        }
    }


    public List<Vacancy> findAllOrderByCompany() throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE is_active = true " +
                "ORDER BY company ASC NULLS LAST";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapResultSet(rs);
        }
    }


    public List<Vacancy> findAllOrderByPublishedDateAsc() throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE is_active = true " +
                "ORDER BY published_date ASC NULLS LAST, created_at ASC NULLS LAST";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapResultSet(rs);
        }
    }


    public List<Vacancy> findAllOrderBySalaryAsc() throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE is_active = true " +
                "ORDER BY COALESCE(salary_from, salary_to) ASC NULLS LAST, " +
                "salary_to ASC NULLS LAST";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapResultSet(rs);
        }
    }

    /**
     * Возвращает список вакансий, отсортированный по городу в алфавитном порядке.
     */
    public List<Vacancy> findAllOrderByCity() throws SQLException {
        String sql = "SELECT * FROM vacancies WHERE is_active = true " +
                "ORDER BY city ASC NULLS LAST";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapResultSet(rs);
        }
    }



    /**
     * Деактивирует вакансии по списку URL.
     */
    public void deactivateByUrls(List<String> urls) throws SQLException {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        String sql = "UPDATE vacancies SET is_active = false, closed_at = ? WHERE url = ANY(?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, LocalDateTime.now());
            Array urlArray = conn.createArrayOf("VARCHAR", urls.toArray());
            ps.setArray(2, urlArray);
            ps.executeUpdate();
        }
    }

}
