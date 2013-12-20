package fi.vm.sade.organisaatio.service.aspects;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fi.vm.sade.organisaatio.service.AbstractOrganisaatioBusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.types.GenericFaultInfoType;

/**
 *
 * @author Tuomas Katva
 */
@Aspect
public class OrganisaatioServiceExceptionWrapperAspect {

    protected static final Logger LOGGER = LoggerFactory.getLogger(OrganisaatioServiceExceptionWrapperAspect.class);

    @Pointcut("within(fi.vm.sade.organisaatio.service.OrganisaatioServiceImpl)")
    public void serviceMethod() {
    }
    
    @SuppressWarnings("rawtypes")
    @Around("serviceMethod()")
    public Object wrapException(ProceedingJoinPoint pjp) throws GenericFault {

        try {
            return pjp.proceed();
        } catch (Throwable throwedException) {
            LOGGER.error("Organisaatio exception wrapped ", throwedException);
            
            MethodSignature sigu = (MethodSignature) pjp.getSignature();
            Class[] types = sigu.getExceptionTypes();
            Set<Class> classSet = new HashSet<Class>(Arrays.asList(types));
            if (classSet.contains(GenericFault.class)) {
            String key = "";
            if (AbstractOrganisaatioBusinessException.class.isAssignableFrom(throwedException.getClass())) {
                key = ((AbstractOrganisaatioBusinessException) throwedException).getKey();
            } else {
                key = throwedException.getClass().getName();
            }
            GenericFaultInfoType info = new GenericFaultInfoType();

            info.setErrorCode(key);
            info.setExplanation(throwedException.getMessage());

            GenericFault fault = new GenericFault(throwedException.getMessage(), info);
            throw fault;
            } else {
                
                LOGGER.error("Unhandled exception : " + throwedException.getClass() + " - " + throwedException.getMessage(),throwedException);
                throw new RuntimeException("Unhandled exception : " + throwedException.getClass() + " - " + throwedException.getMessage(),throwedException); 
            }
        }

    } 
}
