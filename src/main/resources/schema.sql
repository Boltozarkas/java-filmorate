-- Удаляем таблицы, если они существуют (для чистой инициализации)
DROP TABLE IF EXISTS film_genre CASCADE;
DROP TABLE IF EXISTS film_likes CASCADE;
DROP TABLE IF EXISTS user_friends CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS mpa CASCADE;

-- Таблица рейтингов MPA
CREATE TABLE IF NOT EXISTS mpa (
    id INTEGER PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE
);

-- Таблица жанров
CREATE TABLE IF NOT EXISTS genres (
    id INTEGER PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Таблица фильмов
CREATE TABLE IF NOT EXISTS films (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration BIGINT NOT NULL CHECK (duration > 0),
    mpa_id INTEGER,
    FOREIGN KEY (mpa_id) REFERENCES mpa(id) ON DELETE SET NULL
);

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    birthday DATE NOT NULL
);

-- Таблица связи фильмов с жанрами (многие ко многим)
CREATE TABLE IF NOT EXISTS film_genre (
    film_id BIGINT NOT NULL,
    genre_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE RESTRICT
);

-- Таблица лайков фильмов
CREATE TABLE IF NOT EXISTS film_likes (
    film_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Таблица друзей (односторонняя дружба)
CREATE TABLE IF NOT EXISTS user_friends (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (user_id != friend_id)
);

-- Создание индексов для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_film_mpa ON films(mpa_id);
CREATE INDEX IF NOT EXISTS idx_film_genre_film ON film_genre(film_id);
CREATE INDEX IF NOT EXISTS idx_film_genre_genre ON film_genre(genre_id);
CREATE INDEX IF NOT EXISTS idx_likes_film ON film_likes(film_id);
CREATE INDEX IF NOT EXISTS idx_likes_user ON film_likes(user_id);
CREATE INDEX IF NOT EXISTS idx_friends_user ON user_friends(user_id);
CREATE INDEX IF NOT EXISTS idx_friends_friend ON user_friends(friend_id);