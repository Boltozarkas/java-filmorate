package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 1;

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получен запрос на получение всех фильмов");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        film.setId(currentId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен с id {}", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);
        // Проверка существования фильма
        validateFilmExists(film);
        films.put(film.getId(), film);
        log.info("Фильм с id {} успешно обновлен", film.getId());
        return film;
    }

    //Проверяет, что фильм существует в хранилище
    private void validateFilmExists(Film film) {
        if (film.getId() <= 0) {
            log.error("ID фильма должен быть указан");
            throw new ValidationException("ID фильма должен быть указан");
        }

        if (!films.containsKey(film.getId())) {
            log.error("Фильм с id {} не найден", film.getId());
            throw new ValidationException("Фильм с id " + film.getId() + " не найден");
        }
    }
}