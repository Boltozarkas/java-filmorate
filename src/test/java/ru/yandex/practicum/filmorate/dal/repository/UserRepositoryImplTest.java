package ru.yandex.practicum.filmorate.dal.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({UserRepositoryImpl.class, UserRowMapper.class})
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserRepositoryImplTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldSaveNewUser() {
        User savedUser = userRepository.save(testUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isPositive();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getLogin()).isEqualTo("testlogin");
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldUpdateExistingUser() {
        User savedUser = userRepository.save(testUser);

        savedUser.setName("Updated Name");
        savedUser.setEmail("updated@example.com");

        User updatedUser = userRepository.save(savedUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        User nonExistentUser = new User();
        nonExistentUser.setId(999L);
        nonExistentUser.setEmail("none@example.com");
        nonExistentUser.setLogin("none");
        nonExistentUser.setBirthday(LocalDate.of(1990, 1, 1));

        assertThatThrownBy(() -> userRepository.save(nonExistentUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void shouldFindUserById() {
        User savedUser = userRepository.save(testUser);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getEmail()).isEqualTo(savedUser.getEmail());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        Optional<User> foundUser = userRepository.findById(999L);
        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldFindAllUsers() {
        userRepository.save(testUser);

        User secondUser = new User();
        secondUser.setEmail("second@example.com");
        secondUser.setLogin("secondlogin");
        secondUser.setName("Second User");
        secondUser.setBirthday(LocalDate.of(1995, 5, 5));
        userRepository.save(secondUser);

        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("test@example.com", "second@example.com");
    }

    @Test
    void shouldDeleteUser() {
        User savedUser = userRepository.save(testUser);
        userRepository.deleteById(savedUser.getId());

        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void shouldCheckIfUserExists() {
        User savedUser = userRepository.save(testUser);

        assertThat(userRepository.existsById(savedUser.getId())).isTrue();
        assertThat(userRepository.existsById(999L)).isFalse();
    }

    @Test
    void shouldAddFriend() {
        User user1 = userRepository.save(testUser);

        User user2 = new User();
        user2.setEmail("friend@example.com");
        user2.setLogin("friendlogin");
        user2.setName("Friend User");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        User savedFriend = userRepository.save(user2);

        userRepository.addFriend(user1.getId(), savedFriend.getId());

        List<Long> friends = userRepository.findFriendIds(user1.getId());
        assertThat(friends).contains(savedFriend.getId());
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = userRepository.save(testUser);

        User user2 = new User();
        user2.setEmail("friend@example.com");
        user2.setLogin("friendlogin");
        user2.setName("Friend User");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        User savedFriend = userRepository.save(user2);

        userRepository.addFriend(user1.getId(), savedFriend.getId());
        userRepository.removeFriend(user1.getId(), savedFriend.getId());

        List<Long> friends = userRepository.findFriendIds(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void shouldFindFriendIds() {
        User user1 = userRepository.save(testUser);

        User user2 = createAndSaveUser("friend1@example.com", "friend1", "Friend 1");
        User user3 = createAndSaveUser("friend2@example.com", "friend2", "Friend 2");

        userRepository.addFriend(user1.getId(), user2.getId());
        userRepository.addFriend(user1.getId(), user3.getId());

        List<Long> friends = userRepository.findFriendIds(user1.getId());

        assertThat(friends).hasSize(2);
        assertThat(friends).containsExactlyInAnyOrder(user2.getId(), user3.getId());
    }

    @Test
    void shouldFindCommonFriends() {
        User user1 = userRepository.save(testUser);

        User user2 = createAndSaveUser("user2@example.com", "user2", "User 2");
        User user3 = createAndSaveUser("user3@example.com", "user3", "User 3");
        User commonFriend = createAndSaveUser("common@example.com", "common", "Common Friend");

        userRepository.addFriend(user1.getId(), commonFriend.getId());
        userRepository.addFriend(user2.getId(), commonFriend.getId());

        userRepository.addFriend(user1.getId(), user3.getId());

        List<User> commonFriends = userRepository.findCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(commonFriend.getId());
    }

    @Test
    void shouldFindUserByEmail() {
        User savedUser = userRepository.save(testUser);
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        assertThat(foundUser).isEmpty();
    }

    private User createAndSaveUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userRepository.save(user);
    }
}