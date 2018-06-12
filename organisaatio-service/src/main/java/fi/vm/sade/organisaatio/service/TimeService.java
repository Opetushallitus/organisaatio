package fi.vm.sade.organisaatio.service;

import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class TimeService {

    public Date getNow() {
        return new Date();
    }

}
