package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @Override
    public User addUser(User user) {
        // Обработка имени (если не задано, используем логин)
        handleUserName(user);

        user.setId(currentId++);
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с id {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!containsUser(user.getId())) {
            log.error("Пользователь с id {} не найден", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        // Обработка имени (если не задано, используем логин)
        handleUserName(user);

        users.put(user.getId(), user);
        log.info("Пользователь с id {} успешно обновлен", user.getId());
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        if (!containsUser(id)) {
            log.error("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        users.remove(id);
        log.info("Пользователь с id {} успешно удален", id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public boolean containsUser(Long id) {
        return users.containsKey(id);
    }

    private void handleUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя не задано, используется логин: {}", user.getLogin());
        }
    }
}