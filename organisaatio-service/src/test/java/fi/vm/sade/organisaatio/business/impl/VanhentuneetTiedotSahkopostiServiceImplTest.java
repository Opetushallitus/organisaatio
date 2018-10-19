package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.PrintingAnswer;
import fi.vm.sade.organisaatio.business.OrganisaatioViestinta;
import fi.vm.sade.organisaatio.config.FreemarkerConfiguration;
import fi.vm.sade.organisaatio.config.UrlConfiguration;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dto.VirkailijaDto;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailRecipient;
import freemarker.template.Configuration;
import static java.util.Arrays.asList;
import java.util.Collection;
import static java.util.Collections.singletonList;
import java.util.List;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

@RunWith(MockitoJUnitRunner.class)
public class VanhentuneetTiedotSahkopostiServiceImplTest {

    private VanhentuneetTiedotSahkopostiServiceImpl service;

    @Mock
    private KayttooikeusClient kayttooikeusClientMock;
    @Mock
    private OrganisaatioViestinta organisaatioViestintaMock;
    @Mock
    private OrganisaatioDAO organisaatioDAOMock;

    @Before
    public void setup() throws Exception {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:Messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);

        FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean = new FreemarkerConfiguration().freeMarkerConfigurationFactoryBean();
        freeMarkerConfigurationFactoryBean.afterPropertiesSet();
        Configuration freemarker = freeMarkerConfigurationFactoryBean.getObject();

        UrlConfiguration properties = new UrlConfiguration();
        service = new VanhentuneetTiedotSahkopostiServiceImpl(kayttooikeusClientMock, organisaatioViestintaMock,
                organisaatioDAOMock, messageSource, freemarker, properties);
        when(organisaatioViestintaMock.sendEmail(any())).then(new PrintingAnswer<>());
    }

    @Test
    public void lahetaSahkopostit() {
        when(kayttooikeusClientMock.listOrganisaatioOid(any())).thenReturn(singletonList("org1"));
        when(organisaatioDAOMock.findOidByTarkastusPvm(any(), any(), any(), anyLong()))
                .thenAnswer((InvocationOnMock invocation) -> invocation.getArgumentAt(2, Collection.class));
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

        verify(organisaatioDAOMock).findOidByTarkastusPvm(any(), any(), eq(singletonList("org1")), anyLong());
        ArgumentCaptor<EmailData> emailDataArgumentCaptor = ArgumentCaptor.forClass(EmailData.class);
        verify(organisaatioViestintaMock, times(2)).sendEmail(emailDataArgumentCaptor.capture());
        List<EmailData> emailDatas = emailDataArgumentCaptor.getAllValues();
        assertThat(emailDatas).extracting(emailData -> emailData.getEmail().getLanguageCode(),
                emailData -> emailData.getRecipient().stream().map(EmailRecipient::getEmail).sorted().collect(toList()))
                .containsExactlyInAnyOrder(
                        tuple("fi", asList("example1@example.com", "example2@example.com", "example4@example.com")),
                        tuple("sv", singletonList("example3@example.com")));
    }

}
