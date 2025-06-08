package org.example.export;

import org.example.model.Vacancy;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HtmlVacancyExporter implements VacancyExporter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void export(List<Vacancy> vacancies, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename + ".html"))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang=\"ru\">");
            writer.println("<head>");
            writer.println("<meta charset=\"UTF-8\">");
            writer.println("<title>Экспорт вакансий</title>");
            writer.println("<style>");
            writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
            writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("th { background-color: #f2f2f2; position: sticky; top: 0; }");
            writer.println("tr:nth-child(even) { background-color: #f9f9f9; }");
            writer.println("tr:hover { background-color: #f5f5f5; }");
            writer.println(".salary { white-space: nowrap; }");
            writer.println(".link { word-break: break-all; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("<h1>Список вакансий</h1>");
            writer.println("<p>Всего вакансий: " + vacancies.size() + "</p>");
            writer.println("<table>");
            writer.println("<thead>");
            writer.println("<tr>");
            writer.println("<th>ID</th>");
            writer.println("<th>Должность</th>");
            writer.println("<th>Компания</th>");
            writer.println("<th>Город</th>");
            writer.println("<th>Зарплата</th>");
            writer.println("<th>График работы</th>");
            writer.println("<th>Дата публикации</th>");
            writer.println("<th>Ссылка</th>");
            writer.println("<th>Источник</th>");
            writer.println("</tr>");
            writer.println("</thead>");
            writer.println("<tbody>");

            for (Vacancy v : vacancies) {
                String salary = formatSalary(v);
                String date = v.getPublishedDate() != null ? v.getPublishedDate().format(DATE_FORMATTER) : "—";

                writer.println("<tr>");
                writer.printf("<td>%s</td>%n", v.getId() != null ? v.getId() : "—");
                writer.printf("<td>%s</td>%n", escapeHtml(v.getTitle()));
                writer.printf("<td>%s</td>%n", escapeHtml(v.getCompany()));
                writer.printf("<td>%s</td>%n", escapeHtml(v.getCity()));
                writer.printf("<td class=\"salary\">%s</td>%n", escapeHtml(salary));
                writer.printf("<td>%s</td>%n", escapeHtml(v.getWorkSchedule()));
                writer.printf("<td>%s</td>%n", date);
                writer.printf("<td class=\"link\"><a href=\"%s\" target=\"_blank\">%s</a></td>%n", 
                    escapeHtml(v.getUrl()), escapeHtml(v.getUrl()));
                writer.printf("<td>%s</td>%n", escapeHtml(v.getSource()));
                writer.println("</tr>");
            }

            writer.println("</tbody>");
            writer.println("</table>");
            writer.println("</body>");
            writer.println("</html>");
        }
    }

    private String formatSalary(Vacancy v) {
        if (v.getSalaryFrom() == null && v.getSalaryTo() == null) {
            return "не указана";
        }
        StringBuilder sb = new StringBuilder();
        if (v.getSalaryFrom() != null) {
            sb.append(v.getSalaryFrom());
        }
        if (v.getSalaryFrom() != null && v.getSalaryTo() != null) {
            sb.append(" - ");
        }
        if (v.getSalaryTo() != null) {
            sb.append(v.getSalaryTo());
        }
        if (v.getCurrency() != null) {
            sb.append(" ").append(v.getCurrency());
        }
        return sb.toString();
    }

    private String escapeHtml(String str) {
        if (str == null) return "—";
        return str.replace("&", "&amp;")
                 .replace("<", "&lt;")
                 .replace(">", "&gt;")
                 .replace("\"", "&quot;")
                 .replace("'", "&#39;");
    }
} 