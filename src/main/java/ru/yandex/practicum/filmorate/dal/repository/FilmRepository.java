package ru.yandex.practicum.filmorate.dal.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmRepository {
    // CRUD операции
    Film save(Film film);

    Optional<Film> findById(Long id);

    List<Film> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    // Операции с лайками
    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    Set<Long> getLikes(Long filmId);

    // Поисковые запросы
    List<Film> findPopular(int limit);

    List<Film> findByGenre(Integer genreId);

    List<Film> findByMpa(Integer mpaId);
}