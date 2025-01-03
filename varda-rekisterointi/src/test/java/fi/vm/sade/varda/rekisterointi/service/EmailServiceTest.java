package fi.vm.sade.varda.rekisterointi.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import fi.vm.sade.varda.rekisterointi.client.KayttooikeusClient;
import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.model.Kasittelyssa;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioDto;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.model.VirkailijaDto;
import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import fi.vm.sade.varda.rekisterointi.service.QueueingEmailService.QueuedEmail;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailServiceTest {
    @MockitoBean
    private QueueingEmailService queueingEmailService;

    @MockitoBean
    private RekisterointiRepository rekisterointiRepository;

    @MockitoBean
    private OrganisaatioClient organisaatioClient;

    @MockitoBean
    private KayttooikeusClient kayttooikeusClient;

    @Autowired
    private EmailService emailService;

    @Test
    public void lahetaRekisterointiEmail() {
        given(queueingEmailService.queueEmail(any())).willReturn("id");
        given(rekisterointiRepository.findById(eq(1l))).willReturn(Optional.of(RekisterointiServiceTest.getRekisterointi()));

        emailService.lahetaRekisterointiEmail(1l);

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(2)).queueEmail(emailCaptor.capture());
        List<QueuedEmail> emails = emailCaptor.getAllValues();
        assertThat(emails.get(0).getRecipients()).containsExactlyInAnyOrder("testi.sahkoposti@foo.bar");
        assertThat(emails.get(1).getRecipients()).containsExactlyInAnyOrder("testi.kayttaja@foo.bar");
        assertThat(emails.get(0).getBody()).isEqualToIgnoringWhitespace("""
<!doctype html>
<html>
<head>

    <title>Organisaation rekisteröityminen Vardaa varten</title>

</head>
<body>
<div>
    <div>
        <span>Hei</span>,<br>
        <span>yksityinen varhaiskasvatuksen palveluntuottaja (fi) on rekisteröitynyt Vardaa varten. Ennen kuin organisaatiosi pääsee tallentamaan muita Vardan vaatimia tietoja, kunta hyväksyy tai hylkää rekisteröitymisen. Saat sähköpostin, kun kunta on käsitellyt rekisteröitymisen Vardaa varten.</span>
        <p>Lisätietoja saat tarvittaessa kunnan varhaiskasvatuspalveluista.</p>
        <p>Vardaa varten tehdyn rekisteröinnin etenemisestä tiedottamiseen nimetyn henkilön sähköpostiosoite on tallennettu  Opetushallituksen Viestintäpalveluun. Henkilötiedot on saatu organisaation puolesta asioivalta henkilöltä, joka on tehnyt rekisteröitymisen Vardaa varten. Henkilötietoja käsitellään Vardaan rekisteröitymiseen liittyen. Henkilötietoja säilytetään Opetushallituksessa, kunnes yhteyttä henkilöön ei rekisteröitymiseen liittyen tarvitse ottaa. Henkilötietoja käsittelevät Opetushallituksessa ne henkilöt, jotka työskentelevät varhaiskasvatuksen tietovarannon (Varda) parissa. Henkilötietojen käsittely perustuu lakisääteiseen tehtävään (varhaiskasvatuksen tietovaranto, varhaiskasvatuslaki (540/2018)).</p>
        <span>Terveisin</span><br/>
        <span>Vardan asiakaspalvelu</span><br/>
        <span>Opetushallitus</span>
        <div>***</div>
    </div>
    <div>
        <span>Hej</span>,<br>
        <span>den privata serviceproducenten inom småbarnspedagogik (fi) har registrerat sig för att ta i bruk Varda. Innan din organisation kan föra in de uppgifter som krävs i Varda ska kommunen godkänna eller avslå registreringen. Du får ett e-postmeddelande då kommunen har behandlat registreringen.</span>
        <p>Kontakta vid behov kommunens tjänster inom småbarnspedagogik för ytterligare information.</p>
        <p>E-postadressen till den person som får information om handläggningen av registreringen har sparats i Utbildningsstyrelens Kommunikationstjänst. Personuppgifterna har anmälts av personen som uträttar ärenden på organisationens vägnar och som har gjort registreringen inför ibruktagandet av Varda. Personuppgifterna används i anslutning till registreringen. Personuppgifterna sparas hos Utbildningsstyrelsen tills det inte längre är nödvändigt att kontakta personen angående registreringen. Vid Utbildningsstyrelsens hanteras personuppgifterna endast av de personer som arbetar med informationsresursen inom småbarnspedagogiken (Varda). Hanteringen av personuppgifterna grundar sig på en lagstadgad uppgift (informationsresursen inom småbarnspedagogiken, lagen om småbarnspedagogik (540/2018)).</p>
        <span>Hälsningar</span><br/>
        <span>Vardas kundtjänst</span><br/>
        <span>Utbildningsstyrelsen</span>

    </div>
</div>
</body>
</html>""");
    }

    @Test
    public void lahetaHyvaksyttyPaatosEmailTest() {
        given(queueingEmailService.queueEmail(any())).willReturn("id");
        Rekisterointi rekisterointi = RekisterointiServiceTest.getRekisterointi().withPaatos(
                new Paatos(true, LocalDateTime.now(), "Pate Paattaja", "yeah!"));
        given(rekisterointiRepository.findById(eq(1l))).willReturn(Optional.of(rekisterointi));

        emailService.lahetaPaatosEmail(1l);

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(1)).queueEmail(emailCaptor.capture());
        QueuedEmail email = emailCaptor.getValue();
        assertThat(email.getRecipients()).containsExactlyInAnyOrder("testi.sahkoposti@foo.bar");
        assertThat(email.getBody()).isEqualToIgnoringWhitespace("""
<!doctype html>
<html>
<head>

    <title>Organisaation rekisteröityminen Vardaa varten on hyväksytty</title>

</head>
<body>
<div>
    <div>
        <span>Hei</span>,<br>
        <span>kunta on hyväksynyt yksityisen varhaiskasvatuksen palveluntuottajasi (fi) rekisteröitymisen Vardaa varten.</span>
        <p>Varda-pääkäyttäjäksi nimetty henkilö on saanut sähköpostitse kutsun luoda Vardaan tunnukset. Kun Vardan vaatimat tunnukset ja käyttöoikeudet ovat kunnossa, tietosisältöjen tallentaminen Vardaan voi alkaa.</p>
        <span>Terveisin</span><br/>
        <span>Vardan asiakaspalvelu</span><br/>
        <span>Opetushallitus</span>
        <div>***</div>
    </div>
    <div>
        <span>Hej</span>,<br>
        <span>kommunen har godkänt den privata serviceproducentens (fi) registrering för att ta i bruk Varda.</span>
        <p>Personens som angetts som Varda-administratör får per e-post en kallelse att skapa ett användarnamn till Varda. Då organisationen har de användarnamn och användarrättigheter som behövs kan man börja föra in uppgifter i Varda.</p>
        <span>Hälsningar</span><br/>
        <span>Vardas kundtjänst</span><br/>
        <span>Utbildningsstyrelsen</span>

    </div>
</div>
</body>
</html>""");
    }

    @Test
    public void lahetaHylattyPaatosEmailTest() {
        given(queueingEmailService.queueEmail(any())).willReturn("id");
        Rekisterointi rekisterointi = RekisterointiServiceTest.getRekisterointi().withPaatos(
                new Paatos(false, LocalDateTime.now(), "Hartsa Hylkaaja", "nopers!"));
        given(rekisterointiRepository.findById(eq(1l))).willReturn(Optional.of(rekisterointi));

        emailService.lahetaPaatosEmail(1l);

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(1)).queueEmail(emailCaptor.capture());
        QueuedEmail email = emailCaptor.getValue();
        assertThat(email.getRecipients()).containsExactlyInAnyOrder("testi.sahkoposti@foo.bar");
        assertThat(email.getBody()).isEqualToIgnoringWhitespace("""
<!doctype html>
<html>
<head>

    <title>Organisaation rekisteröityminen Vardaa varten on hylätty</title>

</head>
<body>
<div>
    <div>
        <span>Hei</span>,<br>
        <span>kunta on hylännyt yksityisen varhaiskasvatuksen palveluntuottajasi (fi) rekisteröitymisen Vardaa varten. Käsittelijän perustelu:</span>
        <span>nopers!</span>
        <p>Lisätietoja saat tarvittaessa kunnasta.</p>
        <span>Terveisin</span><br/>
        <span>Vardan asiakaspalvelu</span><br/>
        <span>Opetushallitus</span>
        <div>***</div>
    </div>
    <div>
        <span>Hej</span>,<br>
        <span>kommunen har avslagit den privata serviceproducentens (fi) registrering för att ta i bruk Varda. Handläggarens motivering:</span>
        <span>nopers!</span>
        <p>Ytterligare information ges vid behov av kommunen.</p>
        <span>Hälsningar</span><br/>
        <span>Vardas kundtjänst</span><br/>
        <span>Utbildningsstyrelsen</span>

    </div>
</div>
</body>
</html>""");
    }

    @Test
    public void lahetaKuntaEmailTest() {
        given(queueingEmailService.queueEmail(any())).willReturn("id");
        Rekisterointi rekisterointi = RekisterointiServiceTest.getRekisterointi();
        given(rekisterointiRepository.findByRegistrationTypeAndTila(any(), eq(Rekisterointi.Tila.KASITTELYSSA.toString())))
                .willReturn(List.of(rekisterointi));
        OrganisaatioDto organisaatio = OrganisaatioDto.of("0-0", "Firma");
        given(organisaatioClient.listBy(any())).willReturn(List.of(organisaatio));
        VirkailijaDto virkailija1 = new VirkailijaDto("1.2.3", "fi", "virkalija@foo.bar"),
                      virkailija2 = new VirkailijaDto("1.2.4", "sv", "tjansteman@foo.bar");
        given(kayttooikeusClient.listVirkailijaBy(any())).willReturn(List.of(virkailija1, virkailija2));

        emailService.lahetaKuntaEmail();

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(2)).queueEmail(emailCaptor.capture());
        List<QueuedEmail> emails = emailCaptor.getAllValues();
        assertThat(emails.get(0).getRecipients()).containsExactlyInAnyOrder("virkalija@foo.bar");
        assertThat(emails.get(0).getBody()).isEqualToIgnoringWhitespace("""
<!doctype html>
<html lang="fi">
<head>

    <title>Yksityisiä varhaiskasvatuksen palveluntuottajia rekisteröitynyt Vardaa varten – käsittele tiedot</title>

</head>
<body>
<div>
    <span>Hei</span>,<br>
    <span>yksityisiä varhaiskasvatuksen palveluntuottajia on rekisteröitynyt Vardaa varten. <strong>Käsittele kunkin yksityisen varhaiskasvatuksen palveluntuottajan rekisteröimät tiedot mahdollisimman pian.</strong></span>
    <p>Käsittelyä odottavia organisaatioita on yhteensä 1 kpl. Yksityinen varhaiskasvatuksen palveluntuottaja voi tallentaa tietoja Vardaan vasta, kun kunta on hyväksynyt rekisteröitymisen Vardaa varten. Hyväksy rekisteröityminen, mikäli tiedot ovat kunnossa. Mikäli tiedoissa on puutteita tai ristiriitaisuuksia, hylkää rekisteröityminen. Tarkemmat ohjeet käsittelyyn löytyvät täältä: https://wiki.eduuni.fi/pages/viewpage.action?pageId=190613018</p>
</div>
<span>Terveisin</span><br/>
<span>Vardan asiakaspalvelu</span><br/>
<span>Opetushallitus</span>
</body>
</html>""");
        assertThat(emails.get(1).getRecipients()).containsExactlyInAnyOrder("tjansteman@foo.bar");
        assertThat(emails.get(1).getBody()).isEqualToIgnoringWhitespace("""
<!doctype html>
<html lang="sv">
<head>

    <title>Privata serviceproducenter inom småbarnspedagogik har registrerat sig för att ta i bruk Varda – behandla uppgifterna</title>

</head>
<body>
<div>
    <span>Hej</span>,<br>
    <span>privata serviceproducenter inom småbarnspedagogik har registrerat sig för att ta i bruk Varda. Behandla uppgifterna som respektive serviceproducent registrerat så snart som möjligt.</span>
    <p>Sammanlagt 1 organisationers registreringar väntar på handläggning. Den privata serviceproducenten inom småbarnspedagogiken kan börja föra in uppgifter i Varda först då kommunen har godkänt registreringen inför Varda. Godkänn registreringen om uppgifterna är korrekta. Om uppgifterna är bristfälliga eller felaktiga, avslå registreringen. Noggrannare anvisningar för handläggningen av registeringen hittar du här: https://wiki.eduuni.fi/pages/viewpage.action?pageId=190613018</p>
</div>
<span>Hälsningar</span><br/>
<span>Vardas kundtjänst</span><br/>
<span>Utbildningsstyrelsen</span>
</body>
</html>""");
    }

    @Test
    public void lahetaKasittelyssaEmailsTest() {
        given(queueingEmailService.queueEmail(any())).willReturn("id");
        given(rekisterointiRepository.findNonVardaKasittelyssa())
                .willReturn(List.of(new Kasittelyssa("jotpa", 4)));

        emailService.lahetaKasittelyssaEmails();

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(1)).queueEmail(emailCaptor.capture());
        QueuedEmail email = emailCaptor.getValue();
        assertThat(email.getRecipients()).containsExactlyInAnyOrder(EmailService.EMAILS_FOR_REGISTRATION_TYPES.get("jotpa"));
        assertThat(email.getBody()).isEqualToIgnoringWhitespace("""
<!doctype html>
<html lang="fi">
<head>

    <title>Organisaatioita on rekisteröitynyt Opintopolun palveluita varten – käsittele tiedot</title>

</head>
<body>
<div>
    <span>Hei</span>,<br>
    <span>organisaatioita on rekisteröitynyt Opintopolun palveluita varten. Käsittele kunkin organisaation rekisteröimät tiedot mahdollisimman pian.</span>
    <p>Käsittelyä odottavia organisaatioita on yhteensä 4 kpl. Organisaatio voi tallentaa tietoja vasta, kun virkailija on hyväksynyt rekisteröitymisen. Hyväksy rekisteröityminen, mikäli tiedot ovat kunnossa. Mikäli tiedoissa on puutteita tai ristiriitaisuuksia, hylkää rekisteröityminen.</p>
</div>
<span>Ystävällisin terveisin,</span><br/>
<span>Opetushallitus</span>
</body>
</html>""");
    }

    @Test
    public void lahetaOngelmaRaporttiTest() {
        given(queueingEmailService.queueEmail(any())).willReturn("id");
        given(rekisterointiRepository.findById(any())).willReturn(Optional.of(RekisterointiServiceTest.getRekisterointi()));

        emailService.lahetaOngelmaRaportti(Set.of(
                new TaskMonitoringService.TaskFailure(TaskMonitoringService.MonitoredTaskType.KUTSU_KAYTTAJA, 1L),
                new TaskMonitoringService.TaskFailure(TaskMonitoringService.MonitoredTaskType.LUO_TAI_PAIVITA_ORGANISAATIO, 2L)));

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(1)).queueEmail(emailCaptor.capture());
        QueuedEmail email = emailCaptor.getValue();
        assertThat(email.getRecipients()).containsExactlyInAnyOrder(EmailService.FAILED_TASKS_EMAIL_ADDRESS);
        assertThat(email.getBody()).contains("<li>LUO_TAI_PAIVITA_ORGANISAATIO, y-tunnus 0000000-0</li>");
        assertThat(email.getBody()).contains("<li>KUTSU_KAYTTAJA, y-tunnus 0000000-0</li>");
    }
}
