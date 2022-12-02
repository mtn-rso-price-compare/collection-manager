INSERT INTO collection (user_id, name) VALUES (0, 'Priljubljeni izdelki');
INSERT INTO tag (name) VALUES ('Meso in mesni izdelki');
INSERT INTO store (name, url) VALUES ('ENGROTUŠ d.o.o.', 'https://www.tus.si');
INSERT INTO collection_item (collection_id, item_id) VALUES ((SELECT id FROM collection WHERE user_id=0 AND name='Priljubljeni izdelki'), 0);
INSERT INTO tag_item (tag_id, item_id) VALUES ((SELECT id FROM tag WHERE name='Meso in mesni izdelki'), 0);
