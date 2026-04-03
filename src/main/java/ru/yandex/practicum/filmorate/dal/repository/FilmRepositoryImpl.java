package ru.yandex.practicum.filmorate.dal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;

@Repository
@Primary
public class FilmRepositoryImpl implements FilmRepository {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;

    @Autowired
    public FilmRepositoryImpl(JdbcTemplate jdbcTemplate,
                              MpaRepository mpaRepository,
                              GenreRepository genreRepository,
                              FilmRowMapper filmRowMapper,
                              GenreRowMapper genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public Film save(Film film) {
        // Валидация MPA
        if (film.getMpa() != null && !mpaRepository.existsById(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг MPA с id " + film.getMpa().getId() + " не найден");
        }

        // Валидация жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreRepository.existsById(genre.getId())) {
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }

        if (film.getId() == null) {
            return insert(film);
        } else {
            return update(film);
        }
    }

    private Film insert(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        saveGenres(film.getId(), film.getGenres());

        return findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм не найден после сохранения"));
    }

    private Film update(Film film) {
        if (!existsById(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );

        deleteGenres(film.getId());
        saveGenres(film.getId(), film.getGenres());

        return findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм не найден после обновления"));
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        film.setMpa(findMpaByFilmId(id));
        film.setGenres(findGenresByFilmId(id));
        film.setLikes(findLikesByFilmId(id));

        return Optional.of(film);
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        for (Film film : films) {
            film.setMpa(findMpaByFilmId(film.getId()));
            film.setGenres(findGenresByFilmId(film.getId()));
            film.setLikes(findLikesByFilmId(film.getId()));
        }

        return films;
    }

    @Override
    public void deleteById(Long id) {
        if (!existsById(id)) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public Set<Long> getLikes(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
    }

    @Override
    public List<Film> findPopular(int limit) {
        String sql = "SELECT f.*, COUNT(fl.user_id) as likes_count " +
                "FROM films f " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, limit);

        for (Film film : films) {
            film.setMpa(findMpaByFilmId(film.getId()));
            film.setGenres(findGenresByFilmId(film.getId()));
            film.setLikes(findLikesByFilmId(film.getId()));
        }

        return films;
    }

    @Override
    public List<Film> findByGenre(Integer genreId) {
        String sql = "SELECT f.* FROM films f " +
                "JOIN film_genre fg ON f.id = fg.film_id " +
                "WHERE fg.genre_id = ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, genreId);

        for (Film film : films) {
            film.setMpa(findMpaByFilmId(film.getId()));
            film.setGenres(findGenresByFilmId(film.getId()));
            film.setLikes(findLikesByFilmId(film.getId()));
        }

        return films;
    }

    @Override
    public List<Film> findByMpa(Integer mpaId) {
        String sql = "SELECT * FROM films WHERE mpa_id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, mpaId);

        for (Film film : films) {
            film.setMpa(findMpaByFilmId(film.getId()));
            film.setGenres(findGenresByFilmId(film.getId()));
            film.setLikes(findLikesByFilmId(film.getId()));
        }

        return films;
    }

    // Приватные вспомогательные методы
    private Mpa findMpaByFilmId(Long filmId) {
        String sql = "SELECT m.id, m.name FROM mpa m " +
                "JOIN films f ON m.id = f.mpa_id " +
                "WHERE f.id = ?";
        List<Mpa> mpaList = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Mpa(rs.getInt("id"), rs.getString("name")), filmId);
        return mpaList.isEmpty() ? null : mpaList.get(0);
    }

    private Set<Genre> findGenresByFilmId(Long filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genre fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? ORDER BY g.id";
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper, filmId);
        return new LinkedHashSet<>(genres);
    }

    private Set<Long> findLikesByFilmId(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
    }

    private void saveGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            jdbcTemplate.update(sql, filmId, genre.getId());
        }
    }

    private void deleteGenres(Long filmId) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", filmId);
    }
}