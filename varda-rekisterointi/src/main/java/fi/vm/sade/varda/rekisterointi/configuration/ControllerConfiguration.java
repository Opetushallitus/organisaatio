package fi.vm.sade.varda.rekisterointi.configuration;

import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class ControllerConfiguration {

    @InitBinder
    private void configure(DataBinder dataBinder) {
        dataBinder.initDirectFieldAccess();
    }

}
