package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.client.KayttooikeusClient;
import fi.vm.sade.organisaatio.dto.VirkailijaDto;
import fi.vm.sade.organisaatio.email.EmailService;
import fi.vm.sade.organisaatio.email.QueuedEmail;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import freemarker.template.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VanhentuneetTiedotSahkopostiServiceImplTest {

    private VanhentuneetTiedotSahkopostiServiceImpl service;

    @Mock
    private KayttooikeusClient kayttooikeusClientMock;
    @Mock
    private OrganisaatioRepository organisaatioRepositoryMock;
    @Mock
    private EmailService emailServiceMock;

    @BeforeEach
    public void setup() throws Exception {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:Messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);

        FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean = new FreeMarkerConfigurationFactoryBean();
        freeMarkerConfigurationFactoryBean.afterPropertiesSet();
        Configuration freemarker = freeMarkerConfigurationFactoryBean.getObject();

        service = new VanhentuneetTiedotSahkopostiServiceImpl(kayttooikeusClientMock, emailServiceMock,
                organisaatioRepositoryMock, messageSource, freemarker);
    }

    @Test
    public void lahetaSahkopostit() {
        when(kayttooikeusClientMock.listOrganisaatioOid(any())).thenReturn(singletonList("org1"));
        when(organisaatioRepositoryMock.findByTarkastusPvm(any(), any(), any(), anyLong()))
                .thenAnswer((InvocationOnMock invocation) -> {
                    Collection<String> oids = invocation.getArgument(2);
                    return oids.stream().map(oid -> {
                        Organisaatio organisaatio = new Organisaatio();
                        organisaatio.setOid(oid);
                        return organisaatio;
                    }).toList();
                });
        VirkailijaDto virkailija1 = new VirkailijaDto();
        virkailija1.setSahkoposti("example1@example.com");
        VirkailijaDto virkailija2 = new VirkailijaDto();
        virkailija2.setAsiointikieli("fi");
        virkailija2.setSahkoposti("example2@example.com");
        VirkailijaDto virkailija3 = new VirkailijaDto();
        virkailija3.setAsiointikieli("sv");
        virkailija3.setSahkoposti("example3@example.com");
        VirkailijaDto virkailija4 = new VirkailijaDto();
        virkailija4.setAsiointikieli("ei_tuettu_kieli");
        virkailija4.setSahkoposti("example4@example.com");
        VirkailijaDto virkailija5 = new VirkailijaDto();
        virkailija5.setSahkoposti(null);
        when(kayttooikeusClientMock.listVirkailija(any()))
                .thenReturn(asList(virkailija1, virkailija2, virkailija3, virkailija4, virkailija5));

        service.lahetaSahkopostit();

        verify(organisaatioRepositoryMock).findByTarkastusPvm(any(), any(), eq(singletonList("org1")), anyLong());
        ArgumentCaptor<QueuedEmail> emailDataArgumentCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(emailServiceMock, times(2)).queueEmail(emailDataArgumentCaptor.capture());
        List<QueuedEmail> emailDatas = emailDataArgumentCaptor.getAllValues();
        assertThat(emailDatas).extracting(emailData -> emailData.getRecipients())
                .containsExactlyInAnyOrder(
                        asList("example1@example.com", "example2@example.com", "example4@example.com"),
                        singletonList("example3@example.com"));

        String svBody = emailDatas.stream()
                .filter(e -> e.getRecipients().get(0).equals("example3@example.com"))
                .findFirst()
                .orElseThrow()
                .getBody();
        assertThat(svBody).contains("""
<!doctype html>
<html lang="sv">
<head>
    <meta charset="utf-8">
    <title>Kontrollera organisationens kontaktinformation</title>
    <style>
        body {
            background: #F6F4F0;
        }
        .box {
            background: #FFFFFF;
            padding: 1em 2em;
            margin: 2em 4em;
        }
    </style>
</head>
<body>
<div class="box">
    <p>
        Kontrollera att kontaktuppgifterna för er organisation och de tillhörande läroanstalterna,
        inklusive e‑postadresser, är uppdaterade.
    </p>
    <p>
        E‑postadressen finns i Studieinfo för administratörer (<a href="null/organisaatio-service/lomake/org1">null/organisaatio-service/lomake/org1</a>)
        under <b>Organisationens uppgifter → Kontaktinformation → Delad e-post</b>.
    </p>
    <p>
        Den ansvariga användaren för Studieinfo upprätthåller läroanstalternas kontaktuppgifter.
    </p>
    <p>
        Utbildningsstyrelsen och Undervisnings- och kulturministeriet använder organisationernas
        e‑postadresser i officiell kommunikation.
    </p>
    <p>
        E‑postadressen används också:
    </p>
    <ul>
        <li>KOSKI och Varda föreskrift</li>
        <li>Move-mätningsresultaten</li>
        <li>kriskommunikation</li>
    </ul>
    <p>
        Utbildningsstyrelsen
    </p>
</div>
<div class="box" style="text-align: right;">
    <img src="http://www.oph.fi/themes/custom/ophfi/logo.svg" height="48" alt="Utbildningsstyrelsen" />
</div>
</body>
</html>""");
    }

}
