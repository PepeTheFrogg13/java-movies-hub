package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MoviesHandler extends BaseHttpHandler {

    private final MoviesStore moviesStore;
    private final Gson gson;

    public MoviesHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
        this.gson = new Gson();
    }

    public void sendErrorResponse(HttpExchange exchange,
                                  String error,
                                  List<String> details,
                                  int status) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(error, details);
        String json = gson.toJson(errorResponse);
        sendJson(exchange, status, json);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Headers headers = exchange.getRequestHeaders();
        String[] uri = exchange.getRequestURI().getPath().split("/");
        String[] params = new String[0];
        if (exchange.getRequestURI().getQuery() != null) {
            params = exchange.getRequestURI().getQuery().split("&");
        }
        //Обработка все GET эднпоинтов
        switch (method) {
            case "GET" -> {
                if (uri.length == 2 && params.length == 0) {
                    List<Movie> movieList = moviesStore.findAll();
                    String json = gson.toJson(movieList);
                    sendJson(exchange, 200, json);
                }
                if (uri.length == 2 && params.length == 1) {
                    String[] entry = params[0].split("=");
                    if (entry[0].equals("year")) {
                        int year;
                        try {
                            year = Integer.parseInt(entry[1]);
                            List<Movie> movieList = moviesStore.findByYear(year);
                            String json = gson.toJson(movieList);
                            sendJson(exchange, 200, json);
                        } catch (Exception e) {
                            sendErrorResponse(exchange, "Некорректный параметр года", List.of("Не указан параметр year", "Указан неверный формат запроса"), 400);
                            return;
                        }
                    } else {
                        sendErrorResponse(exchange, "Некорректный параметр года", List.of("Не указан параметр year", "Указан неверный формат запроса"), 400);
                        return;
                    }
                }
                if (uri.length == 3) {
                    try {
                        long id = Long.parseLong(uri[2]);
                        Optional<Movie> movieOptional = moviesStore.findById(id);
                        if (movieOptional.isEmpty()) {
                            sendErrorResponse(exchange, "Фильм не найден", List.of("Указан несуществующий идентификатор!"), 404);
                        } else {
                            String json = gson.toJson(movieOptional.get());
                            sendJson(exchange, 200, json);
                        }
                    } catch (Exception e) {
                        sendErrorResponse(exchange, "Некорректный ID", List.of("Формат данных не соотвествует формату данных id"), 400);
                    }
                }
            }
            //Обработка POST эндпоинта
            case "POST" -> {
                if (!headers.containsKey("Content-Type")) {
                    sendErrorResponse(exchange, "Отсутсвует нужный заголовок ", List.of("В заголовках запроса не указан Content-Type"), 415);
                    return;
                } else {
                    List<String> contentTypeValues = headers.get("Content-Type");
                    if (contentTypeValues.isEmpty()
                            || !contentTypeValues.contains("application/json")
                            || !contentTypeValues.contains("charset=UTF-8")) {
                        sendErrorResponse(exchange, "Неправильное значение заголовка Content-Type", List.of("Не указано значение application/json", "charset=UTF-8"), 415);
                        return;
                    }
                }
                try {
                    JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
                    int year = jsonObject.get("year").getAsInt();
                    String title = jsonObject.get("title").getAsString();
                    int validateYear = LocalDate.now().getYear() + 1;
                    if (title.isEmpty() || title.length() > 100 || year < 1888 || year > validateYear) {
                        sendErrorResponse(exchange, "Ошибка валидации", List.of("Название не должно быть пустым", "Год должен быть между 1888 и 2026"), 400);
                        return;
                    }
                    Movie movie = moviesStore.addMovie(title, year);
                    sendJson(exchange, 201, gson.toJson(movie));
                } catch (Exception e) {
                    sendErrorResponse(exchange, "Ошика десериализации", List.of("В теле запроса не указан неверный набор полей"), 400);
                }
            }
            case "DELETE" -> {
                if (uri.length == 3) {
                    try {
                        long id = Long.parseLong(uri[2]);
                        Optional<Movie> movieOptional = moviesStore.findById(id);
                        if (movieOptional.isEmpty()) {
                            sendErrorResponse(exchange, "Фильм не найден", List.of("Указан несуществующий идентификатор!"), 404);
                        } else {
                            moviesStore.deleteById(id);
                            exchange.sendResponseHeaders(204, -1);
                            exchange.close();
                        }
                    } catch (Exception e) {
                        sendErrorResponse(exchange, "Некорректный ID", List.of("Формат данных не соотвествует формату данных id"), 400);
                    }
                }
            }
            default -> {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }
}
