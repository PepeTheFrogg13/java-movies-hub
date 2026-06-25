package ru.practicum.moviehub.http;

import org.junit.jupiter.api.*;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MoviesApiTest {

    private static final String BASE = "http://localhost:8080"; // !!! добавьте базовую часть URL
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore moviesStore;

    @BeforeAll
    static void beforeAll() {
        moviesStore = new MoviesStore();
        server = new MoviesServer(moviesStore, 8080);
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        server.start();
    }

    @BeforeEach
    void beforeEach() {
        moviesStore.clearList();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    @Order(1)
    void getMoviesReturnsArray() throws Exception {
        moviesStore.addMovie("Матрица", 1999);
        moviesStore.addMovie("Титаник", 1997);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies")) // !!! Добавьте правильный URI
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertEquals("[{\"id\":1,\"title\":\"Матрица\",\"year\":1999},{\"id\":2,\"title\":\"Титаник\",\"year\":1997}]", body);
    }

    @Test
    void getMoviesWhenEmptyReturnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies")) // !!! Добавьте правильный URI
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    @Order(2)
    void getMoviesById() throws Exception {
        moviesStore.addMovie("Матрица", 1999);//id = 3
        moviesStore.addMovie("Титаник", 1997);//id = 4

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/3")) // !!! Добавьте правильный URI
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();

        assertEquals("{\"id\":3,\"title\":\"Матрица\",\"year\":1999}", body);
    }

    @Test
    @Order(3)
    void getMoviesByWrongId() throws Exception {
        moviesStore.addMovie("Матрица", 1999);//id = 5
        moviesStore.addMovie("Титаник", 1997);//id = 6

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1")) // !!! Добавьте правильный URI
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode(), "GET /movies должен вернуть 404");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertEquals("{\"error\":\"Фильм не найден\",\"details\":[\"Указан несуществующий идентификатор!\"]}", body);
    }

    @Test
    @Order(4)
    void getMoviesByWrongFormatId() throws Exception {
        moviesStore.addMovie("Матрица", 1999);//id = 7
        moviesStore.addMovie("Титаник", 1997);//id = 8

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/matrix")) // !!! Добавьте правильный URI
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "GET /movies должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertEquals("{\"error\":\"Некорректный ID\",\"details\":[\"Формат данных не соотвествует формату данных id\"]}", body);
    }

    @Test
    @Order(5)
    void deleteMovieSucsess() throws Exception {
        moviesStore.addMovie("Матрица", 1999);//id = 9
        moviesStore.addMovie("Титаник", 1997);//id = 10

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/10")) // !!! Добавьте правильный URI
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, resp.statusCode(), "GET /movies должен вернуть 204");
    }

    @Test
    @Order(6)
    void deleteMovieError() throws Exception {
        moviesStore.addMovie("Матрица", 1999);//id = 11
        moviesStore.addMovie("Титаник", 1997);//id = 12

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/10")) // !!! Добавьте правильный URI
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode(), "GET /movies должен вернуть 404");
    }

    @Test
    @Order(7)
    void deleteMovieErrorFormat() throws Exception {
        moviesStore.addMovie("Матрица", 1999);//id = 13
        moviesStore.addMovie("Титаник", 1997);//id = 14

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/titanic")) // !!! Добавьте правильный URI
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "GET /movies должен вернуть 400");

        String body = resp.body().trim();
        assertEquals("{\"error\":\"Некорректный ID\",\"details\":[\"Формат данных не соотвествует формату данных id\"]}", body);
    }

    @Test
    @Order(8)
    void getMoviesByYearSucsess() throws Exception {
        moviesStore.addMovie("Матрица", 1999);//id = 15
        moviesStore.addMovie("Титаник", 1997);//id = 16
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=1999")) // !!! Добавьте правильный URI
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertEquals("[{\"id\":15,\"title\":\"Матрица\",\"year\":1999}]", body);
    }

    @Test
    @Order(9)
    void getMoviesByYearEmpty() throws Exception {
        moviesStore.addMovie("Матрица", 1999);//id = 17
        moviesStore.addMovie("Титаник", 1997);//id = 18
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2013"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertEquals("[]", body);
    }

    @Test
    @Order(10)
    void getMoviesByYearWrong() throws Exception {
        moviesStore.addMovie("Матрица", 1999);//id = 19
        moviesStore.addMovie("Титаник", 1997);//id = 20
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=abcd")) // !!! Добавьте правильный URI
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "GET /movies должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertEquals("{\"error\":\"Некорректный параметр года\",\"details\":[\"Не указан параметр year\",\"Указан неверный формат запроса\"]}", body);
    }

    @Test
    @Order(11)
    void postMoviesSucsess() throws Exception {

        String json = "{\"title\":\"Грязь\",\"year\":2013}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies")) // !!! Добавьте правильный URI
                .header("Content-Type", "application/json")
                .header("Content-Type", "charset=UTF-8")// !!! Добавьте правильный URI
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertEquals("{\"id\":21,\"title\":\"Грязь\",\"year\":2013}", body);
    }

    @Test
    @Order(12)
    void postMoviesEmptyTitle() throws Exception {

        String json = "{\"title\":\"\",\"year\":2013}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies")) // !!! Добавьте правильный URI
                .header("Content-Type", "application/json")
                .header("Content-Type", "charset=UTF-8")// !!! Добавьте правильный URI
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /movies должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertEquals("{\"error\":\"Ошибка валидации\",\"details\":[\"Название не должно быть пустым\",\"Год должен быть между 1888 и 2026\"]}", body);
    }

    @Test
    @Order(13)
    void postMoviesLongTitle() throws Exception {

        String json = "{\"title\":\"1231321111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\",\"year\":2013}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies")) // !!! Добавьте правильный URI
                .header("Content-Type", "application/json")
                .header("Content-Type", "charset=UTF-8")// !!! Добавьте правильный URI
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /movies должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertEquals("{\"error\":\"Ошибка валидации\",\"details\":[\"Название не должно быть пустым\",\"Год должен быть между 1888 и 2026\"]}", body);
    }

    @Test
    @Order(14)
    void postMoviesWrongYear() throws Exception {

        String json = "{\"title\":\"Грязь\",\"year\":1850}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies")) // !!! Добавьте правильный URI
                .header("Content-Type", "application/json")
                .header("Content-Type", "charset=UTF-8")// !!! Добавьте правильный URI
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /movies должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertEquals("{\"error\":\"Ошибка валидации\",\"details\":[\"Название не должно быть пустым\",\"Год должен быть между 1888 и 2026\"]}", body);
    }

    @Test
    @Order(15)
    void postMoviesWrongHeaders() throws Exception {

        String json = "{\"title\":\"Грязь\",\"year\":2013}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies")) // !!! Добавьте правильный URI
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(415, resp.statusCode(), "POST /movies должен вернуть 415");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertEquals("{\"error\":\"Неправильное значение заголовка Content-Type\",\"details\":[\"Не указано значение application/json\",\"charset\\u003dUTF-8\"]}", body);
    }

    @Test
    @Order(16)
    void postMoviesWrongBody() throws Exception {

        String json = "{\"naimenovanie\":\"Грязь\",\"god\":2013}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies")) // !!! Добавьте правильный URI
                .header("Content-Type", "application/json")
                .header("Content-Type", "charset=UTF-8")// !!!
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /movies должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertEquals("{\"error\":\"Ошика десериализации\",\"details\":[\"В теле запроса не указан неверный набор полей\"]}", body);
    }


}