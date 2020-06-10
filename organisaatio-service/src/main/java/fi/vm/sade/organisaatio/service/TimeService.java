package fi.vm.sade.organisaatio.service;

import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TimeService {

    public Date getNow() {
        return new Date();
    }

}
