package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);

        // Обработка имени (если не задано, используем логин)
        handleUserName(user);

        user.setId(currentId++);
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с id {}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя: {}", user);

        // Проверка существования пользователя
        validateUserExists(user);

        // Обработка имени (если не задано, используем логин)
        handleUserName(user);

        users.put(user.getId(), user);
        log.info("Пользователь с id {} успешно обновлен", user.getId());
        return user;
    }

    //Проверяет, что пользователь существует в хранилище
    private void validateUserExists(User user) {
        if (user.getId() <= 0) {
            log.error("ID пользователя должен быть указан");
            throw new ValidationException("ID пользователя должен быть указан");
        }

        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id {} не найден", user.getId());
            throw new ValidationException("Пользователь с id " + user.getId() + " не найден");
        }
    }

    //Обрабатывает имя пользователя:
    //если имя не задано или пустое, использует логин
    private void handleUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя не задано, используется логин: {}", user.getLogin());
        }
    }
}