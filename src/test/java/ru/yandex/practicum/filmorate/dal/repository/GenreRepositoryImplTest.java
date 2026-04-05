package ru.yandex.practicum.filmorate.dal.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({GenreRepositoryImpl.class, GenreRowMapper.class})
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class GenreRepositoryImplTest {

    @Autowired
    private GenreRepository genreRepository;

    @Test
    void shouldFindAllGenres() {
        List<Genre> genres = genreRepository.findAll();

        assertThat(genres).isNotEmpty();
        assertThat(genres).hasSize(6);
        assertThat(genres.get(0).getName()).isEqualTo("Комедия");
        assertThat(genres.get(1).getName()).isEqualTo("Драма");
        assertThat(genres.get(2).getName()).isEqualTo("Мультфильм");
        assertThat(genres.get(3).getName()).isEqualTo("Триллер");
        assertThat(genres.get(4).getName()).isEqualTo("Документальный");
        assertThat(genres.get(5).getName()).isEqualTo("Боевик");
    }

    @Test
    void shouldFindGenreById() {
        Optional<Genre> genre = genreRepository.findById(1);

        assertThat(genre).isPresent();
        assertThat(genre.get().getId()).isEqualTo(1);
        assertThat(genre.get().getName()).isEqualTo("Комедия");
    }

    @Test
    void shouldReturnEmptyWhenGenreNotFound() {
        Optional<Genre> genre = genreRepository.findById(999);
        assertThat(genre).isEmpty();
    }

    @Test
    void shouldCheckIfGenreExists() {
        assertThat(genreRepository.existsById(1)).isTrue();
        assertThat(genreRepository.existsById(999)).isFalse();
    }

    @Test
    void shouldFindAllGenresSortedById() {
        List<Genre> genres = genreRepository.findAll();

        for (int i = 0; i < genres.size() - 1; i++) {
            assertThat(genres.get(i).getId()).isLessThan(genres.get(i + 1).getId());
        }
    }
}