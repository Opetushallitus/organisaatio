package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.config.scheduling.FetchKoodistotTask;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(name = "feature.mockapi")
@Hidden
@RestController
@RequestMapping({"/mock/init"})
@RequiredArgsConstructor
@Slf4j
public class MockInitResource {
    private final FetchKoodistotTask fetchKoodistotTask;

    @PostMapping
    @Transactional
    public void createTestData() {
        log.info("Importing koodistos");
        fetchKoodistotTask.execute();
    }
}

