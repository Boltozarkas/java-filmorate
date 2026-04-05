package ru.yandex.practicum.filmorate.dal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class MpaRepositoryImpl implements MpaRepository {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    @Autowired
    public MpaRepositoryImpl(JdbcTemplate jdbcTemplate, MpaRowMapper mpaRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaRowMapper = mpaRowMapper;
    }

    @Override
    public List<Mpa> findAll() {
        String sql = "SELECT id, name FROM mpa ORDER BY id";
        return jdbcTemplate.query(sql, mpaRowMapper);
    }

    @Override
    public Optional<Mpa> findById(Integer id) {
        String sql = "SELECT id, name FROM mpa WHERE id = ?";
        List<Mpa> mpaList = jdbcTemplate.query(sql, mpaRowMapper, id);
        return mpaList.stream().findFirst();
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}