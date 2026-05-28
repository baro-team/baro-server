CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS vehicle_status (
    vehicle_id    VARCHAR(50)      PRIMARY KEY,
    location      GEOMETRY(POINT, 4326),
    latitude      DOUBLE PRECISION,
    longitude     DOUBLE PRECISION,
    speed         INTEGER,
    heading       DOUBLE PRECISION,
    battery       DOUBLE PRECISION,
    autonomy_mode VARCHAR(20),
    status        VARCHAR(20),
    trip_id       VARCHAR(100),
    engine_oil    DOUBLE PRECISION,
    brake_oil     DOUBLE PRECISION,
    washer_fluid  DOUBLE PRECISION,
    ext_temp      DOUBLE PRECISION,
    last_seen     VARCHAR(50),
    updated_at    TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_vehicle_status_location
    ON vehicle_status USING GIST(location);
