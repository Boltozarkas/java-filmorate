package ru.yandex.practicum.filmorate.dal.repository;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GenreRepository {
    List<Genre> findAll();

    Optional<Genre> findById(Integer id);

    boolean existsById(Integer id);

    // Методы для работы с жанрами фильмов
    Map<Long, Set<Genre>> findGenresByFilmIds(List<Long> filmIds);

    void saveFilmGenres(Long filmId, Set<Genre> genres);

    void deleteFilmGenres(Long filmId);
}

