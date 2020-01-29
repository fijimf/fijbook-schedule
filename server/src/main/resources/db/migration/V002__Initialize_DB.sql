CREATE TABLE season
(
    id   BIGSERIAL PRIMARY KEY,
    year INT       UNIQUE NOT NULL
);

CREATE UNIQUE INDEX ON season (year);

CREATE TABLE team (
                      id BIGSERIAL PRIMARY KEY,
                      key VARCHAR(48) NOT NULL,
                      name VARCHAR(64) NOT NULL,
                      nickname VARCHAR(64) NOT NULL,
                      logo_url VARCHAR(128) NOT NULL,
                      color1 VARCHAR(48) NOT NULL,
                      color2 VARCHAR(48) NOT NULL
);

CREATE UNIQUE INDEX ON team(key);
CREATE UNIQUE INDEX ON team(name);

CREATE TABLE alias
(
    id      BIGSERIAL    PRIMARY KEY,
    team_id BIGINT       NOT NULL  REFERENCES team(id),
    alias   VARCHAR(128) NOT NULL
);

CREATE UNIQUE INDEX ON alias (alias);

CREATE TABLE conference
(
    id        BIGSERIAL    PRIMARY KEY,
    key       VARCHAR(32)  NOT NULL,
    name      VARCHAR(144) NOT NULL,
    short_name VARCHAR(36) NOT NULL,
    level     VARCHAR(64) NOT NULL,
    logo_url  VARCHAR(256) NULL
);

CREATE UNIQUE INDEX ON conference (key);
CREATE UNIQUE INDEX ON conference (name);

CREATE TABLE conference_mapping
(
    id            BIGSERIAL PRIMARY KEY,
    season_id     BIGINT    NOT NULL REFERENCES season(id),
    conference_id BIGINT    NOT NULL REFERENCES conference(id),
    team_id       BIGINT    NOT NULL REFERENCES team(id)
);

CREATE UNIQUE INDEX ON conference_mapping (season_id, team_id);

CREATE TABLE game
(
    id              BIGSERIAL    PRIMARY KEY,
    season_id       BIGINT       NOT NULL REFERENCES season(id),
    date            DATE         NOT NULL,
    time            TIMESTAMP    NOT NULL,
    home_team_id    BIGINT       NOT NULL REFERENCES team(id),
    away_team_id    BIGINT       NOT NULL REFERENCES team(id),
    location        VARCHAR(128) NULL,
    is_neutral      BOOLEAN      NULL,
    load_key        VARCHAR(32)  NOT NULL
);
CREATE UNIQUE INDEX ON game (date, home_team_id, away_team_id);

CREATE TABLE result
(
    id          BIGSERIAL PRIMARY KEY,
    game_id     BIGINT    NOT NULL REFERENCES game(id),
    home_score  INT       NOT NULL,
    away_score  INT       NOT NULL,
    num_periods INT       NOT NULL
);

CREATE UNIQUE INDEX on result (game_id);
