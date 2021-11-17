package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.HealtCheck;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${server.service.context-path}/")
public class HealtCheckImpl implements HealtCheck {

    @Override
    public String hello() {
        return String.valueOf(System.currentTimeMillis());
    }
}
