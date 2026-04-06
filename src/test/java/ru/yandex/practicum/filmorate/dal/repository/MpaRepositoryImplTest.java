package ru.yandex.practicum.filmorate.dal.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({MpaRepositoryImpl.class, MpaRowMapper.class})
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MpaRepositoryImplTest {

    @Autowired
    private MpaRepository mpaRepository;

    @Test
    void shouldFindAllMpa() {
        List<Mpa> ratings = mpaRepository.findAll();

        assertThat(ratings).isNotEmpty();
        assertThat(ratings).hasSize(5);
        assertThat(ratings.get(0).getName()).isEqualTo("G");
        assertThat(ratings.get(1).getName()).isEqualTo("PG");
        assertThat(ratings.get(2).getName()).isEqualTo("PG-13");
        assertThat(ratings.get(3).getName()).isEqualTo("R");
        assertThat(ratings.get(4).getName()).isEqualTo("NC-17");
    }

    @Test
    void shouldFindMpaById() {
        Optional<Mpa> rating = mpaRepository.findById(1);

        assertThat(rating).isPresent();
        assertThat(rating.get().getId()).isEqualTo(1);
        assertThat(rating.get().getName()).isEqualTo("G");
    }

    @Test
    void shouldFindAllMpaRatings() {
        String[] expectedNames = {"G", "PG", "PG-13", "R", "NC-17"};

        for (int i = 1; i <= 5; i++) {
            Optional<Mpa> rating = mpaRepository.findById(i);
            assertThat(rating).isPresent();
            assertThat(rating.get().getName()).isEqualTo(expectedNames[i - 1]);
        }
    }

    @Test
    void shouldReturnEmptyWhenMpaNotFound() {
        Optional<Mpa> rating = mpaRepository.findById(999);
        assertThat(rating).isEmpty();
    }

    @Test
    void shouldCheckIfMpaExists() {
        assertThat(mpaRepository.existsById(1)).isTrue();
        assertThat(mpaRepository.existsById(999)).isFalse();
    }

    @Test
    void shouldFindAllMpaSortedById() {
        List<Mpa> ratings = mpaRepository.findAll();

        for (int i = 0; i < ratings.size() - 1; i++) {
            assertThat(ratings.get(i).getId()).isLessThan(ratings.get(i + 1).getId());
        }
    }
}