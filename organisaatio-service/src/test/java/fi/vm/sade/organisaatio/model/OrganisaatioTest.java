package fi.vm.sade.organisaatio.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrganisaatioTest {

    @Test
    void parentOidsWithNull() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOids(null);

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isNull();
    }

    @Test
    void parentOidsWithEmpty() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOids(Collections.emptyList());

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isNull();
    }

    @Test
    void parentOidWithValidPath() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOids(Arrays.asList(
                "1.2.246.562.10.86638002385", "1.2.246.562.10.81269623245", "1.2.246.562.10.00000000001"));

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isEqualTo("1.2.246.562.10.86638002385");
    }

    @Test
    void parentOidWithValidRootPath() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOids(Collections.singletonList("1.2.246.562.10.00000000001"));

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isEqualTo("1.2.246.562.10.00000000001");
    }

    @Test
    void testSortYhteysTietoByKieli(){
        Set<Yhteystieto> yhteystietos= new HashSet<>();
        Yhteystieto fi = new Yhteystieto();
        fi.setKieli("kieli_fi");
        Yhteystieto sv = new Yhteystieto();
        sv.setKieli("kieli_sv");
        Yhteystieto en = new Yhteystieto();
        en.setKieli("kieli_en");
        yhteystietos.add(en);
        assertThat(yhteystietos.stream().min(Organisaatio::sortYhteysTietoByKieli).orElse(null).getKieli()).isEqualTo("kieli_en");
        assertThat(yhteystietos.stream().max(Organisaatio::sortYhteysTietoByKieli).orElse(null).getKieli()).isEqualTo("kieli_en");
        yhteystietos.add(sv);
        assertThat(yhteystietos.stream().min(Organisaatio::sortYhteysTietoByKieli).orElse(null).getKieli()).isEqualTo("kieli_sv");
        assertThat(yhteystietos.stream().max(Organisaatio::sortYhteysTietoByKieli).orElse(null).getKieli()).isEqualTo("kieli_en");
        yhteystietos.add(fi);
        assertThat(yhteystietos.stream().min(Organisaatio::sortYhteysTietoByKieli).orElse(null).getKieli()).isEqualTo("kieli_fi");
        assertThat(yhteystietos.stream().max(Organisaatio::sortYhteysTietoByKieli).orElse(null).getKieli()).isEqualTo("kieli_en");
    }


}
