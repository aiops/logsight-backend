CREATE TABLE users
(
    id              BIGSERIAL PRIMARY KEY,
    password        TEXT    NOT NULL,
    email           TEXT    NOT NULL unique ,
    key             TEXT,
    date_created    TIMESTAMP WITHOUT TIME ZONE,
    activation_date TIMESTAMP WITHOUT TIME ZONE,
    activated       boolean not null default false
);

CREATE TABLE applications
(
  id                BIGSERIAL PRIMARY KEY,
  name              TEXT    NOT NULL,
  status            TEXT    NOT NULL default 'IN_PROGRESS',
  user_id           BIGINT REFERENCES users(id)
);