package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.varda.rekisterointi.client.KoodistoClient;
import fi.vm.sade.varda.rekisterointi.model.Koodi;
import fi.vm.sade.varda.rekisterointi.model.KoodistoType;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/koodisto")
public class KoodistoController {

    private final KoodistoClient koodistoClient;

    public KoodistoController(KoodistoClient koodistoClient) {
        this.koodistoClient = koodistoClient;
    }

    @GetMapping("/{koodisto}/koodi")
    Collection<Koodi> getKoodi(@PathVariable KoodistoType koodisto,
                               @RequestParam(required = false) Optional<Integer> versio,
                               @RequestParam(required = false) Optional<Boolean> onlyValid) {
        if (KoodistoType.YRITYSMUOTO.equals(koodisto)) {
            return yritysmuotoKoodistoMock();
        }
        return koodistoClient.listKoodit(koodisto, versio, onlyValid);
    }

    private static Collection<Koodi> yritysmuotoKoodistoMock() {
        return Arrays.asList(
                yritysmuotoKoodiMock("0", "Ei yritysmuotoa"),
                yritysmuotoKoodiMock("6", "Aatteellinen yhdistys"),
                yritysmuotoKoodiMock("64", "Ahvenanmaan liikelaitos"),
                yritysmuotoKoodiMock("43", "Ahvenanmaan maakunta ja sen virastot"),
                yritysmuotoKoodiMock("3", "Asukashallintoalue"),
                yritysmuotoKoodiMock("4", "Asumisoikeusyhdistys"),
                yritysmuotoKoodiMock("2", "Asunto-osakeyhtiö"),
                yritysmuotoKoodiMock("5", "Avoin yhtiö"),
                yritysmuotoKoodiMock("57", "Elinkeinoyhtymä"),
                yritysmuotoKoodiMock("32", "Eläkesäätiö"),
                yritysmuotoKoodiMock("48", "Erillishallinnollinen valtion laitos"),
                yritysmuotoKoodiMock("31", "Erityislainsäädäntöön perustuva yhdistys"),
                yritysmuotoKoodiMock("7", "Euroopp.taloudell.etuyht.sivutoimipaikka"),
                yritysmuotoKoodiMock("8", "Eurooppalainen taloudellinen etuyhtymä"),
                yritysmuotoKoodiMock("85", "Eurooppaosuuskunnan kiinteä toimipaikka"),
                yritysmuotoKoodiMock("83", "Eurooppaosuuskunta"),
                yritysmuotoKoodiMock("84", "Eurooppaosuuspankki"),
                yritysmuotoKoodiMock("80", "Eurooppayhtiö"),
                yritysmuotoKoodiMock("44", "Ev.lut.kirkko"),
                yritysmuotoKoodiMock("9", "Hypoteekkiyhdistys"),
                yritysmuotoKoodiMock("11", "Julkinen keskinäinen vakuutusyhtiö"),
                yritysmuotoKoodiMock("17", "Julkinen osakeyhtiö"),
                yritysmuotoKoodiMock("23", "Julkinen vakuutusosakeyhtiö"),
                yritysmuotoKoodiMock("70", "Kauppakamari"),
                yritysmuotoKoodiMock("10", "Keskinäinen kiinteistöosakeyhtiö"),
                yritysmuotoKoodiMock("58", "Keskinäinen vahinkovak.yhdistys"),
                yritysmuotoKoodiMock("12", "Keskinäinen vakuutusyhtiö"),
                yritysmuotoKoodiMock("13", "Kommandiittiyhtiö"),
                yritysmuotoKoodiMock("54", "Konkurssipesä"),
                yritysmuotoKoodiMock("61", "Kunnallinen liikelaitos"),
                yritysmuotoKoodiMock("41", "Kunta"),
                yritysmuotoKoodiMock("62", "Kuntainliiton liikelaitos"),
                yritysmuotoKoodiMock("42", "Kuntayhtymä"),
                yritysmuotoKoodiMock("53", "Kuolinpesä"),
                yritysmuotoKoodiMock("28", "Laivanisännistöyhtiö"),
                yritysmuotoKoodiMock("35", "Metsänhoitoyhdistys"),
                yritysmuotoKoodiMock("49", "Muu julkisoikeudellinen oikeushenkilö"),
                yritysmuotoKoodiMock("56", "Muu kiinteistöosakeyhtiö"),
                yritysmuotoKoodiMock("39", "Muu säätiö"),
                yritysmuotoKoodiMock("38", "Muu taloudellinen yhdistys"),
                yritysmuotoKoodiMock("59", "Muu verotuksen yksikkö"),
                yritysmuotoKoodiMock("29", "Muu yhdistys"),
                yritysmuotoKoodiMock("52", "Muu yhteisvast.pidätysvelvollinen"),
                yritysmuotoKoodiMock("30", "Muu yhtiö"),
                yritysmuotoKoodiMock("63", "Muut oikeushenkilöt"),
                yritysmuotoKoodiMock("45", "Ortodoksinen kirkko"),
                yritysmuotoKoodiMock("16", "Osakeyhtiö"),
                yritysmuotoKoodiMock("14", "Osuuskunta"),
                yritysmuotoKoodiMock("15", "Osuuspankki"),
                yritysmuotoKoodiMock("90", "Paliskunta"),
                yritysmuotoKoodiMock("71", "Seurakunta/Paikallisyhteisö"),
                yritysmuotoKoodiMock("19", "Sivuliike"),
                yritysmuotoKoodiMock("20", "Säästöpankki"),
                yritysmuotoKoodiMock("18", "Säätiö"),
                yritysmuotoKoodiMock("21", "Taloudellinen yhdistys"),
                yritysmuotoKoodiMock("33", "Työeläkekassa"),
                yritysmuotoKoodiMock("37", "Työttömyyskassa"),
                yritysmuotoKoodiMock("60", "Ulkomainen yhteisö"),
                yritysmuotoKoodiMock("46", "Uskonnollinen yhdyskunta"),
                yritysmuotoKoodiMock("36", "Vakuutuskassa"),
                yritysmuotoKoodiMock("24", "Vakuutusosakeyhtiö"),
                yritysmuotoKoodiMock("25", "Vakuutusyhdistys"),
                yritysmuotoKoodiMock("40", "Valtio ja sen laitokset"),
                yritysmuotoKoodiMock("22", "Valtion liikelaitos"),
                yritysmuotoKoodiMock("51", "Verotusyhtymä"),
                yritysmuotoKoodiMock("50", "Yhteisetuudet"),
                yritysmuotoKoodiMock("55", "Yhteismetsä"),
                yritysmuotoKoodiMock("26", "Yksityinen elinkeinonharjoittaja"),
                yritysmuotoKoodiMock("47", "Ylioppilaskunta tai osakunta")
        );
    }

    private static Koodi yritysmuotoKoodiMock(String arvo, String nimiFi) {
        Koodi koodi = new Koodi();
        koodi.uri = String.format("yritysmuoto_%s", arvo);
        koodi.arvo = arvo;
        koodi.nimi = Map.of("fi", nimiFi);
        return koodi;
    }

}
