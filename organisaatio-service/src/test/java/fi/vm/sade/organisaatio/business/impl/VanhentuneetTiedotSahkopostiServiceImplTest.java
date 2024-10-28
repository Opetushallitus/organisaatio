package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.client.KayttooikeusClient;
import fi.vm.sade.organisaatio.dto.VirkailijaDto;
import fi.vm.sade.organisaatio.email.EmailService;
import fi.vm.sade.organisaatio.email.QueuedEmail;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.properties.OphProperties;
import freemarker.template.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class VanhentuneetTiedotSahkopostiServiceImplTest {

    private VanhentuneetTiedotSahkopostiServiceImpl service;

    @Mock
    private KayttooikeusClient kayttooikeusClientMock;
    @Mock
    private OrganisaatioRepository organisaatioRepositoryMock;
    @Mock
    private EmailService emailServiceMock;

    private OphProperties properties = new OphProperties("/organisaatio-service-oph.properties");

    @BeforeEach
    public void setup() throws Exception {
        properties.addFiles("/application.properties");
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:Messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);

        FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean = new FreeMarkerConfigurationFactoryBean();
        freeMarkerConfigurationFactoryBean.afterPropertiesSet();
        Configuration freemarker = freeMarkerConfigurationFactoryBean.getObject();

        service = new VanhentuneetTiedotSahkopostiServiceImpl(kayttooikeusClientMock, emailServiceMock,
                organisaatioRepositoryMock, messageSource, freemarker, properties);
    }

    @Test
    public void lahetaSahkopostit() {
        when(kayttooikeusClientMock.listOrganisaatioOid(any())).thenReturn(singletonList("org1"));
        when(organisaatioRepositoryMock.findByTarkastusPvm(any(), any(), any(), anyLong()))
                .thenAnswer((InvocationOnMock invocation) -> invocation.getArgument(2, Collection.class).stream().map(oid -> {
                    Organisaatio organisaatio = new Organisaatio();
                    organisaatio.setOid((String) oid);
                    return organisaatio;
                }).toList());
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
        assertThat(svBody).contains("Granskning av uppgifter");
    }

}
