BEGIN;

CREATE TABLE IF NOT EXISTS movies (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    director TEXT NOT NULL,
    main_cast TEXT,
    genres TEXT[] DEFAULT '{}',
    runtime_minutes INTEGER,
    release_date DATE,
    synopsis TEXT,
    poster_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS halls (
    id BIGSERIAL PRIMARY KEY,
    alias TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS movie_sessions (
    movie_id BIGINT, 
    hall_id BIGINT,
    timeslot_start TIMESTAMP NOT NULL,
    timeslot_end TIMESTAMP NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (movie_id, hall_id, timeslot_start),
    CONSTRAINT fk_movie_id FOREIGN KEY (movie_id) REFERENCES movies(id),
    CONSTRAINT fk_hall_id FOREIGN KEY (hall_id) REFERENCES halls(id)
);

COMMIT;
