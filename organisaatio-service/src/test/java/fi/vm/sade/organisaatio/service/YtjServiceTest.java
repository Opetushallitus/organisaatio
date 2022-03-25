package fi.vm.sade.organisaatio.service;

import fi.vm.sade.organisaatio.business.impl.OrganisaatioYtjServiceImpl;
import fi.vm.sade.organisaatio.resource.YTJResource;
import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YtjServiceTest {

    private YTJResource ytjResource;

    private OrganisaatioYtjServiceImpl organisaatioYtjService;

    public YtjServiceTest() {
        ytjResource = mock(YTJResource.class);
        when(ytjResource.doYtjMassSearch(anyList()))
                .thenAnswer(new Answer<List<YTJDTO>>() {
                    @Override
                    public List<YTJDTO> answer(InvocationOnMock invocation) {

                        @SuppressWarnings("unchecked")
                        List<String> ytunnusList = (List<String>) invocation.getArguments()[0];
                        return ytunnusList.stream().map(ytunnus -> new YTJDTO() {{
                            setYtunnus(ytunnus);
                        }}).collect(toList());
                    }
                });
        organisaatioYtjService = new OrganisaatioYtjServiceImpl();
        ReflectionTestUtils.setField(organisaatioYtjService, "ytjResource", ytjResource);
    }

    private static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of("fetchLessThan1000", 500),
                Arguments.of("fetch1000Test", 1000),
                Arguments.of("fetchMoreThan1000Test", 1500)
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void fetchLessThan1000(String message, int amount) {
        List<YTJDTO> ytjdtoList = invokeFetchDataFromYtj(amount);
        assertEquals(amount, ytjdtoList.size(), message);
    }


    private List<YTJDTO> invokeFetchDataFromYtj(int amount) {
        List<String> ytunnusMock = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            ytunnusMock.add("2769790-1");
        }
        return ReflectionTestUtils.invokeMethod(organisaatioYtjService, "fetchDataFromYtj", ytunnusMock);
    }
}
