CREATE TABLE alias
(
    id      BIGSERIAL    NOT NULL,
    team_id BIGINT       NOT NULL,
    alias   VARCHAR(128) NOT NULL
);
CREATE UNIQUE INDEX ON alias (alias);

CREATE TABLE conference
(
    id        BIGSERIAL    NOT NULL,
    key       VARCHAR(32)  NOT NULL,
    name      VARCHAR(144) NOT NULL,
    long_name VARCHAR(256) NOT NULL,
    logo_url  VARCHAR(256) NULL
);

CREATE UNIQUE INDEX ON conference (key);

CREATE TABLE conference_mapping
(
    id            BIGSERIAL NOT NULL,
    season_id     BIGINT    NOT NULL,
    conference_id BIGINT    NOT NULL,
    team_id       BIGINT    NOT NULL
);

CREATE UNIQUE INDEX ON conference_mapping (season_id, team_id);

CREATE TABLE game
(
    id              BIGSERIAL    NOT NULL,
    season_id       BIGINT       NOT NULL,
    date            DATE         NOT NULL,
    time            TIMESTAMP    NOT NULL,
    home_team_id    BIGINT       NOT NULL,
    away_team_id    BIGINT       NOT NULL,
    location        VARCHAR(128) NULL,
    is_neutral      BOOLEAN      NULL,
    load_key        VARCHAR(32)  NOT NULL
);
CREATE UNIQUE INDEX ON game (date, home_team_id, away_team_id);

CREATE TABLE result
(
    id          BIGSERIAL NOT NULL,
    game_id     BIGINT    NOT NULL,
    home_score  INT       NOT NULL,
    away_score  INT       NOT NULL,
    num_periods INT       NOT NULL
);

CREATE UNIQUE INDEX on result (game_id);

CREATE TABLE season
(
    id   BIGSERIAL NOT NULL,
    year INT       NOT NULL
);

CREATE UNIQUE INDEX ON season (year);

CREATE TABLE team (
    id BIGSERIAL NOT NULL,
    key VARCHAR(48) NOT NULL,
    name VARCHAR(64) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    logo_url VARCHAR(128) NOT NULL,
    color1 VARCHAR(48) NOT NULL,
    color2 VARCHAR(48) NOT NULL
);

CREATE UNIQUE INDEX ON team(key);
CREATE UNIQUE INDEX ON team(name);
