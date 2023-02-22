INSERT INTO venues (building_name, room_name) VALUES ('Building-1', '1-1');
INSERT INTO venues (building_name, room_name) VALUES ('Building-1', '1-2');
INSERT INTO venues (building_name, room_name) VALUES ('Building-1', '1-3');
INSERT INTO venues (building_name, room_name) VALUES ('Building-1', '2-1');
INSERT INTO venues (building_name, room_name) VALUES ('Building-1', '2-2');
INSERT INTO venues (building_name, room_name) VALUES ('Building-1', '2-3');

INSERT INTO staff (first_name, last_name, role) VALUES ('Olivia', 'Smith', 'lecturer');
INSERT INTO staff (first_name, last_name, role) VALUES ('Emma', 'Watson', 'lecturer');
INSERT INTO staff (first_name, last_name, role) VALUES ('Steve', 'Evans', 'lecturer');
INSERT INTO staff (first_name, last_name, role) VALUES ('Elijah', 'Wood', 'lecturer');

INSERT INTO bookings (venue_id, staff_id, booking_length, description) VALUES (1, 1, 60, 'Economics 101 lecture');
INSERT INTO bookings (venue_id, staff_id, booking_length, description) VALUES (2, 2, 60, 'Business 101 lecture');