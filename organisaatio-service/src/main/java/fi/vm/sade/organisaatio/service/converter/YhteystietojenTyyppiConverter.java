package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.YhteystietoElementti;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;

import javax.persistence.EntityManager;
import java.util.Map;
import java.util.Set;

public class YhteystietojenTyyppiConverter extends Converter<YhteystietojenTyyppiDTO, YhteystietojenTyyppi> {

    public YhteystietojenTyyppiConverter(ConverterFactory converterFactory, EntityManager entityManager) {
        super(converterFactory, entityManager);
    }

    @Override
    public void setValuesToDTO(YhteystietojenTyyppi entity, YhteystietojenTyyppiDTO dto) {
        for (String oty : entity.getSovellettavatOrganisaatioTyyppis()) {
            dto.getSovellettavatOrganisaatios().add(oty);
        }

        dto.setNimi(convertNimiToDto(entity));

        dto.getSovellettavatOppilaitostyyppis().addAll(entity.getSovellettavatOppilaitostyyppis());

        dto.getAllLisatietokenttas().addAll(converterFactory.convertToDTO(entity.getLisatietos(), YhteystietoElementtiDTO.class));//setAllLisatietokenttas(converterFactory.convertToDTO(entity.getLisatietos(), YhteystietoElementtiDTO.class));
    }

    private MonikielinenTekstiTyyppi convertNimiToDto(YhteystietojenTyyppi entity) {
    	MonikielinenTeksti nimiE = entity.getNimi();
    	if (nimiE == null) {
    		return null;
    	}
    	MonikielinenTekstiTyyppi nimiDTO = new MonikielinenTekstiTyyppi();
    	for (Map.Entry<String, String> e : nimiE.getValues().entrySet()) {
    		Teksti curTeksti = new Teksti();
    		curTeksti.setKieliKoodi(e.getKey());
    		curTeksti.setValue(e.getValue());
    		nimiDTO.getTeksti().add(curTeksti);
    	}
    	return nimiDTO;
    }

    @Override
    public void setValuesToJPA(YhteystietojenTyyppiDTO dto, YhteystietojenTyyppi entity, boolean merge) {
        entity.setSovellettavatOrganisaatioTyyppis(dto.getSovellettavatOrganisaatios());

        entity.setSovellettavatOppilaitostyyppis(dto.getSovellettavatOppilaitostyyppis());

        entity.setNimi(convertNimiToJpa(dto));

        Set<YhteystietoElementtiDTO> newLisatietokenttas = dto.getAllLisatietokenttas();

        // jos ollaan updateamassa
        // katsotaan ollaanko poistettua kenttää luomassa uudestaan -> muutetaankin olemassaolevaa
        YhteystietojenTyyppi oldVersion = null;
        if (entity.getId() != null) {

            oldVersion = entityManager.find(YhteystietojenTyyppi.class, entity.getId());
            for (YhteystietoElementti oldKentta : oldVersion.getLisatietos()) {
                YhteystietoElementtiDTO newKentta = getLisatietokentta(dto, oldKentta.getNimi());
                boolean creatingSameKenttaAgain = !oldKentta.isKaytossa() && newKentta != null;
                if (creatingSameKenttaAgain) {
                    newKentta.setOid(oldKentta.getOid());
                }
            }
        }

        Set<YhteystietoElementti> newLisatietos = converterFactory.convertYhteystietoElementtisToJPA(newLisatietokenttas, merge);

        // jos ollaan updateamassa
        // katsotaan onko kenttiä poistumassa -> merkataan poistetuksi poistamisen sijaan
        if (entity.getId() != null) {

            for (YhteystietoElementti oldKentta : oldVersion.getLisatietos()) {
                boolean existsInNewVersion = newLisatietos.contains(oldKentta);
                if (!existsInNewVersion) {
                    oldKentta.setKaytossa(false);
                    newLisatietos.add(oldKentta);
                    oldKentta.setYhteystietojenTyyppi(entity);
                }
            }
        }

        entity.setLisatietos(newLisatietos);

    }

    private MonikielinenTeksti convertNimiToJpa(YhteystietojenTyyppiDTO dto) {
    	MonikielinenTekstiTyyppi nimiDTO = dto.getNimi();
    	if (nimiDTO == null) {
    		return null;
    	}
    	MonikielinenTeksti nimiE = new MonikielinenTeksti();
    	for (Teksti curTeksti : dto.getNimi().getTeksti()) {
    		nimiE.addString(curTeksti.getKieliKoodi(), curTeksti.getValue());
    	}
    	return nimiE;
    }

    private YhteystietoElementtiDTO getLisatietokentta(YhteystietojenTyyppiDTO dto, String kenttaNimi) {
        for (YhteystietoElementtiDTO kentta : dto.getAllLisatietokenttas()) {
            if (kentta.getNimi().equals(kenttaNimi)) {
                return kentta;
            }
        }
        return null;
    }
}