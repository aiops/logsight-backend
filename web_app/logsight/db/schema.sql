CREATE TABLE users
(
  id                BIGSERIAL PRIMARY KEY,
  username          TEXT    NOT NULL,
  password          TEXT    NOT NULL,
  email             TEXT    NOT NULL,
  first_name        TEXT NOT NULL,
  last_name         TEXT NOT NULL,
  date_created      TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE applications
(
  id                BIGSERIAL PRIMARY KEY,
  name              TEXT    NOT NULL,
  user_id           BIGINT REFERENCES users(id)
);