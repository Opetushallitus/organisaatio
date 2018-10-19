package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.VarhaiskasvatuksenKielipainotus;
import fi.vm.sade.organisaatio.model.VarhaiskasvatuksenToimintamuoto;
import fi.vm.sade.organisaatio.model.VarhaiskasvatuksenToimipaikkaTiedot;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.ValidationException;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrganisaatioValidationServiceImplTest {
    private OrganisaatioValidationServiceImpl organisaatioValidationService;

    @Before
    public void setup() {
        this.organisaatioValidationService = new OrganisaatioValidationServiceImpl("root", new OrganisaatioKoodistoMock());
    }

    @Test
    public void isNotSet() {
        Organisaatio organisaatio = new Organisaatio();
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.null");
    }

    @Test
    public void paikkojenLkmNull() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.paikkojenlkm.null");
    }

    @Test
    public void jarjestamismuotoNull() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.jarjestamismuoto.invalidkoodi");
    }

    @Test
    public void jarjestamismuotoInvalidKoodi() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        varhaiskasvatuksenToimipaikkaTiedot.setJarjestamismuoto("invalid");
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.jarjestamismuoto.invalidkoodi");
    }

    @Test
    public void jarjestelmaNull() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        varhaiskasvatuksenToimipaikkaTiedot.setJarjestamismuoto("vardajarjestamismuoto_jm03");
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.jarjestelma.invalidkoodi");
    }

    @Test
    public void jarjestelmaInvalidKoodi() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        varhaiskasvatuksenToimipaikkaTiedot.setJarjestamismuoto("vardajarjestamismuoto_jm03");
        varhaiskasvatuksenToimipaikkaTiedot.setKasvatusopillinenJarjestelma("invalid");
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.jarjestelma.invalidkoodi");
    }

    @Test
    public void painotusNull() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        varhaiskasvatuksenToimipaikkaTiedot.setJarjestamismuoto("vardajarjestamismuoto_jm03");
        varhaiskasvatuksenToimipaikkaTiedot.setKasvatusopillinenJarjestelma("vardakasvatusopillinenjarjestelma_kj99");
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.painotus.invalidkoodi");
    }

    @Test
    public void painotusInvalidKoodi() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        varhaiskasvatuksenToimipaikkaTiedot.setJarjestamismuoto("vardajarjestamismuoto_jm03");
        varhaiskasvatuksenToimipaikkaTiedot.setKasvatusopillinenJarjestelma("vardakasvatusopillinenjarjestelma_kj99");
        varhaiskasvatuksenToimipaikkaTiedot.setToiminnallinenPainotus("invalid");
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.painotus.invalidkoodi");
    }

    @Test
    public void toimintamuodotNull() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        varhaiskasvatuksenToimipaikkaTiedot.setJarjestamismuoto("vardajarjestamismuoto_jm03");
        varhaiskasvatuksenToimipaikkaTiedot.setKasvatusopillinenJarjestelma("vardakasvatusopillinenjarjestelma_kj99");
        varhaiskasvatuksenToimipaikkaTiedot.setToiminnallinenPainotus("vardatoiminnallinenpainotus_tp99");
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenToimintamuodot(null);
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.toimintamuodot.null");
    }

    @Test
    public void toimintamuodotEmpty() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        varhaiskasvatuksenToimipaikkaTiedot.setJarjestamismuoto("vardajarjestamismuoto_jm03");
        varhaiskasvatuksenToimipaikkaTiedot.setKasvatusopillinenJarjestelma("vardakasvatusopillinenjarjestelma_kj99");
        varhaiskasvatuksenToimipaikkaTiedot.setToiminnallinenPainotus("vardatoiminnallinenpainotus_tp99");
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenToimintamuodot(new HashSet<>());
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.toimintamuodot.empty");
    }

    @Test
    public void toimintamuodotInvalidKoodi() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        varhaiskasvatuksenToimipaikkaTiedot.setJarjestamismuoto("vardajarjestamismuoto_jm03");
        varhaiskasvatuksenToimipaikkaTiedot.setKasvatusopillinenJarjestelma("vardakasvatusopillinenjarjestelma_kj99");
        varhaiskasvatuksenToimipaikkaTiedot.setToiminnallinenPainotus("vardatoiminnallinenpainotus_tp99");
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenToimintamuodot(Collections.singleton(new VarhaiskasvatuksenToimintamuoto()));
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.toimintamuodot.invalidkoodi");
    }

    @Test
    public void kielipainotuksetNull() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        varhaiskasvatuksenToimipaikkaTiedot.setJarjestamismuoto("vardajarjestamismuoto_jm03");
        varhaiskasvatuksenToimipaikkaTiedot.setKasvatusopillinenJarjestelma("vardakasvatusopillinenjarjestelma_kj99");
        varhaiskasvatuksenToimipaikkaTiedot.setToiminnallinenPainotus("vardatoiminnallinenpainotus_tp99");
        VarhaiskasvatuksenToimintamuoto varhaiskasvatuksenToimintamuoto = new VarhaiskasvatuksenToimintamuoto();
        varhaiskasvatuksenToimintamuoto.setToimintamuoto("vardatoimintamuoto_tm02");
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenToimintamuodot(Collections.singleton(varhaiskasvatuksenToimintamuoto));
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenKielipainotukset(null);
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.kielipainotukset.null");
    }

    @Test
    public void kielipainotuksetEmpty() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        varhaiskasvatuksenToimipaikkaTiedot.setJarjestamismuoto("vardajarjestamismuoto_jm03");
        varhaiskasvatuksenToimipaikkaTiedot.setKasvatusopillinenJarjestelma("vardakasvatusopillinenjarjestelma_kj99");
        varhaiskasvatuksenToimipaikkaTiedot.setToiminnallinenPainotus("vardatoiminnallinenpainotus_tp99");
        VarhaiskasvatuksenToimintamuoto varhaiskasvatuksenToimintamuoto = new VarhaiskasvatuksenToimintamuoto();
        varhaiskasvatuksenToimintamuoto.setToimintamuoto("vardatoimintamuoto_tm02");
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenToimintamuodot(Collections.singleton(varhaiskasvatuksenToimintamuoto));
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenKielipainotukset(new HashSet<>());
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.kielipainotukset.empty");
    }

    @Test
    public void allOk() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        varhaiskasvatuksenToimipaikkaTiedot.setJarjestamismuoto("vardajarjestamismuoto_jm03");
        varhaiskasvatuksenToimipaikkaTiedot.setKasvatusopillinenJarjestelma("vardakasvatusopillinenjarjestelma_kj99");
        varhaiskasvatuksenToimipaikkaTiedot.setToiminnallinenPainotus("vardatoiminnallinenpainotus_tp99");
        VarhaiskasvatuksenToimintamuoto varhaiskasvatuksenToimintamuoto = new VarhaiskasvatuksenToimintamuoto();
        varhaiskasvatuksenToimintamuoto.setToimintamuoto("vardatoimintamuoto_tm02");
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenToimintamuodot(Collections.singleton(varhaiskasvatuksenToimintamuoto));
        VarhaiskasvatuksenKielipainotus varhaiskasvatuksenKielipainotus = new VarhaiskasvatuksenKielipainotus();
        varhaiskasvatuksenKielipainotus.setKielipainotus("kieli_bh");
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenKielipainotukset(Collections.singleton(varhaiskasvatuksenKielipainotus));
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        assertThatCode(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .doesNotThrowAnyException();
    }

}
