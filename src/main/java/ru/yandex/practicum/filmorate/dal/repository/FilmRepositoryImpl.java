package ru.yandex.practicum.filmorate.dal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
public class FilmRepositoryImpl implements FilmRepository {

    private static final String SQL_INSERT_FILM =
            "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_FILM =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String SQL_SELECT_FILM_BY_ID =
            "SELECT f.*, m.id as mpa_id, m.name as mpa_name FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id WHERE f.id = ?";
    private static final String SQL_SELECT_ALL_FILMS =
            "SELECT f.*, m.id as mpa_id, m.name as mpa_name FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id";
    private static final String SQL_DELETE_FILM =
            "DELETE FROM films WHERE id = ?";
    private static final String SQL_COUNT_FILMS =
            "SELECT COUNT(*) FROM films WHERE id = ?";
    private static final String SQL_INSERT_LIKE =
            "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String SQL_DELETE_LIKE =
            "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String SQL_SELECT_LIKES_BY_FILM_IDS =
            "SELECT film_id, user_id FROM film_likes WHERE film_id IN (%s)";
    private static final String SQL_SELECT_POPULAR_FILMS =
            "SELECT f.*, m.id as mpa_id, m.name as mpa_name, COUNT(fl.user_id) as likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "GROUP BY f.id, m.id, m.name " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";
    private static final String SQL_SELECT_FILMS_BY_GENRE =
            "SELECT f.*, m.id as mpa_id, m.name as mpa_name FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "JOIN film_genre fg ON f.id = fg.film_id " +
                    "WHERE fg.genre_id = ?";
    private static final String SQL_SELECT_FILMS_BY_MPA =
            "SELECT f.*, m.id as mpa_id, m.name as mpa_name FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "WHERE f.mpa_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final MpaRepository mpaRepository;
    private final FilmRowMapper filmRowMapper;

    @Autowired
    public FilmRepositoryImpl(JdbcTemplate jdbcTemplate,
                              MpaRepository mpaRepository,
                              FilmRowMapper filmRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaRepository = mpaRepository;
        this.filmRowMapper = filmRowMapper;
    }

    @Override
    public Film save(Film film) {
        if (film.getMpa() != null && !mpaRepository.existsById(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг MPA с id " + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            validateGenresExist(film.getGenres());
        }

        if (film.getId() == null) {
            return insert(film);
        } else {
            return update(film);
        }
    }

    private void validateGenresExist(Set<Genre> genres) {
        List<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toList());

        String placeholders = genreIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("SELECT COUNT(*) FROM genres WHERE id IN (%s)", placeholders);

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genreIds.toArray());
        if (count == null || count != genreIds.size()) {
            throw new NotFoundException("Один или несколько жанров не найдены");
        }
    }

    private Film insert(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_FILM, new String[]{"id"});
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

        return findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм не найден после сохранения"));
    }

    private Film update(Film film) {
        if (!existsById(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        jdbcTemplate.update(SQL_UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );

        return findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм не найден после обновления"));
    }

    @Override
    public Optional<Film> findById(Long id) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_FILM_BY_ID, (rs, rowNum) -> {
            Film film = filmRowMapper.mapRow(rs, rowNum);
            Mpa mpa = rs.getInt("mpa_id") != 0 ?
                    new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")) : null;
            film.setMpa(mpa);
            film.setLikes(new HashSet<>()); // Пустые лайки, заполнятся в сервисе
            return film;
        }, id);

        return films.isEmpty() ? Optional.empty() : Optional.of(films.get(0));
    }

    @Override
    public List<Film> findAll() {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_ALL_FILMS, (rs, rowNum) -> {
            Film film = filmRowMapper.mapRow(rs, rowNum);
            Mpa mpa = rs.getInt("mpa_id") != 0 ?
                    new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")) : null;
            film.setMpa(mpa);
            film.setLikes(new HashSet<>());
            return film;
        });

        return films;
    }

    @Override
    public void deleteById(Long id) {
        if (!existsById(id)) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        jdbcTemplate.update(SQL_DELETE_FILM, id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(SQL_COUNT_FILMS, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(SQL_INSERT_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update(SQL_DELETE_LIKE, filmId, userId);
    }

    @Override
    public Set<Long> getLikes(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
    }

    @Override
    public List<Film> findPopular(int limit) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_POPULAR_FILMS, (rs, rowNum) -> {
            Film film = filmRowMapper.mapRow(rs, rowNum);
            Mpa mpa = rs.getInt("mpa_id") != 0 ?
                    new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")) : null;
            film.setMpa(mpa);
            film.setLikes(new HashSet<>());
            return film;
        }, limit);

        return films;
    }

    @Override
    public List<Film> findByGenre(Integer genreId) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_FILMS_BY_GENRE, (rs, rowNum) -> {
            Film film = filmRowMapper.mapRow(rs, rowNum);
            Mpa mpa = rs.getInt("mpa_id") != 0 ?
                    new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")) : null;
            film.setMpa(mpa);
            film.setLikes(new HashSet<>());
            return film;
        }, genreId);

        return films;
    }

    @Override
    public List<Film> findByMpa(Integer mpaId) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_FILMS_BY_MPA, (rs, rowNum) -> {
            Film film = filmRowMapper.mapRow(rs, rowNum);
            Mpa mpa = rs.getInt("mpa_id") != 0 ?
                    new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")) : null;
            film.setMpa(mpa);
            film.setLikes(new HashSet<>());
            return film;
        }, mpaId);

        return films;
    }

    // Новый метод для массовой загрузки лайков (используется в сервисе)
    public Map<Long, Set<Long>> findLikesByFilmIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return new HashMap<>();
        }

        String ids = filmIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String sql = String.format(SQL_SELECT_LIKES_BY_FILM_IDS, ids);

        Map<Long, Set<Long>> likesMap = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            likesMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        });

        return likesMap;
    }
}