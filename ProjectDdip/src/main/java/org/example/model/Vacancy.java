package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Модель вакансии, содержащая все необходимые поля для хранения информации о вакансии.
 * Поддерживает как обязательные, так и опциональные поля (зарплата, график работы и т.д.).
 */
public class Vacancy {
    private Long id;
    private String title;
    private String company;
    private String city;
    private String description;
    private String url;
    private LocalDateTime publishedDate;
    private String requirements;
    private String source;
    private String sourceUrl;
    private LocalDateTime createdAt;
    private String workSchedule;     
    private Integer salaryFrom;         // минимальная зарплата, если есть
    private Integer salaryTo;           // максимальная зарплата
    private String currency;
    private Boolean isActive;
    private LocalDateTime closedAt;


    public Vacancy() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(String workSchedule) {
        this.workSchedule = workSchedule;
    }



    public Integer getSalaryFrom() {
        return salaryFrom;
    }

    public void setSalaryFrom(Integer salaryFrom) {
        this.salaryFrom = salaryFrom;
    }

    public Integer getSalaryTo() {
        return salaryTo;
    }

    public void setSalaryTo(Integer salaryTo) {
        this.salaryTo = salaryTo;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }
    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    @Override
    public String toString() {
        String pub = (publishedDate != null)
                ? publishedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "—";
        String crt = (createdAt != null)
                ? createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "—";


        String salaryStr = "—";
        if (salaryFrom != null || salaryTo != null) {
            if (salaryFrom != null && salaryTo != null) {
                salaryStr = String.format("от %d до %d %s", salaryFrom, salaryTo,
                        (currency != null ? currency : ""));
            } else if (salaryFrom != null) {
                salaryStr = String.format("от %d %s", salaryFrom,
                        (currency != null ? currency : ""));
            } else {
                salaryStr = String.format("до %d %s", salaryTo,
                        (currency != null ? currency : ""));
            }
        }

        return String.format(
                "[ID: %d] %s в %s (%s), зарплата: %s, график: %s, специализация: %s%n" +
                        "  опубликовано: %s, источник: %s, создано: %s%n" +
                        "  ссылка: %s%n" +
                        "  описание: %s%n" +
                        "  требования: %s%n",
                id != null ? id : 0,
                title != null ? title : "—",
                company != null ? company : "—",
                city != null ? city : "—",
                salaryStr,
                workSchedule != null ? workSchedule : "—",
                pub,
                source != null ? source : "—",
                crt,
                url != null ? url : "—",
                description != null ? description : "—",
                requirements != null ? requirements : "—"
        );
    }
}

