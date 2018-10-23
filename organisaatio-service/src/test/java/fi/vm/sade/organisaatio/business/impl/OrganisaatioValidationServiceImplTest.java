package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.VarhaiskasvatuksenKielipainotus;
import fi.vm.sade.organisaatio.model.VarhaiskasvatuksenToiminnallinenpainotus;
import fi.vm.sade.organisaatio.model.VarhaiskasvatuksenToimipaikkaTiedot;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.ValidationException;
import java.util.Collections;
import java.util.Date;
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
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().setPaikkojenLukumaara(null);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.paikkojenlkm.null");
    }

    @Test
    public void jarjestamismuotoNull() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().setVarhaiskasvatuksenJarjestamismuodot(null);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.jarjestamismuodot.null");
    }

    @Test
    public void toimintamuotoInvalidKoodi() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().setToimintamuoto("invalid");
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.toimintamuoto.invalidkoodi");
    }

    @Test
    public void kasvatusopillinenjarjestelmaNull() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().setKasvatusopillinenJarjestelma(null);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.jarjestelma.invalidkoodi");
    }

    @Test
    public void kasvatusopillinenjarjestelmaInvalidKoodi() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().setKasvatusopillinenJarjestelma("invalid");
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.jarjestelma.invalidkoodi");
    }

    @Test
    public void painotusNull() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().setVarhaiskasvatuksenToiminnallinenpainotukset(null);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.toiminnallinenpainotus.null");
    }

    @Test
    public void toiminnallinenpainotusAlkupvmNull() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().getVarhaiskasvatuksenToiminnallinenpainotukset()
                .forEach(painotus -> painotus.setAlkupvm(null));
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.toiminnallinenpainotus.alkupvm.null");
    }

    @Test
    public void toiminnallinenpainotusLoppupvmInvalid() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().getVarhaiskasvatuksenToiminnallinenpainotukset()
                .forEach(painotus -> painotus.setLoppupvm(new Date(100)));
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.toiminnallinenpainotus.loppupvm.invalid");
    }

    @Test
    public void toiminnallinenpainotusInvalidKoodi() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().getVarhaiskasvatuksenToiminnallinenpainotukset()
                .forEach(painotus -> painotus.setToiminnallinenpainotus("invalid"));
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.toiminnallinenpainotus.invalidkoodi");
    }

    @Test
    public void toimintamuodotNull() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().setToimintamuoto(null);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.toimintamuoto.null");
    }

    @Test
    public void jarjestamismuodotEmpty() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().setVarhaiskasvatuksenJarjestamismuodot(new HashSet<>());
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.jarjestamismuodot.empty");
    }

    @Test
    public void toimintamuodotInvalidKoodi() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().setToimintamuoto("invalid");
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.toimintamuoto.invalidkoodi");
    }

    @Test
    public void kielipainotuksetNull() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().setVarhaiskasvatuksenKielipainotukset(null);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.kielipainotukset.null");
    }

    @Test
    public void kielipainotuksetAlkupvmNull() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().getVarhaiskasvatuksenKielipainotukset()
                .forEach(painotus -> painotus.setAlkupvm(null));
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.kielipainotukset.alkupvm.null");
    }

    @Test
    public void kielipainotuksetLoppupvmInvalid() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().getVarhaiskasvatuksenKielipainotukset()
                .forEach(painotus -> painotus.setLoppupvm(new Date(100)));
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.kielipainotukset.loppupvm.invalid");
    }

    @Test
    public void kielipainotuksetEmpty() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().setVarhaiskasvatuksenKielipainotukset(new HashSet<>());
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .isInstanceOf(ValidationException.class).hasMessage("validation.varhaiskasvatuksentoimipaikka.kielipainotukset.empty");
    }

    @Test
    public void allOk() {
        Organisaatio organisaatio = this.createValidOrganisation();
        assertThatCode(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .doesNotThrowAnyException();
    }

    private Organisaatio createValidOrganisation() {
        Organisaatio organisaatio = new Organisaatio();
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(1L);
        varhaiskasvatuksenToimipaikkaTiedot.setToimintamuoto("vardatoimintamuoto_tm02");
        varhaiskasvatuksenToimipaikkaTiedot.setKasvatusopillinenJarjestelma("vardakasvatusopillinenjarjestelma_kj99");
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenJarjestamismuodot(Collections.singleton("vardajarjestamismuoto_jm03"));
        VarhaiskasvatuksenToiminnallinenpainotus varhaiskasvatuksenToiminnallinenpainotus = new VarhaiskasvatuksenToiminnallinenpainotus();
        varhaiskasvatuksenToiminnallinenpainotus.setAlkupvm(new Date(1000000));
        varhaiskasvatuksenToiminnallinenpainotus.setLoppupvm(new Date());
        varhaiskasvatuksenToiminnallinenpainotus.setToiminnallinenpainotus("vardatoiminnallinenpainotus_tp99");
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenToiminnallinenpainotukset(Collections.singleton(varhaiskasvatuksenToiminnallinenpainotus));
        VarhaiskasvatuksenKielipainotus varhaiskasvatuksenKielipainotus = new VarhaiskasvatuksenKielipainotus();
        varhaiskasvatuksenKielipainotus.setAlkupvm(new Date(1000000));
        varhaiskasvatuksenKielipainotus.setLoppupvm(new Date());
        varhaiskasvatuksenKielipainotus.setKielipainotus("kieli_bh");
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenKielipainotukset(Collections.singleton(varhaiskasvatuksenKielipainotus));
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        return organisaatio;
    }

}
