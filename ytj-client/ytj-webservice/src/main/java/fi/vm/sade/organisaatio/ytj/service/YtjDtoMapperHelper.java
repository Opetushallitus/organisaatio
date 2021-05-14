package fi.vm.sade.organisaatio.ytj.service;

import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJOsoiteDTO;
import fi.ytj.YrityksenOsoiteV2DTO;
import fi.ytj.YrityksenYhteystietoDTO;
import fi.ytj.YritysHakuDTO;
import fi.ytj.YritysTiedotV2DTO;
import java.util.List;
import java.util.stream.Collectors;

public class YtjDtoMapperHelper {

    public static final String KIELI_SV = "Svenska";
    private static final String YHTEYSTIETOLAJI_PUHELIN = "1";
    private static final String YHTEYSTIETOLAJI_FAKSI = "2";
    private static final String YHTEYSTIETOLAJI_EMAIL = "3";
    private static final String YHTEYSTIETOLAJI_WWW = "4";
    private static final String YHTEYSTIETOLAJI_MATKAPUHELIN = "5";


    public YTJDTO mapYritysTiedotV2DTOtoYTJDTO(YritysTiedotV2DTO vastaus) {
        YTJDTO ytj = new YTJDTO();
        if (vastaus.getYrityksenKieli() != null
                && vastaus.getYrityksenKieli().getSeloste() != null
                && vastaus.getYrityksenKieli().getSeloste().equalsIgnoreCase(KIELI_SV)) {
            ytj.setSvNimi(vastaus.getToiminimi().getToiminimi());
        } else {
            ytj.setNimi(vastaus.getToiminimi().getToiminimi() != null
                    ? vastaus.getToiminimi().getToiminimi()
                    : (vastaus.getYrityksenHenkilo() != null ? vastaus.getYrityksenHenkilo().getNimi() : null));
        }
        ytj.setYtunnus(vastaus.getYritysTunnus().getYTunnus());
        ytj.setKotiPaikkaKoodi(vastaus.getKotipaikka() != null ? vastaus.getKotipaikka().getKoodi() : null);
        ytj.setPostiOsoite(vastaus.getYrityksenPostiOsoite() != null ? mapYtjOsoite(vastaus.getYrityksenPostiOsoite()) : null);
        ytj.setKotiPaikka(vastaus.getKotipaikka() != null ? vastaus.getKotipaikka().getSeloste() : null);
        if (vastaus.getToiminimi() != null ) {
            ytj.setAloitusPvm(vastaus.getToiminimi().getAlkuPvm());
        }
        if (vastaus.getYrityksenKieli() != null) {
            ytj.setYrityksenKieli(vastaus.getYrityksenKieli().getSeloste() != null ? vastaus.getYrityksenKieli().getSeloste() : "");
        }
        ytj.setKayntiOsoite(
                vastaus.getYrityksenKayntiOsoite() != null ?  mapYtjOsoite(vastaus.getYrityksenKayntiOsoite()) : null);
        mapYhteysTiedot(vastaus, ytj);
        mapYritysmuotoAndToimiala(vastaus, ytj);
        ytj.setYritysTunnus(vastaus.getYritysTunnus());
        ytj.setYritystunnusHistoria(vastaus.getYritystunnusHistoria());
        return ytj;
    }

    private YTJOsoiteDTO mapYtjOsoite(YrityksenOsoiteV2DTO osoiteParam) {
        YTJOsoiteDTO osoite = null;
        if (osoiteParam != null) {
            osoite = new YTJOsoiteDTO();
            osoite.setKieli(osoiteParam.getKieli());
            osoite.setKatu(getKatuOsoite(osoiteParam));
            osoite.setPostinumero(osoiteParam.getPostinumero());
            osoite.setToimipaikka(osoiteParam.getToimipaikka());
            osoite.setMaa(osoiteParam.getMaa());
            osoite.setMaakoodi(osoiteParam.getMaakoodi());
        }
        return osoite;
    }

    private void mapYhteysTiedot(YritysTiedotV2DTO yritysParam, YTJDTO yritys) {
        if (yritysParam.getYrityksenYhteystiedot() != null) {
            for (YrityksenYhteystietoDTO yhtTieto : yritysParam.getYrityksenYhteystiedot().getYrityksenYhteystietoDTO()) {
                switch (yhtTieto.getLaji().trim()) {
                    case YHTEYSTIETOLAJI_PUHELIN:
                    case YHTEYSTIETOLAJI_MATKAPUHELIN: yritys.setPuhelin(yhtTieto.getYhteysTieto()); break;
                    case YHTEYSTIETOLAJI_FAKSI: yritys.setFaksi(yhtTieto.getYhteysTieto()); break;
                    case YHTEYSTIETOLAJI_EMAIL: yritys.setSahkoposti(yhtTieto.getYhteysTieto()); break;
                    case YHTEYSTIETOLAJI_WWW: yritys.setWww(yhtTieto.getYhteysTieto()); break;
                    default: /* nop */
                }
            }
        }
    }

    private void mapYritysmuotoAndToimiala(YritysTiedotV2DTO yritysParam, YTJDTO yritys) {
        if (yritysParam.getYritysmuoto() != null) {
            yritys.setYritysmuoto(yritysParam.getYritysmuoto().getSeloste());
            yritys.setYritysmuotoKoodi(yritysParam.getYritysmuoto().getKoodi());
        }
        if (yritysParam.getToimiala() != null) {
            yritys.setToimiala(yritysParam.getToimiala().getSeloste());
            yritys.setToimialaKoodi(yritysParam.getToimiala().getKoodi());
        }
    }

    private String getKatuOsoite(YrityksenOsoiteV2DTO osoiteParam) {
        if (osoiteParam.getKatu() != null
                && osoiteParam.getKatu().trim().length() > 0
                || osoiteParam.getUlkomaanosoite() != null
                && osoiteParam.getUlkomaanosoite().trim().length() > 0) {
            String kokoKatuOsoite = String.join(" ",
                    osoiteParam.getKatu(),
                    (osoiteParam.getTalo() != null ? osoiteParam.getTalo() : ""),
                    (osoiteParam.getPorras() != null ? osoiteParam.getPorras() : ""),
                    (osoiteParam.getHuoneisto() != null ? osoiteParam.getHuoneisto() : ""));

            if (kokoKatuOsoite.trim().length() < 1) {
                kokoKatuOsoite = osoiteParam.getUlkomaanosoite();
            }

            return kokoKatuOsoite;
        } else if (osoiteParam.getPostilokero() != null && osoiteParam.getPostilokero().trim().length() > 0) {
            return "PL " + osoiteParam.getPostilokero();
        } else {
            return null;
        }
    }

    public List<YTJDTO> mapYritysHakuDTOListToDtoList(List<YritysHakuDTO> vastaukset) {
        if (vastaukset != null) {
            return vastaukset.stream().map(this::mapYritysHakuDTOToDto).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public YTJDTO mapYritysHakuDTOToDto(YritysHakuDTO ytjParam) {
        YTJDTO dto = null;
        if (ytjParam != null) {
            dto = new YTJDTO();
            dto.setNimi(ytjParam.getYritysnimi());
            dto.setYtunnus(ytjParam.getYTunnus());
        }
        return dto;
    }
}
