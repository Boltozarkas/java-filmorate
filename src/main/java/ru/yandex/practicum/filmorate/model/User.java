package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private long id;
    private Set<Long> friends = new HashSet<>(); // для хранения друзей

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен содержать символ @ и быть корректным")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелов")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения не может быть пустой")
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    // Если имя не задано, используем логин
    public String getName() {
        if (name == null || name.isBlank()) {
            return login;
        }
        return name;
    }
}