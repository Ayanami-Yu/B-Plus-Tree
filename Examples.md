## Create
CREATE SCHEMA music;

CREATE SCHEMA phone_book;

CREATE TABLE music.singers (
    id int,
    name char,
    genre char
); 

CREATE TABLE phone_book.dir (
    id int,
    name char,
    number char,
    addr char
);

CREATE INDEX genre
ON music.singers (genre);

CREATE INDEX name
ON phone_book.dir (name);

## Insert
insert into music.singers (id, name, genre)
values (1, 'David Bowie', 'Glam');

insert into music.singers (id, name, genre)
values (2, 'Nirvana', 'Grunge');

insert into music.singers (id, name, genre)
values (3, 'My Bloody Valentine', 'Shoegaze');

insert into music.singers (id, name, genre)
values (4, 'Led Zeppelin', 'Heavy Metal');

insert into music.singers (id, name, genre)
values (5, 'Sex Pistols', 'Punk');

insert into music.singers (id, name, genre)
values (6, 'Talking Heads', 'New Wave');

insert into music.singers (id, name, genre)
values (7, 'The Jesus And Mary Chain', 'Shoegaze');

insert into music.singers (id, name, genre)
values (8, 'Slowdive', 'Shoegaze');

insert into music.singers (id, name, genre)
values (9, 'Television', 'Punk');

insert into music.singers (id, name, genre)
values (10, 'Radiohead', 'Art Rock');

insert into music.singers (id, name, genre)
values (11, 'Sonic Youth', 'Noise Rock');

insert into music.singers (id, name, genre)
values (12, 'Sonic Youth', 'Experimental Rock');

insert into music.singers (id, name, genre)
values (13, 'Sonic Youth', 'Indie Rock');

insert into music.singers (id, name, genre)
values (14, 'Sonic Youth', 'Alternative Rock');

## Select
SELECT name, genre
FROM music.singers;

SELECT * FROM music.singers;

SELECT name, genre
FROM music.singers
WHERE id = 7;

SELECT name, genre
FROM music.singers
WHERE name = 'My Bloody Valentine';

SELECT genre
FROM music.singers
WHERE name = 'Talking Heads';

SELECT *
FROM music.singers
WHERE name = 'Sonic Youth';

SELECT *
FROM music.singers
WHERE genre = 'Shoegaze';

SELECT name, number, addr
FROM phone_book.dir
WHERE name = 'David Bowie';

## Delete
DELETE FROM music.singers WHERE genre = 'New Wave';

DELETE FROM music.singers WHERE id = 1;

DELETE FROM music.singers WHERE name = 'Sonic Youth';

DELETE FROM music.singers;

DELETE FROM music.singers WHERE genre = 'Shoegaze';

DELETE FROM phone_book.dir WHERE name = 'David Bowie';