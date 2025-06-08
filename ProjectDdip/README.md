# Vacancy Aggregator

Проект представляет собой агрегатор вакансий, который собирает и обрабатывает данные о вакансиях из различных источников.

## Технологии

- Java 21
- Maven
- PostgreSQL
- JUnit 5
- Mockito
- Jackson (для работы с JSON)

## Требования

- JDK 21 или выше
- Maven 3.6 или выше
- PostgreSQL

## Структура проекта

```
vacancy-aggregator/
├── src/
│   ├── main/         # Исходный код
│   └── test/         # Тесты
├── pom.xml           # Конфигурация Maven
└── README.md         # Документация
```

## Установка и запуск

1. Клонируйте репозиторий:
```bash
git clone https://github.com/TKeep21/DripJavaProject
cd vacancy-aggregator
```

2. Настройка базы данных:
   - Установите PostgreSQL, если еще не установлен
   - Создайте новую базу данных:
     ```sql
     CREATE DATABASE vacancy_aggregator;
     ```
   - Создайте пользователя и назначьте права:
     ```sql
     CREATE USER vacancy_user WITH PASSWORD 'your_password';
     GRANT ALL PRIVILEGES ON DATABASE vacancy_aggregator TO vacancy_user;
     ```
   - Настройте параметры подключения в файле конфигурации (путь к файлу: `src/main/resources/config.properties`):
     ```properties
     db.url=jdbc:postgresql://localhost:5432/vacancy_aggregator
     db.user=vacancy_user
     db.password=your_password
     ```

3. Соберите проект с помощью Maven:
```bash
mvn clean package
```

4. Запустите приложение:
```bash
java -jar target/vacancy-aggregator-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Запуск тестов

Для запуска тестов выполните:
```bash
mvn test
```

## Конфигурация

Перед запуском убедитесь, что:
1. PostgreSQL установлен и запущен
2. Создана база данных для проекта
3. Настроены параметры подключения к базе данных в файле конфигурации
4. Пользователь базы данных имеет все необходимые права

## Структура базы данных

После первого запуска приложения будут автоматически созданы все необходимые таблицы в базе данных. Структура таблиц включает:
- Таблицу вакансий
- Таблицу источников данных
- Таблицу для хранения истории обновлений

## Лицензия

MIT License

Copyright (c) 2025 Магомедов Махач

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. 