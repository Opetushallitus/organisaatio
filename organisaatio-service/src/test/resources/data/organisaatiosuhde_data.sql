-- Organisations --
insert into organisaatio (id, version, oid) values
  (1, 1, '23456.0'),
  (2, 1, '23457.0'),
  (3, 1, '23458.0'),
  (4, 1, '23459.0'),
  (5, 1, '23460.0'),
  (6, 1, '23461.0'),
  (7, 1, '23462.0');
-- Organisation relations --
insert into organisaatiosuhde (id, version, suhdetyyppi, child_id, parent_id, alkupvm) values
  (1, 1, 'HISTORIA', 2, 1, '2000-01-01'),
  (2, 1, 'HISTORIA', 2, 1, '2000-01-01'),
  (3, 1, 'HISTORIA', 3, 1, '2000-01-01'),
  (4, 1, 'HISTORIA', 5, 4, '2000-01-01'),
  (5, 1, 'HISTORIA', 6, 4, '2000-01-02'),
  (6, 1, 'HISTORIA', 7, 4, '1999-12-31');