--
-- Lisätään MKT columnit ects-tiedoille
--

ALTER TABLE organisaatiometadata ADD COLUMN hakutoimistoectsemailmkt bigint;

ALTER TABLE organisaatiometadata ADD COLUMN hakutoimistoectsnimimkt bigint;

ALTER TABLE organisaatiometadata ADD COLUMN hakutoimistoectspuhelinmkt bigint;

ALTER TABLE organisaatiometadata ADD COLUMN hakutoimistoectstehtavanimikemkt bigint;
