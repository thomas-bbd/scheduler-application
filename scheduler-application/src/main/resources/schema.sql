DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS venues;

CREATE TABLE IF NOT EXISTS bookings(
    id int identity primary key,
    venue_id int not null,
    staff_id int not null,
    description varchar(100) not null
);

CREATE TABLE IF NOT EXISTS staff(
   id int identity primary key,
   first_name varchar(100) not null,
   last_name varchar(100) not null,
   role varchar(100) not null
);

CREATE TABLE IF NOT EXISTS venues(
    id int identity primary key,
    building_name varchar(100) not null,
    room_name varchar(100) not null
);