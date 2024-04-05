package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.PrintingAnswer;
import fi.vm.sade.organisaatio.business.OrganisaatioViestinta;
import fi.vm.sade.organisaatio.client.KayttooikeusClient;
import fi.vm.sade.organisaatio.dto.VirkailijaDto;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.repository.OrganisaatioSahkopostiRepository;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.organisaatio.model.email.EmailData;
import fi.vm.sade.organisaatio.model.email.EmailRecipient;
import freemarker.template.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class VanhentuneetTiedotSahkopostiServiceImplTest {

    private VanhentuneetTiedotSahkopostiServiceImpl service;

    @Mock
    private KayttooikeusClient kayttooikeusClientMock;
    @Mock
    private OrganisaatioViestinta organisaatioViestintaMock;
    @Mock
    private OrganisaatioRepository organisaatioRepositoryMock;
    @Mock
    private OrganisaatioSahkopostiRepository organisaatioSahkopostiRepositoryMock;

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

        service = new VanhentuneetTiedotSahkopostiServiceImpl(kayttooikeusClientMock, organisaatioViestintaMock,
                organisaatioRepositoryMock, organisaatioSahkopostiRepositoryMock, messageSource, freemarker, properties);
        when(organisaatioViestintaMock.sendEmail(any(), anyBoolean())).then(new PrintingAnswer<>());
    }

    @Test
    public void lahetaSahkopostit() {
        when(kayttooikeusClientMock.listOrganisaatioOid(any())).thenReturn(singletonList("org1"));
        when(organisaatioRepositoryMock.findByTarkastusPvm(any(), any(), any(), anyLong()))
                .thenAnswer((InvocationOnMock invocation) -> invocation.getArgument(2, Collection.class).stream().map(oid -> {
                    Organisaatio organisaatio = new Organisaatio();
                    organisaatio.setOid((String) oid);
                    return organisaatio;
                }).collect(toList()));
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
        ArgumentCaptor<EmailData> emailDataArgumentCaptor = ArgumentCaptor.forClass(EmailData.class);
        verify(organisaatioViestintaMock, times(2)).sendEmail(emailDataArgumentCaptor.capture(), eq(false));
        List<EmailData> emailDatas = emailDataArgumentCaptor.getAllValues();
        assertThat(emailDatas).extracting(emailData -> emailData.getEmail().getLanguageCode(),
                emailData -> emailData.getRecipient().stream().map(EmailRecipient::getEmail).sorted().collect(toList()))
                .containsExactlyInAnyOrder(
                        tuple("fi", asList("example1@example.com", "example2@example.com", "example4@example.com")),
                        tuple("sv", singletonList("example3@example.com")));
    }

}
