--
-- Muutetaan vuodenvaihteen virheelliset liitokset historiaksi
--

update organisaatiosuhde set suhdetyyppi = 'HISTORIA' where suhdetyyppi = 'LIITOS' and alkupvm > '2014-12-01';