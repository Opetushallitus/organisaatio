CREATE EXTENSION IF NOT EXISTS pg_trgm; -- infratiimin pitää ajaa tämä käsin ennen asennusta!
DROP INDEX CONCURRENTLY IF EXISTS organisaatio_parentoidpath_idx;
CREATE INDEX CONCURRENTLY organisaatio_parentoidpath_idx ON organisaatio USING gin (parentoidpath gin_trgm_ops);
