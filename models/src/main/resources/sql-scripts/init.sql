INSERT INTO collection (user_id, name, is_locked) VALUES (1, 'Priljubljeno', TRUE);
INSERT INTO collection (user_id, name, is_locked) VALUES (2, 'Priljubljeno', TRUE);
INSERT INTO tag (name) VALUES ('Topli napitki');
INSERT INTO collection_item (collection_id, item_id, amount) VALUES ((SELECT id FROM collection WHERE user_id=1 AND name='Priljubljeno'), 1, 1);
INSERT INTO tag_item (tag_id, item_id) VALUES ((SELECT id FROM tag WHERE name='Topli napitki'), 1);
