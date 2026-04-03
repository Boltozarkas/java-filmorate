package ru.yandex.practicum.filmorate.dal.inmemory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.repository.GenreRepository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Component
public class InMemoryGenreRepository implements GenreRepository {

    private final Map<Integer, Genre> genres = new HashMap<>();

    public InMemoryGenreRepository() {
        // Инициализация жанров
        genres.put(1, new Genre(1, "Комедия"));
        genres.put(2, new Genre(2, "Драма"));
        genres.put(3, new Genre(3, "Мультфильм"));
        genres.put(4, new Genre(4, "Триллер"));
        genres.put(5, new Genre(5, "Документальный"));
        genres.put(6, new Genre(6, "Боевик"));
    }

    @Override
    public List<Genre> findAll() {
        return new ArrayList<>(genres.values());
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        return Optional.ofNullable(genres.get(id));
    }

    @Override
    public boolean existsById(Integer id) {
        return genres.containsKey(id);
    }
}