package ru.yandex.practicum.filmorate.dal.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({FilmRepositoryImpl.class, MpaRepositoryImpl.class, GenreRepositoryImpl.class,
        UserRepositoryImpl.class, FilmRowMapper.class, MpaRowMapper.class,
        GenreRowMapper.class, UserRowMapper.class})
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmRepositoryImplTest {

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private MpaRepository mpaRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private UserRepository userRepository;

    private Film testFilm;
    private User testUser;

    @BeforeEach
    void setUp() {
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        testFilm.setDuration(120L);

        Mpa mpa = mpaRepository.findById(1).orElse(null);
        testFilm.setMpa(mpa);

        Set<Genre> genres = new LinkedHashSet<>();
        genreRepository.findById(1).ifPresent(genres::add);
        testFilm.setGenres(genres);

        // Создаем тестового пользователя
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        testUser = userRepository.save(testUser);
    }

    @Test
    void shouldSaveNewFilm() {
        Film savedFilm = filmRepository.save(testFilm);

        assertThat(savedFilm).isNotNull();
        assertThat(savedFilm.getId()).isPositive();
        assertThat(savedFilm.getName()).isEqualTo("Test Film");
        assertThat(savedFilm.getDescription()).isEqualTo("Test Description");
        assertThat(savedFilm.getReleaseDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(savedFilm.getDuration()).isEqualTo(120L);
        assertThat(savedFilm.getMpa()).isNotNull();
        assertThat(savedFilm.getMpa().getId()).isEqualTo(1);
        assertThat(savedFilm.getGenres()).hasSize(1);
    }

    @Test
    void shouldUpdateExistingFilm() {
        Film savedFilm = filmRepository.save(testFilm);

        savedFilm.setName("Updated Film");
        savedFilm.setDescription("Updated Description");
        savedFilm.setDuration(150L);

        Film updatedFilm = filmRepository.save(savedFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedFilm.getDuration()).isEqualTo(150L);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentFilm() {
        Film nonExistentFilm = new Film();
        nonExistentFilm.setId(999L);
        nonExistentFilm.setName("Non Existent");
        nonExistentFilm.setDescription("Test");
        nonExistentFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        nonExistentFilm.setDuration(100L);

        assertThatThrownBy(() -> filmRepository.save(nonExistentFilm))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void shouldFindFilmById() {
        Film savedFilm = filmRepository.save(testFilm);
        Optional<Film> foundFilm = filmRepository.findById(savedFilm.getId());

        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getId()).isEqualTo(savedFilm.getId());
        assertThat(foundFilm.get().getName()).isEqualTo(savedFilm.getName());
        assertThat(foundFilm.get().getMpa()).isNotNull();
        assertThat(foundFilm.get().getGenres()).isNotEmpty();
    }

    @Test
    void shouldReturnEmptyWhenFilmNotFound() {
        Optional<Film> foundFilm = filmRepository.findById(999L);
        assertThat(foundFilm).isEmpty();
    }

    @Test
    void shouldFindAllFilms() {
        filmRepository.save(testFilm);

        Film secondFilm = new Film();
        secondFilm.setName("Second Film");
        secondFilm.setDescription("Second Description");
        secondFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        secondFilm.setDuration(90L);
        secondFilm.setMpa(mpaRepository.findById(2).orElse(null));
        filmRepository.save(secondFilm);

        var films = filmRepository.findAll();

        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName)
                .containsExactlyInAnyOrder("Test Film", "Second Film");
    }

    @Test
    void shouldDeleteFilm() {
        Film savedFilm = filmRepository.save(testFilm);
        filmRepository.deleteById(savedFilm.getId());

        Optional<Film> deletedFilm = filmRepository.findById(savedFilm.getId());
        assertThat(deletedFilm).isEmpty();
    }

    @Test
    void shouldCheckIfFilmExists() {
        Film savedFilm = filmRepository.save(testFilm);

        assertThat(filmRepository.existsById(savedFilm.getId())).isTrue();
        assertThat(filmRepository.existsById(999L)).isFalse();
    }

    @Test
    void shouldAddLike() {
        Film savedFilm = filmRepository.save(testFilm);

        // Используем реального пользователя, который есть в БД
        filmRepository.addLike(savedFilm.getId(), testUser.getId());

        Set<Long> likes = filmRepository.getLikes(savedFilm.getId());
        assertThat(likes).contains(testUser.getId());
    }

    @Test
    void shouldRemoveLike() {
        Film savedFilm = filmRepository.save(testFilm);

        filmRepository.addLike(savedFilm.getId(), testUser.getId());
        filmRepository.removeLike(savedFilm.getId(), testUser.getId());

        Set<Long> likes = filmRepository.getLikes(savedFilm.getId());
        assertThat(likes).doesNotContain(testUser.getId());
    }

    @Test
    void shouldGetLikes() {
        Film savedFilm = filmRepository.save(testFilm);

        // Создаем второго пользователя
        User secondUser = new User();
        secondUser.setEmail("second@example.com");
        secondUser.setLogin("secondlogin");
        secondUser.setName("Second User");
        secondUser.setBirthday(LocalDate.of(1991, 1, 1));
        secondUser = userRepository.save(secondUser);

        filmRepository.addLike(savedFilm.getId(), testUser.getId());
        filmRepository.addLike(savedFilm.getId(), secondUser.getId());

        Set<Long> likes = filmRepository.getLikes(savedFilm.getId());

        assertThat(likes).hasSize(2);
        assertThat(likes).containsExactlyInAnyOrder(testUser.getId(), secondUser.getId());
    }

    @Test
    void shouldFindPopularFilms() {
        Film film1 = filmRepository.save(testFilm);

        Film film2 = new Film();
        film2.setName("Popular Film");
        film2.setDescription("Popular Description");
        film2.setReleaseDate(LocalDate.of(2021, 1, 1));
        film2.setDuration(100L);
        film2.setMpa(mpaRepository.findById(1).orElse(null));
        Film savedFilm2 = filmRepository.save(film2);

        // Создаем дополнительных пользователей
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1992, 1, 1));
        user2 = userRepository.save(user2);

        User user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setLogin("user3");
        user3.setName("User 3");
        user3.setBirthday(LocalDate.of(1993, 1, 1));
        user3 = userRepository.save(user3);

        // Добавляем больше лайков второму фильму
        filmRepository.addLike(savedFilm2.getId(), testUser.getId());
        filmRepository.addLike(savedFilm2.getId(), user2.getId());
        filmRepository.addLike(savedFilm2.getId(), user3.getId());

        List<Film> popularFilms = filmRepository.findPopular(5);

        assertThat(popularFilms).isNotEmpty();
        assertThat(popularFilms.get(0).getId()).isEqualTo(savedFilm2.getId());
    }

    @Test
    void shouldFindFilmsByGenre() {
        Film savedFilm = filmRepository.save(testFilm);

        List<Film> filmsByGenre = filmRepository.findByGenre(1);

        assertThat(filmsByGenre).isNotEmpty();
        assertThat(filmsByGenre).extracting(Film::getId).contains(savedFilm.getId());
    }

    @Test
    void shouldFindFilmsByMpa() {
        Film savedFilm = filmRepository.save(testFilm);

        List<Film> filmsByMpa = filmRepository.findByMpa(1);

        assertThat(filmsByMpa).isNotEmpty();
        assertThat(filmsByMpa).extracting(Film::getId).contains(savedFilm.getId());
    }

    @Test
    void shouldSaveFilmWithoutMpa() {
        testFilm.setMpa(null);
        Film savedFilm = filmRepository.save(testFilm);

        Optional<Film> foundFilm = filmRepository.findById(savedFilm.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getMpa()).isNull();
    }

    @Test
    void shouldSaveFilmWithoutGenres() {
        testFilm.setGenres(null);
        Film savedFilm = filmRepository.save(testFilm);

        Optional<Film> foundFilm = filmRepository.findById(savedFilm.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getGenres()).isEmpty();
    }

    @Test
    void shouldUpdateGenres() {
        Film savedFilm = filmRepository.save(testFilm);

        Set<Genre> newGenres = new LinkedHashSet<>();
        genreRepository.findById(2).ifPresent(newGenres::add);
        genreRepository.findById(3).ifPresent(newGenres::add);
        savedFilm.setGenres(newGenres);

        Film updatedFilm = filmRepository.save(savedFilm);

        assertThat(updatedFilm.getGenres()).hasSize(2);
        assertThat(updatedFilm.getGenres()).extracting(Genre::getId)
                .containsExactlyInAnyOrder(2, 3);
    }
}