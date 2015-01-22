-- Organisations --
insert into organisaatio (id, version) values
  (1, 1),
  (2, 1),
  (3, 1),
  (4, 1),
  (5, 1),
  (6, 1),
  (7, 1);
-- Organisation relations --
insert into organisaatiosuhde (id, version, suhdetyyppi, child_id, parent_id, alkupvm) values
  (1, 1, 'HISTORIA', 2, 1, '2000-01-01'),
  (2, 1, 'HISTORIA', 2, 1, '2000-01-01'),
  (3, 1, 'HISTORIA', 3, 1, '2000-01-01'),
  (4, 1, 'HISTORIA', 5, 4, '2000-01-01'),
  (5, 1, 'HISTORIA', 6, 4, '2000-01-02'),
  (6, 1, 'HISTORIA', 7, 4, '1999-12-31');