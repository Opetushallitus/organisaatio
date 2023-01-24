package fi.vm.sade.organisaatio.ytj.mock;

import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJKieli;
import fi.vm.sade.organisaatio.ytj.api.YTJOsoiteDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjConnectionException;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjExceptionType;
import fi.ytj.YTunnusDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock for YTJService.
 *
 * @author mlyly
 */
public class YTJServiceMock implements YTJService {

    public YTJServiceMock() {
        initMockData();
    }

    @Override
    public YTJDTO findByYTunnus(String ytunnus, YTJKieli kieli) throws YtjConnectionException {
        if (ytunnus != null && ytunnus.isEmpty())
            throw new YtjConnectionException(YtjExceptionType.SOAP, "empty ytunnus");
        return dataByYTunnus.get(ytunnus);
    }

    // Stupid mock that just adds values to the list even if value is not found and null is added to the list.
    @Override
    public List<YTJDTO> findByYTunnusBatch(List<String> ytunnuses, YTJKieli ytjKieli) throws YtjConnectionException {
        if (ytunnuses.isEmpty()) throw new YtjConnectionException(YtjExceptionType.SOAP, "empty ytunnuses");
        List<YTJDTO> ytjdtos = new ArrayList<>();
        for (String str : ytunnuses) {
            ytjdtos.add(findByYTunnus(str, ytjKieli));
        }
        return ytjdtos;
    }

    @Override
    public List<YTJDTO> findByYNimi(String nimi, boolean naytaPassiiviset, YTJKieli kieli) throws YtjConnectionException {

        if (nimi.equals("-")) throw new YtjConnectionException(YtjExceptionType.SOAP, "empty nimi (-)");
        if (nimi.equals("x")) throw new YtjConnectionException(YtjExceptionType.OTHER, "empty nimi (x)");
        List<YTJDTO> result = new ArrayList<>();

        for (YTJDTO ytjdto : dataByYTunnus.values()) {
            if (ytjdto.getNimi().contains(nimi)) {
                result.add(ytjdto);
            }
        }

        return result;
    }

    private final Map<String, YTJDTO> dataByYTunnus = new HashMap<>();

    private void initMockData() {
        for (String[] strings : MOCK_DATA) {
            dataByYTunnus.put(strings[0], create(strings[0], strings[1], strings[2]));
        }
    }

    private YTJDTO create(String ytunnus, String nimi, String yritysmuoto) {
        YTJDTO dto = new YTJDTO();
        dto.setYtunnus(ytunnus);
        if (ytunnus.trim().equals("2255802-1")) {
            YTJOsoiteDTO osoite = new YTJOsoiteDTO();
            osoite.setKatu("Ygankuja 1");
            osoite.setMaa("Suomi");
            osoite.setMaakoodi("FI");
            osoite.setToimipaikka("Helsinki");
            osoite.setPostinumero("00100");
            osoite.setKieli(1);
            dto.setPostiOsoite(osoite);
            dto.setYrityksenKieli("Suomi");
            dto.setSahkoposti("example@example.com");
            dto.setAloitusPvm("10.02.1999");
            dto.setNimi(nimi);
            dto.setYritysTunnus(new YTunnusDTO() {{
                setAlkupvm("28.02.2011");
            }});
            dto.setYritysmuoto(yritysmuoto);
        } else if (ytunnus.trim().equals("1492449-0")) {
            YTJOsoiteDTO osoite = new YTJOsoiteDTO();
            osoite.setKatu("Svenska gatan 1");
            osoite.setMaa("Ruotsi");
            osoite.setMaakoodi("SV");
            osoite.setToimipaikka("Helsinki");
            osoite.setPostinumero("00100");
            osoite.setKieli(2);
            dto.setPostiOsoite(osoite);
            dto.setYrityksenKieli("Svenska");
            dto.setSahkoposti("example@example.com");
            dto.setWww("http://www.ytj.sv");
            dto.setPuhelin("0100000210");
            dto.setAloitusPvm("11.03.1999");
            dto.setSvNimi(nimi);
            dto.setNimi(nimi);
            dto.setYritysTunnus(new YTunnusDTO() {{
                setAlkupvm("28.02.2011");
            }});
            dto.setYritysmuoto(yritysmuoto);
        } else {
            YTJOsoiteDTO osoite = new YTJOsoiteDTO();
            osoite.setKatu("Tie 1");
            osoite.setMaa("Suami");
            osoite.setMaakoodi("FI");
            osoite.setToimipaikka("Helsinki");
            osoite.setPostinumero("00100");
            osoite.setKieli(1);
            dto.setPostiOsoite(osoite);
            dto.setKayntiOsoite(osoite);
            dto.setYrityksenKieli("Suomi");
            dto.setSahkoposti("example@example.com");
            dto.setWww("http://www.ytj.fi");
            dto.setPuhelin("0100000211");
            dto.setAloitusPvm("12.04.1999");
            dto.setNimi(nimi);
            dto.setYritysTunnus(new YTunnusDTO() {{
                setAlkupvm("01.01.1999");
            }});
            dto.setYritysmuoto(yritysmuoto);
        }
        return dto;
    }

    private static final String[][] MOCK_DATA = new String[][]{
            {"2170532-5", "?", "Aatteellinen yhdistys"},
            {"1458640-1", "AIESEC-HELSINGIN YLIOPISTO RY ", "Aatteellinen yhdistys"},
            {"1940671-3", "FCG Finnish Consulting Group Oy ", "Osakeyhtiö"},
            {"0313471-7", "Helsingin Yliopisto", "Muu julkisoikeudellinen oikeushenkilö"},
            {"2267512-1", "Helsingin Yliopistokiinteistöt Oy - Helsingfors Universitets... ", "Osakeyhtiö"},
            {"0312657-3", "HELSINGIN YLIOPISTOLLISEN KESKUSSAIRAALAN HENKILÖKUNTA KTV R... ", "Aatteellinen yhdistys"},
            {"1449784-7", "HELSINGIN YLIOPISTOLLISEN KESKUSSAIRAALAN PERUSHOITAJAT RY ", "Aatteellinen yhdistys"},
            {"1087544-1", "HELSINGIN YLIOPISTOLLISEN KESKUSSAIRAALAN URHEILUSEURA- ", "Aatteellinen yhdistys"},
            {"1730639-9", "Helsingin yliopistomuseon säätiö", "Säätiö"},
            {"1773346-2", "HELSINGIN YLIOPISTON ALUMNI RY HELSINGFORS UNIVERSITETS ALUM... ", "Aatteellinen yhdistys"},
            {"2199401-6", "Helsingin yliopiston ammattiosasto JHL.ry ", "Aatteellinen yhdistys"},
            {"1469720-8", "HELSINGIN YLIOPISTON DOSENTTIYHDISTYS-DOCENTFÖRENINGEN VID H... ", "Aatteellinen yhdistys"},
            {"0835305-4", "HELSINGIN YLIOPISTON HENKILÖKUNTAYHDISTYS HYHY - PERSONALFÖR... ", "Aatteellinen yhdistys"},
            {"0681677-2", "Helsingin yliopiston Holding Oy ", "Osakeyhtiö"},
            {"2456571-9", "Helsingin yliopiston humanistisen tiedekunnan tiedekuntajärj... ", "Aatteellinen yhdistys"},
            {"0773573-9", "Helsingin yliopiston Kehityspalvelut Oy ", "Osakeyhtiö"},
            {"0281366-2", "HELSINGIN YLIOPISTON KEMISTIT R.Y. ", "Aatteellinen yhdistys"},
            {"2183469-6", "Helsingin yliopiston matemaattis-luonnontieteellisten opiske... ", "Aatteellinen yhdistys"},
            {"2003915-6", "Helsingin Yliopiston Musiikkiseura HYMS - Musiksällskapet vi... ", "Aatteellinen yhdistys"},
            {"1075005-9", "Helsingin Yliopiston Musiikkiseuran Kamarikuoroyhdistys ", "Aatteellinen yhdistys"},
            {"1466234-7", "HELSINGIN YLIOPISTON PARTIOLAISET RY, SCOUTERNA VID HELSINGF... ", "Aatteellinen yhdistys"},
            {"1706114-1", "HELSINGIN YLIOPISTON POLYMEERI- JA PUUKEMISTIT R.Y. ", "Aatteellinen yhdistys"},
            {"2026585-8", "Helsingin yliopiston pyöräilyseura Prologi ry ", "Aatteellinen yhdistys"},
            {"0246242-8", "HELSINGIN YLIOPISTON RAHASTOT", "Valtio ja sen laitokset"},
            {"1722768-2", "HELSINGIN YLIOPISTON SAVOLAINEN OSAKUNTA ", "Ylioppilaskunta tai osakunta"},
            {"1891796-8", "HELSINGIN YLIOPISTON SCIENCE FICTION KLUBI RY ", "Aatteellinen yhdistys"},
            {"1711773-1", "HELSINGIN YLIOPISTON TAEKWON-DO RY ", "Aatteellinen yhdistys"},
            {"1491703-9", "Helsingin yliopiston tiedesäätiö ", "Säätiö"},
            {"0222866-6", "HELSINGIN YLIOPISTON TIETEENTEKIJÄT - FORSKARFÖRENINGEN VID ... ", "Aatteellinen yhdistys"},
            {"1097929-1", "HELSINGIN YLIOPISTON VERKKOPALLOSEURA HYVS RY ", "Aatteellinen yhdistys"},
            {"1745903-7", "HELSINGIN YLIOPISTON YLIOPPILASKUNNAN ELOKUVARYHMÄ RY ", "Aatteellinen yhdistys"},
            {"2307545-1", "Helsingin yliopiston ylioppilaskunnan osakuntalainen unioni ", "Aatteellinen yhdistys"},
            {"0199976-8", "HELSINGIN YLIOPISTON YLIOPPILASKUNTA ", "Ylioppilaskunta tai osakunta"},
            {"1570233-9", "HELSINGIN YLIOPISTOSSA LASTENTARHANOPETTAJIKSI OPISKELEVIEN ... ", "Aatteellinen yhdistys"},
            {"1439743-2", "Helsingin Yliopitollisen Keskussairaalan Tekninen- ja Toimis... ", "Aatteellinen yhdistys"},
            {"0251003-9", "HÄMÄLÄIS-OSAKUNTA HELSINGIN YLIOPISTOSSA ", "Muu yhdistys"},
            {"2171125-8", "Kansantaloustieteen alumnit Helsingin yliopistossa ry ", "Aatteellinen yhdistys"},
            {"0571974-3", "Lylyisten virkistysalueyhdistys ry ", "Aatteellinen yhdistys"},
            {"1490443-0", "Tehy ry:n HYKS:n Meilahden alueen ammattiosasto ry ", "Aatteellinen yhdistys"},
            {"2401713-0", "TEHYn HYKSin Naistensairaalan ammattiosasto ry ", "Aatteellinen yhdistys"},
            {"2397998-7", "Vasara, Helsingin yliopiston geologinen kerho ry ", "Aatteellinen yhdistys"},
            {"2255802-1", "Katva Consulting", "Yksityinen elinkeinonharjoittaja"},
            {"1111111-1", "Diibadaa", "Yksityinen elinkeinonharjoittaja"},
            {"2222222-1", "Diibadaadaa", "Yksityinen elinkeinonharjoittaja"},
            {"1492449-0", "Ruotsalainen & koulutustoimija", "Yksityinen elinkeinonharjoittaja"},
            {"6165189-7", "Piilotustesti varhaiskasvatuksen yksityinen elinkeinonharjoittaja", "Yksityinen elinkeinonharjoittaja"}
    };
}
