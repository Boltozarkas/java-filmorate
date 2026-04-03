package ru.yandex.practicum.filmorate.dal.repository;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreRepository {
    List<Genre> findAll();

    Optional<Genre> findById(Integer id);

    boolean existsById(Integer id);
}