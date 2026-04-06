package ru.yandex.practicum.filmorate.dal.repository;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    // CRUD операции
    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    // Операции с друзьями
    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<Long> findFriendIds(Long userId);

    // Поисковые запросы
    List<User> findCommonFriends(Long userId, Long otherId);

    Optional<User> findByEmail(String email);
}