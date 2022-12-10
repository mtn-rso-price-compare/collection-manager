INSERT INTO collection (user_id, name) VALUES (1, 'Priljubljeni izdelki');
INSERT INTO tag (name) VALUES ('Meso in mesni izdelki');
INSERT INTO collection_item (collection_id, item_id) VALUES ((SELECT id FROM collection WHERE user_id=1 AND name='Priljubljeni izdelki'), 1);
INSERT INTO tag_item (tag_id, item_id) VALUES ((SELECT id FROM tag WHERE name='Meso in mesni izdelki'), 1);
