ALTER TABLE paatos DROP COLUMN paattaja_id;
ALTER TABLE paatos ADD COLUMN paattaja_oid text NOT NULL;
