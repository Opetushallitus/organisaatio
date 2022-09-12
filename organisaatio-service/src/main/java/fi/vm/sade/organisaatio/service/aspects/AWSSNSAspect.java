package fi.vm.sade.organisaatio.service.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.organisaatio.config.AWSSNSLakkautusTopic;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class AWSSNSAspect {
    private final AWSSNSLakkautusTopic lakkautusTopic;
    private final OrganisaatioRepository organisaatioRepository;

    @Before("execution(public * fi.vm.sade.organisaatio.business.OrganisaatioBusinessService.saveOrUpdate(..))")
    private void updateOrgAdvice(JoinPoint jp) throws Throwable {
        Object updated = jp.getArgs()[0];
        if (updated instanceof OrganisaatioRDTOV4) {
            OrganisaatioRDTOV4 updatedOrg = (OrganisaatioRDTOV4) updated;
            Organisaatio oldOrg = organisaatioRepository.customFindByOid(updatedOrg.getOid());
            if (!OrganisaatioUtil.isSameDay(updatedOrg.getLakkautusPvm(), oldOrg.getLakkautusPvm())) {
                String message =new ObjectMapper().writeValueAsString(Map.of("oid", updatedOrg.getOid()));
                lakkautusTopic.pubTopic(message);
            }
        } else {
            log.error("SNS PULBISH FAILED for {}, {}", updated, updated.getClass().getName());
        }
    }
}
