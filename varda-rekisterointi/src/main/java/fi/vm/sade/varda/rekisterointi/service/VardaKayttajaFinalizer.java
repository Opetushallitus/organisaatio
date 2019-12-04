package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.client.KayttooikeusClient;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.repository.PaatosRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VardaKayttajaFinalizer {

    private static final String VARDA_TOIMINTAMUOTO_PAIVAKOTI = "vardatoimintamuoto_tm01";
    private static final String VARDA_TOIMINTAMUOTO_PERHEPAIVAHOITO = "vardatoimintamuoto_tm02";
    private static final String VARDA_TOIMINTAMUOTO_RYHMAPERHEPAIVAHOIO = "vardatoimintamuoto_tm03";

    private final PaatosRepository paatosRepository;
    private final KayttooikeusClient kayttooikeusClient;
    private final Map<String,Long> toimintamuotoKayttooikeusRyhmaId;

    public VardaKayttajaFinalizer(PaatosRepository paatosRepository,
                                  KayttooikeusClient kayttooikeusClient,
                                  OphProperties properties) {
        this.paatosRepository = paatosRepository;
        this.kayttooikeusClient = kayttooikeusClient;
        toimintamuotoKayttooikeusRyhmaId = Map.of(
                VARDA_TOIMINTAMUOTO_PAIVAKOTI,
                Long.valueOf(properties.getProperty("varda-rekisterointi.kayttooikeus.ryhma.paivakoti")),
                VARDA_TOIMINTAMUOTO_PERHEPAIVAHOITO,
                Long.valueOf(properties.getProperty("varda-rekisterointi.kayttooikeus.ryhma.perhepaivahoito")),
                VARDA_TOIMINTAMUOTO_RYHMAPERHEPAIVAHOIO,
                Long.valueOf(properties.getProperty("varda-rekisterointi.kayttooikeus.ryhma.ryhmaperhepaivahoito"))
        );
    }

    public void kutsuKayttaja(Rekisterointi rekisterointi) {
        Paatos paatos = paatosRepository.findById(rekisterointi.id).orElseThrow(
                () -> new InvalidInputException("Päätöstä ei löydy rekisteröinnille: " + rekisterointi.id)
        );
        kayttooikeusClient.kutsuKayttaja(
                paatos.paattaja,
                rekisterointi.kayttaja,
                rekisterointi.organisaatio.oid,
                kayttooikeusRyhmaId(rekisterointi.toimintamuoto)
        );
    }

    private Long kayttooikeusRyhmaId(String toimintamuoto) {
        return toimintamuotoKayttooikeusRyhmaId.get(toimintamuoto);
    }
}
