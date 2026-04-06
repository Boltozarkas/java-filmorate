package ru.yandex.practicum.filmorate.dal.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilmRowMapperTest {

    private FilmRowMapper mapper;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        mapper = new FilmRowMapper();
        resultSet = mock(ResultSet.class);
    }

    @Test
    void shouldMapRowWithAllFields() throws SQLException {
        Long expectedId = 1L;
        String expectedName = "Test Film";
        String expectedDescription = "Test Description";
        LocalDate expectedReleaseDate = LocalDate.of(2020, 1, 1);
        Long expectedDuration = 120L;

        when(resultSet.getLong("id")).thenReturn(expectedId);
        when(resultSet.getString("name")).thenReturn(expectedName);
        when(resultSet.getString("description")).thenReturn(expectedDescription);
        when(resultSet.getDate("release_date")).thenReturn(java.sql.Date.valueOf(expectedReleaseDate));
        when(resultSet.getLong("duration")).thenReturn(expectedDuration);

        Film film = mapper.mapRow(resultSet, 1);

        assertThat(film).isNotNull();
        assertThat(film.getId()).isEqualTo(expectedId);
        assertThat(film.getName()).isEqualTo(expectedName);
        assertThat(film.getDescription()).isEqualTo(expectedDescription);
        assertThat(film.getReleaseDate()).isEqualTo(expectedReleaseDate);
        assertThat(film.getDuration()).isEqualTo(expectedDuration);
    }

    @Test
    void shouldMapRowWithNullDescription() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn("Test Film");
        when(resultSet.getString("description")).thenReturn(null);
        when(resultSet.getDate("release_date")).thenReturn(java.sql.Date.valueOf(LocalDate.of(2020, 1, 1)));
        when(resultSet.getLong("duration")).thenReturn(120L);

        Film film = mapper.mapRow(resultSet, 1);

        assertThat(film).isNotNull();
        assertThat(film.getDescription()).isNull();
    }

    @Test
    void shouldMapRowWithZeroDuration() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn("Test Film");
        when(resultSet.getString("description")).thenReturn("Test Description");
        when(resultSet.getDate("release_date")).thenReturn(java.sql.Date.valueOf(LocalDate.of(2020, 1, 1)));
        when(resultSet.getLong("duration")).thenReturn(0L);

        Film film = mapper.mapRow(resultSet, 1);

        assertThat(film).isNotNull();
        assertThat(film.getDuration()).isZero();
    }

    @Test
    void shouldMapRowWithVeryLongName() throws SQLException {
        String longName = "A".repeat(255);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn(longName);
        when(resultSet.getString("description")).thenReturn("Test Description");
        when(resultSet.getDate("release_date")).thenReturn(java.sql.Date.valueOf(LocalDate.of(2020, 1, 1)));
        when(resultSet.getLong("duration")).thenReturn(120L);

        Film film = mapper.mapRow(resultSet, 1);

        assertThat(film).isNotNull();
        assertThat(film.getName()).isEqualTo(longName);
        assertThat(film.getName().length()).isEqualTo(255);
    }

    @Test
    void shouldMapRowWithVeryLongDescription() throws SQLException {
        String longDescription = "B".repeat(200);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn("Test Film");
        when(resultSet.getString("description")).thenReturn(longDescription);
        when(resultSet.getDate("release_date")).thenReturn(java.sql.Date.valueOf(LocalDate.of(2020, 1, 1)));
        when(resultSet.getLong("duration")).thenReturn(120L);

        Film film = mapper.mapRow(resultSet, 1);

        assertThat(film).isNotNull();
        assertThat(film.getDescription()).isEqualTo(longDescription);
        assertThat(film.getDescription().length()).isEqualTo(200);
    }
}