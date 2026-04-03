package ru.yandex.practicum.filmorate.dal.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserRowMapperTest {

    private UserRowMapper mapper;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        mapper = new UserRowMapper();
        resultSet = mock(ResultSet.class);
    }

    @Test
    void shouldMapRowWithAllFields() throws SQLException {
        Long expectedId = 1L;
        String expectedEmail = "test@example.com";
        String expectedLogin = "testlogin";
        String expectedName = "Test User";
        LocalDate expectedBirthday = LocalDate.of(1990, 1, 1);

        when(resultSet.getLong("id")).thenReturn(expectedId);
        when(resultSet.getString("email")).thenReturn(expectedEmail);
        when(resultSet.getString("login")).thenReturn(expectedLogin);
        when(resultSet.getString("name")).thenReturn(expectedName);
        when(resultSet.getDate("birthday")).thenReturn(java.sql.Date.valueOf(expectedBirthday));

        User user = mapper.mapRow(resultSet, 1);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(expectedId);
        assertThat(user.getEmail()).isEqualTo(expectedEmail);
        assertThat(user.getLogin()).isEqualTo(expectedLogin);
        // Геттер возвращает имя, так как оно не null и не пустое
        assertThat(user.getName()).isEqualTo(expectedName);
        assertThat(user.getBirthday()).isEqualTo(expectedBirthday);
    }

    @Test
    void shouldMapRowWithNullName() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("email")).thenReturn("test@example.com");
        when(resultSet.getString("login")).thenReturn("testlogin");
        when(resultSet.getString("name")).thenReturn(null);
        when(resultSet.getDate("birthday")).thenReturn(java.sql.Date.valueOf(LocalDate.of(1990, 1, 1)));

        User user = mapper.mapRow(resultSet, 1);

        assertThat(user).isNotNull();
        // Геттер возвращает login, так как name == null
        assertThat(user.getName()).isEqualTo("testlogin");
    }

    @Test
    void shouldMapRowWithEmptyName() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("email")).thenReturn("test@example.com");
        when(resultSet.getString("login")).thenReturn("testlogin");
        when(resultSet.getString("name")).thenReturn("");
        when(resultSet.getDate("birthday")).thenReturn(java.sql.Date.valueOf(LocalDate.of(1990, 1, 1)));

        User user = mapper.mapRow(resultSet, 1);

        assertThat(user).isNotNull();
        // Геттер возвращает login, так как name пустая строка
        assertThat(user.getName()).isEqualTo("testlogin");
    }

    @Test
    void shouldMapRowWithBlankName() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("email")).thenReturn("test@example.com");
        when(resultSet.getString("login")).thenReturn("testlogin");
        when(resultSet.getString("name")).thenReturn("   ");
        when(resultSet.getDate("birthday")).thenReturn(java.sql.Date.valueOf(LocalDate.of(1990, 1, 1)));

        User user = mapper.mapRow(resultSet, 1);

        assertThat(user).isNotNull();
        // Геттер возвращает login, так как name состоит только из пробелов (isBlank() = true)
        assertThat(user.getName()).isEqualTo("testlogin");
    }

    @Test
    void shouldMapRowWithVeryLongEmail() throws SQLException {
        String longEmail = "verylongemailaddressthatmightexceednormallength@example.com";
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("email")).thenReturn(longEmail);
        when(resultSet.getString("login")).thenReturn("testlogin");
        when(resultSet.getString("name")).thenReturn("Test User");
        when(resultSet.getDate("birthday")).thenReturn(java.sql.Date.valueOf(LocalDate.of(1990, 1, 1)));

        User user = mapper.mapRow(resultSet, 1);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(longEmail);
    }

    @Test
    void shouldMapRowWithSpecialCharactersInLogin() throws SQLException {
        String specialLogin = "user_123-test";
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("email")).thenReturn("test@example.com");
        when(resultSet.getString("login")).thenReturn(specialLogin);
        when(resultSet.getString("name")).thenReturn("Test User");
        when(resultSet.getDate("birthday")).thenReturn(java.sql.Date.valueOf(LocalDate.of(1990, 1, 1)));

        User user = mapper.mapRow(resultSet, 1);

        assertThat(user).isNotNull();
        assertThat(user.getLogin()).isEqualTo(specialLogin);
    }

    @Test
    void shouldMapRowWithFutureBirthday() throws SQLException {
        LocalDate futureBirthday = LocalDate.now().plusYears(1);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("email")).thenReturn("test@example.com");
        when(resultSet.getString("login")).thenReturn("testlogin");
        when(resultSet.getString("name")).thenReturn("Test User");
        when(resultSet.getDate("birthday")).thenReturn(java.sql.Date.valueOf(futureBirthday));

        User user = mapper.mapRow(resultSet, 1);

        assertThat(user).isNotNull();
        assertThat(user.getBirthday()).isEqualTo(futureBirthday);
    }
}