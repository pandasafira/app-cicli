-- Script de criacao do banco Cicli (SQLite)

CREATE TABLE active_ingredient (
    id           INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name         TEXT NOT NULL,
    name_search  TEXT NOT NULL
);

CREATE TABLE "alarm" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, user_id INTEGER NOT NULL, hour INTEGER NOT NULL CHECK (hour BETWEEN 0 AND 23), minute INTEGER NOT NULL CHECK (minute BETWEEN 0 AND 59), interval_minutes INTEGER NOT NULL CHECK (interval_minutes >= 0), repeat_limit INTEGER NOT NULL CHECK (repeat_limit BETWEEN 0 AND 10), active INTEGER NOT NULL, created_at TEXT NOT NULL, sound_index INTEGER NOT NULL DEFAULT 0, FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);

CREATE TABLE birth_pill (
    id               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    med_name         TEXT    NOT NULL,
    med_name_search  TEXT    NOT NULL,   -- nome normalizado p/ busca
    active_days      INTEGER NOT NULL,   -- comprimidos com hormônio
    placebo_days     INTEGER NOT NULL,   -- comprimidos de farinha (0 se não tem)
    pause_days       INTEGER NOT NULL,   -- dias sem comprimido (0 se não tem)
    miss_hours       INTEGER,            -- tolerância de atraso, em horas
    source_date      TEXT                -- data da consulta à fonte oficial
, source TEXT NOT NULL DEFAULT 'anvisa');

CREATE TABLE birth_pill_ingredient (
    birth_pill_id  INTEGER NOT NULL,
    ingredient_id  INTEGER NOT NULL,
    PRIMARY KEY (birth_pill_id, ingredient_id),
    FOREIGN KEY (birth_pill_id) REFERENCES birth_pill(id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES active_ingredient(id) ON DELETE CASCADE
);

CREATE TABLE birth_pill_medicine_to_avoid (
    birth_pill_id          INTEGER NOT NULL,
    medicine_to_avoid_id   INTEGER NOT NULL,
    PRIMARY KEY (birth_pill_id, medicine_to_avoid_id),
    FOREIGN KEY (birth_pill_id) REFERENCES birth_pill(id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_to_avoid_id) REFERENCES medicine_to_avoid(id) ON DELETE CASCADE
);

CREATE TABLE medication_history (
    id             INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    user_id        INTEGER NOT NULL,
    pack_id        INTEGER NOT NULL,
    alarm_id       INTEGER,               -- NULL se o alarme for apagado depois
    scheduled_at   TEXT NOT NULL,         -- quando era para tomar
    confirmed_at   TEXT,                  -- NULL se não tomou
    status         TEXT NOT NULL CHECK (status IN ('TAKEN', 'NOT_TAKEN')),
    created_at     TEXT NOT NULL,
    FOREIGN KEY (user_id)  REFERENCES users(id)     ON DELETE CASCADE,
    FOREIGN KEY (pack_id)  REFERENCES pill_pack(id) ON DELETE CASCADE,
    FOREIGN KEY (alarm_id) REFERENCES alarm(id)     ON DELETE SET NULL
);

CREATE TABLE medicine_to_avoid (
    id           INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name         TEXT NOT NULL,
    name_search  TEXT NOT NULL,
    why          TEXT NOT NULL
);

CREATE TABLE pill_pack (
    id             INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    user_id        INTEGER NOT NULL,
    birth_pill_id  INTEGER,               -- NULL se for cartela manual
    active_days    INTEGER NOT NULL,
    placebo_days   INTEGER NOT NULL,
    pause_days     INTEGER NOT NULL,
    start_date     TEXT,                  -- NULL até o usuário informar
    is_current     INTEGER NOT NULL,      -- 1 = cartela em uso
    created_at     TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (birth_pill_id) REFERENCES birth_pill(id) ON DELETE SET NULL
);

CREATE TABLE users (
    id          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name        TEXT    NOT NULL,
    birth_date  TEXT    NOT NULL,
    created_at  TEXT    NOT NULL
);

CREATE INDEX index_active_ingredient_name_search ON active_ingredient (name_search);

CREATE INDEX index_alarm_user_id ON alarm (user_id);

CREATE INDEX index_birth_pill_ingredient_ingredient_id ON birth_pill_ingredient (ingredient_id);

CREATE INDEX index_birth_pill_med_name_search ON birth_pill (med_name_search);

CREATE INDEX index_birth_pill_medicine_to_avoid_medicine_to_avoid_id
    ON birth_pill_medicine_to_avoid (medicine_to_avoid_id);

CREATE INDEX index_medication_history_alarm_id ON medication_history (alarm_id);

CREATE INDEX index_medication_history_pack_id ON medication_history (pack_id);

CREATE UNIQUE INDEX index_medication_history_pack_id_scheduled_at
    ON medication_history (pack_id, scheduled_at);

CREATE INDEX index_medication_history_user_id ON medication_history (user_id);

CREATE INDEX index_medicine_to_avoid_name_search ON medicine_to_avoid (name_search);

CREATE INDEX index_pill_pack_birth_pill_id ON pill_pack (birth_pill_id);

CREATE INDEX index_pill_pack_user_id ON pill_pack (user_id);
