package ru.yandex.practicum.filmorate.dal.inmemory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.repository.MpaRepository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

@Component
public class InMemoryMpaRepository implements MpaRepository {

    private final Map<Integer, Mpa> mpaRatings = new HashMap<>();

    public InMemoryMpaRepository() {
        // Инициализация рейтингов MPA
        mpaRatings.put(1, new Mpa(1, "G"));
        mpaRatings.put(2, new Mpa(2, "PG"));
        mpaRatings.put(3, new Mpa(3, "PG-13"));
        mpaRatings.put(4, new Mpa(4, "R"));
        mpaRatings.put(5, new Mpa(5, "NC-17"));
    }

    @Override
    public List<Mpa> findAll() {
        return new ArrayList<>(mpaRatings.values());
    }

    @Override
    public Optional<Mpa> findById(Integer id) {
        return Optional.ofNullable(mpaRatings.get(id));
    }

    @Override
    public boolean existsById(Integer id) {
        return mpaRatings.containsKey(id);
    }
}