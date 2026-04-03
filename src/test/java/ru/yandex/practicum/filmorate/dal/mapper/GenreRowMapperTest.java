package ru.yandex.practicum.filmorate.dal.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenreRowMapperTest {

    private GenreRowMapper mapper;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        mapper = new GenreRowMapper();
        resultSet = mock(ResultSet.class);
    }

    @Test
    void shouldMapRowWithAllFields() throws SQLException {
        Integer expectedId = 1;
        String expectedName = "Комедия";

        when(resultSet.getInt("id")).thenReturn(expectedId);
        when(resultSet.getString("name")).thenReturn(expectedName);

        Genre genre = mapper.mapRow(resultSet, 1);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(expectedId);
        assertThat(genre.getName()).isEqualTo(expectedName);
    }

    @Test
    void shouldMapRowForAllGenres() throws SQLException {
        List<Integer> ids = Arrays.asList(1, 2, 3, 4, 5, 6);
        List<String> names = Arrays.asList("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");

        for (int i = 0; i < ids.size(); i++) {
            Integer id = ids.get(i);
            String name = names.get(i);

            when(resultSet.getInt("id")).thenReturn(id);
            when(resultSet.getString("name")).thenReturn(name);

            Genre genre = mapper.mapRow(resultSet, 1);

            assertThat(genre.getId()).isEqualTo(id);
            assertThat(genre.getName()).isEqualTo(name);
        }
    }

    @Test
    void shouldMapRowWithNullName() throws SQLException {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn(null);

        Genre genre = mapper.mapRow(resultSet, 1);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(1);
        assertThat(genre.getName()).isNull();
    }

    @Test
    void shouldMapRowWithEmptyName() throws SQLException {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("");

        Genre genre = mapper.mapRow(resultSet, 1);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(1);
        assertThat(genre.getName()).isEmpty();
    }

    @Test
    void shouldMapRowWithVeryLongGenreName() throws SQLException {
        String longName = "Очень-очень-очень-длинное-название-жанра-которое-никто-не-ожидал";
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn(longName);

        Genre genre = mapper.mapRow(resultSet, 1);

        assertThat(genre).isNotNull();
        assertThat(genre.getName()).isEqualTo(longName);
    }

    @Test
    void shouldMapRowWithMaxId() throws SQLException {
        Integer maxId = Integer.MAX_VALUE;
        when(resultSet.getInt("id")).thenReturn(maxId);
        when(resultSet.getString("name")).thenReturn("Test Genre");

        Genre genre = mapper.mapRow(resultSet, 1);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(maxId);
    }
}