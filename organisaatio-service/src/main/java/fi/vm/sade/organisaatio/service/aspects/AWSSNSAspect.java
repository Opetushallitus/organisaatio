package fi.vm.sade.organisaatio.service.aspects;

import fi.vm.sade.organisaatio.config.AWSSNSLakkautusTopic;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class AWSSNSAspect {
    private final AWSSNSLakkautusTopic lakkautusTopic;
    private final OrganisaatioRepository organisaatioRepository;


    @Pointcut("execution(public * fi.vm.sade.organisaatio.repository.OrganisaatioRepository.saveAndFlush(..))))")
    private void saveAndFlushOrganisaatio() {
        // Pointcut for saveAndFlush
    }

    @Pointcut("execution(* fi.vm.sade.organisaatio.repository.OrganisaatioRepository.save(..))))")
    private void saveOrganisaatio() {
        // Pointcut for save
    }

    @Before("saveOrganisaatio() || saveAndFlushOrganisaatio()")
    private void publishSNSOnSaveOrganisaatio(JoinPoint jp)  {
        Object updated = jp.getArgs()[0];
        if (updated instanceof Organisaatio) {
            Organisaatio updatedOrg = (Organisaatio) updated;
            Organisaatio oldOrg = organisaatioRepository.findFirstByOid(updatedOrg.getOid());
            handleLakkautusPvmChange(updatedOrg, oldOrg);
        } else {
            log.error("SNS PULBISH FAILED for {}, {}", updated, updated.getClass().getName());
        }
    }

    private void handleLakkautusPvmChange(Organisaatio updatedOrg, Organisaatio oldOrg)  {
        if (lakkautusPvmChanged(updatedOrg, oldOrg)) {
            lakkautusTopic.pubTopic(Map.of("oid", updatedOrg.getOid()));
        }
    }

    private static boolean lakkautusPvmChanged(Organisaatio updatedOrg, Organisaatio oldOrg) {
        return oldOrg != null && !OrganisaatioUtil.isSameDay(updatedOrg.getLakkautusPvm(), oldOrg.getLakkautusPvm());
    }
}
