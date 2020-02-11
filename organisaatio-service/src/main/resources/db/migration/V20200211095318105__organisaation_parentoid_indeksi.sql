-- ajetaan mieluiten manuaalisesti sopivana ajankohtana concurrently:
-- CREATE INDEX CONCURRENTLY organisaatio_parentoidpath_trgm_idx ON organisaatio USING gin (parentoidpath gin_trgm_ops);
CREATE INDEX IF NOT EXISTS organisaatio_parentoidpath_trgm_idx ON organisaatio USING gin (parentoidpath gin_trgm_ops);
