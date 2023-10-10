--
-- Muutetaan organisaatiotyypitstr "Toimipiste|Toimipiste|" --> "Toimipiste|"
--

update organisaatio set organisaatiotyypitstr = regexp_replace(organisaatiotyypitstr,'(Toimipiste\|)+','Toimipiste|')
where organisaatiotyypitstr like '%Toimipiste%Toimipiste%'

