CREATE TABLE author (
    id serial PRIMARY KEY,
    full_name varchar(200) NOT NULL,
    created timestamp DEFAULT now()
);