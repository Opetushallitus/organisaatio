package fi.vm.sade.rajapinnat.ytj.api;

import fi.ytj.ArrayOfYritysTunnusHistoriaDTO;
import fi.ytj.YTunnusDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static java.util.Objects.requireNonNull;

public class YTJDTOBuilder {

    private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final String ytunnus;
    private YTJKieli kieli;
    private String nimiFi;
    private String nimiSv;
    private YTunnusDTO yritysTunnus;
    private ArrayOfYritysTunnusHistoriaDTO yritysTunnusHistoria;
    private String yritysmuotoKoodi;
    private String yritysmuotoNimi;
    private String toimialaKoodi;
    private String toimialaNimi;
    private YTJOsoiteDTO postiosoite;
    private YTJOsoiteDTO kayntiosoite;
    private String sahkoposti;
    private String www;
    private String puhelin;
    private String kotipaikkaKoodi;
    private String kotipaikkaNimi;
    private LocalDate aloitusPvm;

    public YTJDTOBuilder(final String ytunnus) {
        this.ytunnus = requireNonNull(ytunnus);
        this.yritysTunnus = new YTunnusDTO() {{
            setYTunnus(ytunnus);
        }};
        this.yritysTunnusHistoria = new ArrayOfYritysTunnusHistoriaDTO() {{
            setYritysTunnusHistoriaDTO(new ArrayList<>());
        }};
    }

    public YTJDTOBuilder kieli(YTJKieli kieli) {
        this.kieli = kieli;
        return this;
    }

    public YTJDTOBuilder nimi(String fi, String sv) {
        this.nimiFi = fi;
        this.nimiSv = sv;
        return this;
    }

    public YTJDTOBuilder yritysmuoto(String koodi, String nimi) {
        this.yritysmuotoNimi = nimi;
        this.yritysmuotoKoodi = koodi;
        return this;
    }

    public YTJDTOBuilder toimiala(String koodi, String nimi) {
        this.toimialaKoodi = koodi;
        this.toimialaNimi = nimi;
        return this;
    }

    public YTJDTOBuilder kayntiosoite(YTJOsoiteDTO kayntiosoite) {
        this.kayntiosoite = kayntiosoite;
        return this;
    }

    public YTJDTOBuilder postiosoite(YTJOsoiteDTO postiosoite) {
        this.postiosoite = postiosoite;
        return this;
    }

    public YTJDTOBuilder sahkoposti(String sahkoposti) {
        this.sahkoposti = sahkoposti;
        return this;
    }

    public YTJDTOBuilder www(String www) {
        this.www = www;
        return this;
    }

    public YTJDTOBuilder puhelin(String puhelin) {
        this.puhelin = puhelin;
        return this;
    }

    public YTJDTOBuilder kotipaikka(String koodi, String nimi) {
        this.kotipaikkaKoodi = koodi;
        this.kotipaikkaNimi = nimi;
        return this;
    }

    public YTJDTOBuilder aloitusPvm(LocalDate aloitusPvm) {
        this.aloitusPvm = aloitusPvm;
        return this;
    }

    public YTJDTO build() {
        YTJDTO dto = new YTJDTO();
        dto.setNimi(nimiFi);
        dto.setSvNimi(nimiSv);
        dto.setYtunnus(ytunnus);
        dto.setYritysTunnus(yritysTunnus);
        dto.setYritystunnusHistoria(yritysTunnusHistoria);
        dto.setYritysmuoto(yritysmuotoNimi);
        dto.setYritysmuotoKoodi(yritysmuotoKoodi);
        dto.setToimiala(toimialaNimi);
        dto.setToimialaKoodi(toimialaKoodi);
        dto.setYrityksenKieli(kieliToYrityksenKieli(kieli));
        dto.setKayntiOsoite(kayntiosoite);
        dto.setPostiOsoite(postiosoite);
        dto.setSahkoposti(sahkoposti);
        dto.setWww(www);
        dto.setPuhelin(puhelin);
        dto.setKotiPaikka(kotipaikkaNimi);
        dto.setKotiPaikkaKoodi(kotipaikkaKoodi);
        if (aloitusPvm != null) {
            dto.setAloitusPvm(aloitusPvm.format(DATE_FORMATTER));
        }
        return dto;
    }

    private static String kieliToYrityksenKieli(YTJKieli kieli) {
        if (kieli == null) {
            return null;
        }
        switch (kieli) {
            case FI:
                return "Suomi";
            case SV:
                return "Svenska";
            case EN:
                return "English";
            default:
                throw new IllegalArgumentException("Tuntematon kieli: " + kieli);
        }
    }

}
