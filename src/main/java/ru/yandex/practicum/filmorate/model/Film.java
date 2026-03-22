package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private long id;
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private Set<Long> likes = new HashSet<>(); // для хранения лайков

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private long duration;

    @NotBlank(message = "Жанр не может быть пустым")
    private String genre;

//            Комедия.
//            Драма.
//            Мультфильм.
//            Триллер.
//            Документальный.
//            Боевик.

    @NotBlank(message = "Возрастной рейтинг не может быть пустым")
    private String rating;

//    G — у фильма нет возрастных ограничений,
//    PG — детям рекомендуется смотреть фильм с родителями,
//    PG-13 — детям до 13 лет просмотр не желателен,
//    R — лицам до 17 лет просматривать фильм можно только в присутствии взрослого,
//    NC-17 — лицам до 18 лет просмотр запрещён.

    @AssertTrue(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    private boolean isReleaseDateValid() {
        if (releaseDate == null) {
            return true;
        }
        return !releaseDate.isBefore(EARLIEST_RELEASE_DATE);
    }
}