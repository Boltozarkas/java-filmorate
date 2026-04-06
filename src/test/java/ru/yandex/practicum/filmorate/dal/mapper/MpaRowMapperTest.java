package ru.yandex.practicum.filmorate.dal.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MpaRowMapperTest {

    private MpaRowMapper mapper;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        mapper = new MpaRowMapper();
        resultSet = mock(ResultSet.class);
    }

    @Test
    void shouldMapRowWithAllFields() throws SQLException {
        Integer expectedId = 1;
        String expectedName = "G";

        when(resultSet.getInt("id")).thenReturn(expectedId);
        when(resultSet.getString("name")).thenReturn(expectedName);

        Mpa mpa = mapper.mapRow(resultSet, 1);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(expectedId);
        assertThat(mpa.getName()).isEqualTo(expectedName);
    }

    @Test
    void shouldMapRowForAllMpaRatings() throws SQLException {
        List<Integer> ids = Arrays.asList(1, 2, 3, 4, 5);
        List<String> names = Arrays.asList("G", "PG", "PG-13", "R", "NC-17");

        for (int i = 0; i < ids.size(); i++) {
            Integer id = ids.get(i);
            String name = names.get(i);

            when(resultSet.getInt("id")).thenReturn(id);
            when(resultSet.getString("name")).thenReturn(name);

            Mpa mpa = mapper.mapRow(resultSet, 1);

            assertThat(mpa.getId()).isEqualTo(id);
            assertThat(mpa.getName()).isEqualTo(name);
        }
    }

    @Test
    void shouldMapRowWithNullName() throws SQLException {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn(null);

        Mpa mpa = mapper.mapRow(resultSet, 1);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(1);
        assertThat(mpa.getName()).isNull();
    }

    @Test
    void shouldMapRowWithEmptyName() throws SQLException {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("");

        Mpa mpa = mapper.mapRow(resultSet, 1);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(1);
        assertThat(mpa.getName()).isEmpty();
    }

    @Test
    void shouldMapRowWithRussianRating() throws SQLException {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("0+");

        Mpa mpa = mapper.mapRow(resultSet, 1);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(1);
        assertThat(mpa.getName()).isEqualTo("0+");
    }

    @Test
    void shouldMapRowWithAllRussianRatings() throws SQLException {
        List<Integer> ids = Arrays.asList(1, 2, 3, 4, 5);
        List<String> names = Arrays.asList("0+", "6+", "12+", "16+", "18+");

        for (int i = 0; i < ids.size(); i++) {
            Integer id = ids.get(i);
            String name = names.get(i);

            when(resultSet.getInt("id")).thenReturn(id);
            when(resultSet.getString("name")).thenReturn(name);

            Mpa mpa = mapper.mapRow(resultSet, 1);

            assertThat(mpa.getId()).isEqualTo(id);
            assertThat(mpa.getName()).isEqualTo(name);
        }
    }

    @Test
    void shouldMapRowWithMaxId() throws SQLException {
        Integer maxId = Integer.MAX_VALUE;
        when(resultSet.getInt("id")).thenReturn(maxId);
        when(resultSet.getString("name")).thenReturn("Test Rating");

        Mpa mpa = mapper.mapRow(resultSet, 1);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(maxId);
    }

    @Test
    void shouldMapRowWithSpecialCharactersInName() throws SQLException {
        String specialName = "PG-13 (Parental Guidance)";
        when(resultSet.getInt("id")).thenReturn(3);
        when(resultSet.getString("name")).thenReturn(specialName);

        Mpa mpa = mapper.mapRow(resultSet, 1);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getName()).isEqualTo(specialName);
    }
}