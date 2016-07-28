package fi.vm.sade.organisaatio.service;

import fi.vm.sade.organisaatio.business.impl.OrganisaatioYtjServiceImpl;
import fi.vm.sade.organisaatio.resource.YTJResource;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(locations = {"classpath:spring/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class YtjServiceTest {
    private YTJResource ytjResource;

    private OrganisaatioYtjServiceImpl organisaatioYtjService;

    public YtjServiceTest() {
        ytjResource = mock(YTJResource.class);
        when(ytjResource.doYtjMassSearch(anyListOf(String.class)))
                .thenAnswer(new Answer<List<YTJDTO>>() {
                    @Override
                    public List<YTJDTO> answer(InvocationOnMock invocation) throws Throwable {
                        List<YTJDTO> ytjdtoList = new ArrayList<>();
                        @SuppressWarnings("unchecked")
                        List<String> args = (List<String>) invocation.getArguments()[0];
                        for(int i = 0; i < args.size(); i++) {
                            ytjdtoList.add(null);
                        }
                        return ytjdtoList;
                    }
                });
        organisaatioYtjService = new OrganisaatioYtjServiceImpl();
        ReflectionTestUtils.setField(organisaatioYtjService, "ytjResource", ytjResource);
    }

    @Test
    public void fetchLessThan1000() {
        final int amount = 500;
        List<YTJDTO> ytjdtoList = invokeFetchDataFromYtj(amount);
        Assert.assertEquals(amount, ytjdtoList.size());
    }

    @Test
    public void fetch1000Test() {
        final int amount = 1000;
        List<YTJDTO> ytjdtoList = invokeFetchDataFromYtj(amount);
        Assert.assertEquals(amount, ytjdtoList.size());
    }

    @Test
    public void fetchMoreThan1000Test() {
        final int amount = 1500;
        List<YTJDTO> ytjdtoList = invokeFetchDataFromYtj(amount);
        Assert.assertEquals(amount, ytjdtoList.size());
    }

    private List<YTJDTO> invokeFetchDataFromYtj(int amount) {
        List<String> ytunnusMock = new ArrayList<>();
        for(int i = 0; i < amount; i++) {
            ytunnusMock.add(null);
        }
        return ReflectionTestUtils.invokeMethod(organisaatioYtjService, "fetchDataFromYtj", ytunnusMock);
    }
}
