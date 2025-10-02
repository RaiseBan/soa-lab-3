-- Create database
CREATE DATABASE musicband_db;

-- Connect to the database
\c musicband_db;

-- Create user (if needed)
CREATE USER musicband_user WITH PASSWORD 'musicband_pass';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE musicband_db TO musicband_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO musicband_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO musicband_user;

-- Table will be created automatically by Hibernate with hbm2ddl.auto=update
-- But here's the expected schema for reference:

/*
CREATE TABLE music_bands (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    x DOUBLE PRECISION,
    y BIGINT NOT NULL CHECK (y <= 945),
    creation_date DATE NOT NULL,
    number_of_participants INTEGER NOT NULL CHECK (number_of_participants > 0),
    albums_count INTEGER CHECK (albums_count IS NULL OR albums_count > 0),
    genre VARCHAR(50) NOT NULL,
    sales DOUBLE PRECISION CHECK (sales IS NULL OR sales > 0)
);

CREATE INDEX idx_music_bands_name ON music_bands(name);
CREATE INDEX idx_music_bands_genre ON music_bands(genre);
CREATE INDEX idx_music_bands_participants ON music_bands(number_of_participants);
*/