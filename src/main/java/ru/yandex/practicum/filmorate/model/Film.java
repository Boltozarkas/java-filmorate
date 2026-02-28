package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    private long id;
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private long duration;

    @AssertTrue(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    private boolean isReleaseDateValid() {
        if (releaseDate == null) {
            return true;
        }
        return !releaseDate.isBefore(EARLIEST_RELEASE_DATE);
    }
}