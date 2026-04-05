package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repository.FilmRepository;
import ru.yandex.practicum.filmorate.dal.repository.GenreRepository;
import ru.yandex.practicum.filmorate.dal.repository.UserRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;

    @Autowired
    public FilmService(FilmRepository filmRepository,
                       UserRepository userRepository,
                       GenreRepository genreRepository) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.genreRepository = genreRepository;
    }

    public List<Film> getAllFilms() {
        List<Film> films = filmRepository.findAll();
        return enrichFilmsWithGenresAndLikes(films);
    }

    public Film getFilmById(Long id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
        return enrichFilmWithGenresAndLikes(film);
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = count == null ? 10 : count;
        List<Film> films = filmRepository.findPopular(limit);
        return enrichFilmsWithGenresAndLikes(films);
    }

    public Film addFilm(Film film) {
        Film savedFilm = filmRepository.save(film);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreRepository.saveFilmGenres(savedFilm.getId(), film.getGenres());
        }

        return enrichFilmWithGenresAndLikes(savedFilm);
    }

    public Film updateFilm(Film film) {
        Film updatedFilm = filmRepository.save(film);

        genreRepository.deleteFilmGenres(updatedFilm.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreRepository.saveFilmGenres(updatedFilm.getId(), film.getGenres());
        }

        return enrichFilmWithGenresAndLikes(updatedFilm);
    }

    public Film addLike(Long filmId, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!filmRepository.existsById(filmId)) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }

        filmRepository.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        return getFilmById(filmId);
    }

    public Film removeLike(Long filmId, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!filmRepository.existsById(filmId)) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }

        filmRepository.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
        return getFilmById(filmId);
    }

    // Новый метод для одного фильма
    private Film enrichFilmWithGenresAndLikes(Film film) {
        List<Long> filmIds = Collections.singletonList(film.getId());
        Map<Long, Set<Genre>> genresMap = genreRepository.findGenresByFilmIds(filmIds);
        Map<Long, Set<Long>> likesMap = filmRepository.findLikesByFilmIds(filmIds);

        film.setGenres(genresMap.getOrDefault(film.getId(), new LinkedHashSet<>()));
        film.setLikes(likesMap.getOrDefault(film.getId(), new HashSet<>()));
        return film;
    }

    // Новый метод для списка фильмов
    private List<Film> enrichFilmsWithGenresAndLikes(List<Film> films) {
        if (films.isEmpty()) {
            return films;
        }

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Long, Set<Genre>> genresMap = genreRepository.findGenresByFilmIds(filmIds);
        Map<Long, Set<Long>> likesMap = filmRepository.findLikesByFilmIds(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setLikes(likesMap.getOrDefault(film.getId(), new HashSet<>()));
        }

        return films;
    }
}