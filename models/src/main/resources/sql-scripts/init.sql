INSERT INTO request (status, last_updated) VALUES (0, TIMESTAMP '2022-11-26 10:45:48');
INSERT INTO item (name) VALUES ('piščančje prsi');
INSERT INTO store (name, url) VALUES ('ENGROTUŠ d.o.o.', 'https://www.tus.si');
INSERT INTO price (item_id, store_id, amount) VALUES ((SELECT id FROM item WHERE name='piščančje prsi'), (SELECT id FROM store WHERE name='ENGROTUŠ d.o.o.'), 3.59);
