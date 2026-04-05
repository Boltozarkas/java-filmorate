package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repository.MpaRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Service
public class MpaService {
    private final MpaRepository mpaRepository;

    @Autowired
    public MpaService(@Qualifier("mpaRepositoryImpl") MpaRepository mpaRepository) {
        this.mpaRepository = mpaRepository;
    }

    public List<Mpa> getAllMpa() {
        return mpaRepository.findAll();
    }

    public Mpa getMpaById(Integer id) {
        return mpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + id + " не найден"));
    }
}