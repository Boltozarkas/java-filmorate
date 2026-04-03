package ru.yandex.practicum.filmorate.dal.inmemory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.repository.FilmRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmRepository implements FilmRepository {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 1;

    @Override
    public Film save(Film film) {
        if (film.getId() == null) {
            film.setId(currentId++);
        } else if (!existsById(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void deleteById(Long id) {
        if (!existsById(id)) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        films.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return films.containsKey(id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        film.getLikes().add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        film.getLikes().remove(userId);
    }

    @Override
    public Set<Long> getLikes(Long filmId) {
        Film film = films.get(filmId);
        if (film == null) {
            return new HashSet<>();
        }
        return new HashSet<>(film.getLikes());
    }

    @Override
    public List<Film> findPopular(int limit) {
        return films.values().stream()
                .sorted(Comparator.comparingInt(f -> -f.getLikes().size()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> findByGenre(Integer genreId) {
        return films.values().stream()
                .filter(film -> film.getGenres() != null &&
                        film.getGenres().stream().anyMatch(g -> g.getId().equals(genreId)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> findByMpa(Integer mpaId) {
        return films.values().stream()
                .filter(film -> film.getMpa() != null && film.getMpa().getId().equals(mpaId))
                .collect(Collectors.toList());
    }
}