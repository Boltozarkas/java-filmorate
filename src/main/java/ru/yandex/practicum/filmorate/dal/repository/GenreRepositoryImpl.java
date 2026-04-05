package ru.yandex.practicum.filmorate.dal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
public class GenreRepositoryImpl implements GenreRepository {

    private static final String SQL_SELECT_ALL_GENRES =
            "SELECT id, name FROM genres ORDER BY id";
    private static final String SQL_SELECT_GENRE_BY_ID =
            "SELECT id, name FROM genres WHERE id = ?";
    private static final String SQL_COUNT_GENRE_BY_ID =
            "SELECT COUNT(*) FROM genres WHERE id = ?";
    private static final String SQL_SELECT_GENRES_BY_FILM_IDS =
            "SELECT fg.film_id, g.id, g.name FROM film_genre fg " +
                    "JOIN genres g ON fg.genre_id = g.id " +
                    "WHERE fg.film_id IN (%s) ORDER BY fg.film_id, g.id";
    private static final String SQL_INSERT_FILM_GENRE =
            "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String SQL_DELETE_FILM_GENRES =
            "DELETE FROM film_genre WHERE film_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    @Autowired
    public GenreRepositoryImpl(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query(SQL_SELECT_ALL_GENRES, genreRowMapper);
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        List<Genre> genres = jdbcTemplate.query(SQL_SELECT_GENRE_BY_ID, genreRowMapper, id);
        return genres.stream().findFirst();
    }

    @Override
    public boolean existsById(Integer id) {
        Integer count = jdbcTemplate.queryForObject(SQL_COUNT_GENRE_BY_ID, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Map<Long, Set<Genre>> findGenresByFilmIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return new HashMap<>();
        }

        String ids = filmIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String sql = String.format(SQL_SELECT_GENRES_BY_FILM_IDS, ids);

        Map<Long, Set<Genre>> genresMap = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
            genresMap.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        });

        return genresMap;
    }

    @Override
    public void saveFilmGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        for (Genre genre : genres) {
            jdbcTemplate.update(SQL_INSERT_FILM_GENRE, filmId, genre.getId());
        }
    }

    @Override
    public void deleteFilmGenres(Long filmId) {
        jdbcTemplate.update(SQL_DELETE_FILM_GENRES, filmId);
    }
}