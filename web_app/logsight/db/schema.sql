CREATE TABLE users.users
(
  id                BIGSERIAL PRIMARY KEY,
  username          TEXT    NOT NULL,
  password          TEXT    NOT NULL,
  email             TEXT    NOT NULL,
  first_name        TEXT NOT NULL,
  last_name         TEXT NOT NULL,
  date_created      TIMESTAMP WITHOUT TIME ZONE
);