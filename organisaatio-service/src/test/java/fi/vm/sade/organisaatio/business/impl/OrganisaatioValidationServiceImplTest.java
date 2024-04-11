package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.validation.ValidationException;
import java.util.Date;
import java.util.HashSet;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

public class OrganisaatioValidationServiceImplTest {
    private OrganisaatioValidationServiceImpl organisaatioValidationService;

    @BeforeEach
    public void setup() {
        this.organisaatioValidationService = new OrganisaatioValidationServiceImpl("root", new OrganisaatioKoodistoMock());
    }

    @Test
    public void nimiOk() {
        Organisaatio organisaatio = new Organisaatio();
        MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.setValues(singletonMap("fi", "nimi suomeksi"));
        organisaatio.setNimi(nimi);

        Throwable throwable = catchThrowable(() -> organisaatioValidationService.validateOrganisation(organisaatio, null, null));

        assertThat(throwable).isNull();
    }

    @Test
    public void nimiTuntematonKieli() {
        Organisaatio organisaatio = new Organisaatio();
        MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.setValues(singletonMap("", "nimi tuntemattomalla kielellä"));
        organisaatio.setNimi(nimi);

        Throwable throwable = catchThrowable(() -> organisaatioValidationService.validateOrganisation(organisaatio, null, null));

        assertThat(throwable).isInstanceOf(ValidationException.class).hasMessage("validation.Organisaatio.nimi.kieli");
    }

    @Test
    public void nimetOk() {
        Organisaatio organisaatio = new Organisaatio();
        MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.setValues(singletonMap("fi", "nimi suomeksi"));
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.setNimi(nimi);
        organisaatio.setNimet(singletonList(organisaatioNimi));

        Throwable throwable = catchThrowable(() -> organisaatioValidationService.validateOrganisation(organisaatio, null, null));

        assertThat(throwable).isNull();
    }

    @Test
    public void nimetTuntematonKieli() {
        Organisaatio organisaatio = new Organisaatio();
        MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.setValues(singletonMap("", "nimi tuntemattomalla kielellä"));
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.setNimi(nimi);
        organisaatio.setNimet(singletonList(organisaatioNimi));

        Throwable throwable = catchThrowable(() -> organisaatioValidationService.validateOrganisation(organisaatio, null, null));

        assertThat(throwable).isInstanceOf(ValidationException.class).hasMessage("validation.Organisaatio.nimet.kieli");
    }

    @Test
    public void kuvausOk() {
        Organisaatio organisaatio = new Organisaatio();
        MonikielinenTeksti kuvaus = new MonikielinenTeksti();
        kuvaus.setValues(singletonMap("kieli_fi#1", "kuvaus suomeksi"));
        organisaatio.setKuvaus2(kuvaus);

        Throwable throwable = catchThrowable(() -> organisaatioValidationService.validateOrganisation(organisaatio, null, null));

        assertThat(throwable).isNull();
    }

    @Test
    public void kuvausTuntematonKieli() {
        Organisaatio organisaatio = new Organisaatio();
        MonikielinenTeksti kuvaus = new MonikielinenTeksti();
        kuvaus.setValues(singletonMap("", "kuvaus tuntemattomalla kielellä"));
        organisaatio.setKuvaus2(kuvaus);

        Throwable throwable = catchThrowable(() -> organisaatioValidationService.validateOrganisation(organisaatio, null, null));

        assertThat(throwable).isInstanceOf(ValidationException.class).hasMessage("validation.Organisaatio.kuvaus2.kieli");
    }

    @Test
    public void postiosoiteOk() {
        Organisaatio organisaatio = new Organisaatio();
        Osoite postiosoite = new Osoite();
        postiosoite.setKieli("kieli_fi#1");
        postiosoite.setOsoiteTyyppi(Osoite.TYYPPI_POSTIOSOITE);
        postiosoite.setOsoite("korvatunturi");
        postiosoite.setPostinumero("99999");
        organisaatio.setYhteystiedot(singleton(postiosoite));

        Throwable throwable = catchThrowable(() -> organisaatioValidationService.validateOrganisation(organisaatio, null, null));

        assertThat(throwable).isNull();
    }

    @Test
    public void postiosoiteTuntematonKieli() {
        Organisaatio organisaatio = new Organisaatio();
        Osoite postiosoite = new Osoite();
        postiosoite.setKieli("kieli_dk#1");
        postiosoite.setOsoiteTyyppi(Osoite.TYYPPI_POSTIOSOITE);
        organisaatio.setYhteystiedot(singleton(postiosoite));

        Throwable throwable = catchThrowable(() -> organisaatioValidationService.validateOrganisation(organisaatio, null, null));

        assertThat(throwable).isInstanceOf(ValidationException.class).hasMessage("validation.Organisaatio.yhteystiedot.kieli");
    }

    @Test
    public void yhteystietoArvoOk() {
        Organisaatio organisaatio = new Organisaatio();
        YhteystietoArvo yhteystietoArvo = new YhteystietoArvo();
        yhteystietoArvo.setKieli("kieli_fi#1");
        organisaatio.setYhteystietoArvos(singleton(yhteystietoArvo));

        Throwable throwable = catchThrowable(() -> organisaatioValidationService.validateOrganisation(organisaatio, null, null));

        assertThat(throwable).isNull();
    }

    @Test
    public void yhteystietoArvoTuntematonKieli() {
        Organisaatio organisaatio = new Organisaatio();
        YhteystietoArvo yhteystietoArvo = new YhteystietoArvo();
        yhteystietoArvo.setKieli("kieli_dk#1");
        organisaatio.setYhteystietoArvos(singleton(yhteystietoArvo));

        Throwable throwable = catchThrowable(() -> organisaatioValidationService.validateOrganisation(organisaatio, null, null));

        assertThat(throwable).isInstanceOf(ValidationException.class).hasMessage("validation.Organisaatio.yhteystietoArvos.kieli");
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
        assertThatCode(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .doesNotThrowAnyException();
    }

    @Test
    public void painotusEmpty() {
        Organisaatio organisaatio = this.createValidOrganisation();
        organisaatio.getVarhaiskasvatuksenToimipaikkaTiedot().setVarhaiskasvatuksenToiminnallinenpainotukset(new HashSet<>());
        assertThatCode(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .doesNotThrowAnyException();
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
        assertThatCode(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .doesNotThrowAnyException();
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
        assertThatCode(() -> ReflectionTestUtils.invokeMethod(organisaatioValidationService, "validateVarhaiskasvatuksenToimipaikkaTiedot", organisaatio))
                .doesNotThrowAnyException();
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
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenJarjestamismuodot(singleton("vardajarjestamismuoto_jm03"));
        VarhaiskasvatuksenToiminnallinenpainotus varhaiskasvatuksenToiminnallinenpainotus = new VarhaiskasvatuksenToiminnallinenpainotus();
        varhaiskasvatuksenToiminnallinenpainotus.setAlkupvm(new Date(1000000));
        varhaiskasvatuksenToiminnallinenpainotus.setLoppupvm(new Date());
        varhaiskasvatuksenToiminnallinenpainotus.setToiminnallinenpainotus("vardatoiminnallinenpainotus_tp99");
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenToiminnallinenpainotukset(singleton(varhaiskasvatuksenToiminnallinenpainotus));
        VarhaiskasvatuksenKielipainotus varhaiskasvatuksenKielipainotus = new VarhaiskasvatuksenKielipainotus();
        varhaiskasvatuksenKielipainotus.setAlkupvm(new Date(1000000));
        varhaiskasvatuksenKielipainotus.setLoppupvm(new Date());
        varhaiskasvatuksenKielipainotus.setKielipainotus("kieli_bh");
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenKielipainotukset(singleton(varhaiskasvatuksenKielipainotus));
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedot);
        return organisaatio;
    }

}
