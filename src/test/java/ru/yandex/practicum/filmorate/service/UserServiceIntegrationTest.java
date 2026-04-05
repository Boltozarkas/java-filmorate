package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

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
    void shouldAddUser() {
        User user = userService.addUser(testUser);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isPositive();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        testUser.setName(null);
        User user = userService.addUser(testUser);

        assertThat(user.getName()).isEqualTo(user.getLogin());
    }

    @Test
    void shouldUpdateUser() {
        User savedUser = userService.addUser(testUser);
        savedUser.setName("Updated Name");

        User updatedUser = userService.updateUser(savedUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldGetUserById() {
        User savedUser = userService.addUser(testUser);
        User foundUser = userService.getUserById(savedUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void shouldAddFriend() {
        User user1 = userService.addUser(testUser);

        User user2 = new User();
        user2.setEmail("friend@example.com");
        user2.setLogin("friend");
        user2.setName("Friend");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User friend = userService.addUser(user2);

        userService.addFriend(user1.getId(), friend.getId());

        List<User> friends = userService.getUserFriends(user1.getId());
        assertThat(friends).contains(friend);
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = userService.addUser(testUser);

        User user2 = new User();
        user2.setEmail("friend@example.com");
        user2.setLogin("friend");
        user2.setName("Friend");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User friend = userService.addUser(user2);

        userService.addFriend(user1.getId(), friend.getId());
        userService.removeFriend(user1.getId(), friend.getId());

        List<User> friends = userService.getUserFriends(user1.getId());
        assertThat(friends).doesNotContain(friend);
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = userService.addUser(testUser);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User secondUser = userService.addUser(user2);

        User commonFriend = new User();
        commonFriend.setEmail("common@example.com");
        commonFriend.setLogin("common");
        commonFriend.setName("Common Friend");
        commonFriend.setBirthday(LocalDate.of(1992, 1, 1));
        User savedCommonFriend = userService.addUser(commonFriend);

        userService.addFriend(user1.getId(), savedCommonFriend.getId());
        userService.addFriend(secondUser.getId(), savedCommonFriend.getId());

        List<User> commonFriends = userService.getCommonFriends(user1.getId(), secondUser.getId());

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(savedCommonFriend.getId());
    }
}