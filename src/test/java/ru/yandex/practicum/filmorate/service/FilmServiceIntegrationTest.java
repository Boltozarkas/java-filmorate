package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.repository.GenreRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmServiceIntegrationTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserService userService;

    @Autowired
    private GenreRepository genreRepository;

    private Film testFilm;
    private User testUser;

    @BeforeEach
    void setUp() {
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        testFilm.setDuration(120L);
        testFilm.setMpa(new Mpa(1, "G"));

        Set<Genre> genres = new LinkedHashSet<>();
        genreRepository.findById(1).ifPresent(genres::add);
        testFilm.setGenres(genres);

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldAddFilm() {
        Film film = filmService.addFilm(testFilm);

        assertThat(film).isNotNull();
        assertThat(film.getId()).isPositive();
        assertThat(film.getName()).isEqualTo("Test Film");
        assertThat(film.getGenres()).isNotEmpty();
    }

    @Test
    void shouldUpdateFilm() {
        Film savedFilm = filmService.addFilm(testFilm);
        savedFilm.setName("Updated Film");

        Film updatedFilm = filmService.updateFilm(savedFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
    }

    @Test
    void shouldGetFilmById() {
        Film savedFilm = filmService.addFilm(testFilm);
        Film foundFilm = filmService.getFilmById(savedFilm.getId());

        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getId()).isEqualTo(savedFilm.getId());
        assertThat(foundFilm.getGenres()).isNotEmpty();
    }

    @Test
    void shouldAddLike() {
        Film film = filmService.addFilm(testFilm);
        User user = userService.addUser(testUser);

        filmService.addLike(film.getId(), user.getId());

        Film updatedFilm = filmService.getFilmById(film.getId());
        assertThat(updatedFilm.getLikes()).contains(user.getId());
    }

    @Test
    void shouldRemoveLike() {
        Film film = filmService.addFilm(testFilm);
        User user = userService.addUser(testUser);

        filmService.addLike(film.getId(), user.getId());
        filmService.removeLike(film.getId(), user.getId());

        Film updatedFilm = filmService.getFilmById(film.getId());
        assertThat(updatedFilm.getLikes()).doesNotContain(user.getId());
    }

    @Test
    void shouldGetPopularFilms() {
        Film film1 = filmService.addFilm(testFilm);

        Film film2 = new Film();
        film2.setName("Popular Film");
        film2.setDescription("Popular Description");
        film2.setReleaseDate(LocalDate.of(2021, 1, 1));
        film2.setDuration(100L);
        film2.setMpa(new Mpa(1, "G"));
        Film popularFilm = filmService.addFilm(film2);

        User user1 = userService.addUser(testUser);
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User secondUser = userService.addUser(user2);

        filmService.addLike(popularFilm.getId(), user1.getId());
        filmService.addLike(popularFilm.getId(), secondUser.getId());

        List<Film> popularFilms = filmService.getPopularFilms(5);

        assertThat(popularFilms).isNotEmpty();
        assertThat(popularFilms.get(0).getId()).isEqualTo(popularFilm.getId());
    }
}