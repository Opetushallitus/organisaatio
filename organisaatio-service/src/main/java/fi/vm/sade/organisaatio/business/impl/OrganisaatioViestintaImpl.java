/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 */

package fi.vm.sade.organisaatio.business.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.vm.sade.organisaatio.business.OrganisaatioViestinta;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioViestintaException;
import fi.vm.sade.organisaatio.client.OrganisaatioViestintaClient;
import fi.vm.sade.organisaatio.dto.ViestintaSahkopostiDto;
import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;
import fi.vm.sade.organisaatio.model.YtjVirhe;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.organisaatio.model.email.EmailData;
import fi.vm.sade.organisaatio.model.email.EmailMessage;
import fi.vm.sade.organisaatio.model.email.EmailRecipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OrganisaatioViestintaImpl implements OrganisaatioViestinta {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final Gson gson;

    @Value("${ryhmasahkoposti.service.email}")
    private String email;

    @Autowired
    private OphProperties properties;

    private OrganisaatioViestintaClient viestintaClient;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    public OrganisaatioViestintaImpl(OrganisaatioViestintaClient viestintaClient) {
        this.viestintaClient = viestintaClient;
        gson = new GsonBuilder().create();
    }

    private OrganisaatioViestintaClient getClient() {
        return viestintaClient;
    }

    @Override
    public String sendEmail(EmailData data, boolean sanitize) {
        return gson.fromJson(viestintaClient.sendEmail(gson.toJson(data), sanitize), ViestintaSahkopostiDto.class).getId();
    }

    @Override
    public void sendPaivitysLokiViestintaEmail(YtjPaivitysLoki ytjPaivitysLoki) {
        if(ytjPaivitysLoki != null) {
            String msgContent = generateMessageFromPaivitysloki(ytjPaivitysLoki);

            sendStringViestintaEmail(msgContent);
        }
        else {
            LOG.error("Null ytjPaivitysLoki. Could not send email.");
        }
    }

    private String generateMessageFromPaivitysloki(YtjPaivitysLoki ytjPaivitysLoki) {
        String time = "";
        Map<String, List<YtjVirhe>> virheMap = YtjPaivitysLoki.getYtjVirheetMapByOid(ytjPaivitysLoki.getYtjVirheet());
        if(ytjPaivitysLoki.getPaivitysaika() != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy 'klo' HH.mm");
            time = simpleDateFormat.format(ytjPaivitysLoki.getPaivitysaika());
        }
        else {
            time = "(aikaa ei asetettu)";
        }
        String msgContent = "YTJ-Tietojen haku " + time + " ";
        if(ytjPaivitysLoki.getPaivitysTila().equals(YtjPaivitysLoki.YTJPaivitysStatus.EPAONNISTUNUT)) {
            msgContent+= "epäonnistui (" + ytjPaivitysLoki.getPaivitysTilaSelite() + ")<br>";
        }
        else {
            msgContent += "onnistui";
            if(!ytjPaivitysLoki.getYtjVirheet().isEmpty()) {
                msgContent += ", " + Integer.toString(virheMap.size()) + " virheellistä";
            }
            msgContent += "<br>";

        }

        for(Map.Entry<String, List<YtjVirhe>> entry : virheMap.entrySet()) {
            String oid = entry.getKey();
            List<YtjVirhe> ytjVirheList = entry.getValue();

            msgContent += "<a href=\"https://" + properties.getProperty("organisaatio.ui.url") + "/"
                    + oid +"\">" + ytjVirheList.get(0).getOrgNimi() + "</a> (";
            String separator = "";
            for(YtjVirhe ytjVirhe : ytjVirheList) {
            msgContent += separator + getMessage(ytjVirhe.getVirheviesti());
                separator = ", ";
            }
            msgContent += ")<br>";
        }

        msgContent += "<br><a href=\"https://" + properties.getProperty("organisaatio.ui.ilmoitukset.url")
                 + "\">YTJ-päivitykset</a>";
        return msgContent;
    }

    void sendStringViestintaEmail(String msgContent) {
        if(msgContent == null || msgContent.isEmpty()) {
            LOG.error("Null or empty string. Could not send email.");
        }
        else {
            generateAndSendEmail(msgContent, new ArrayList<String>(){{add(email);}});
        }
    }

    void generateAndSendEmail(String msgContent, List<String> receiverEmails) {
        List<EmailRecipient> receiverList = new ArrayList<>();
        for(String receiverEmail : receiverEmails) {
            EmailRecipient receiver = new EmailRecipient(null, receiverEmail);
            receiverList.add(receiver);
        }
        EmailMessage emailMessage = new EmailMessage("Organisaatio-service", null, null, "YTJ-paivitys info", msgContent);
        EmailData messageData = new EmailData(receiverList, emailMessage);
        String json = gson.toJson(messageData);
        try {
            getClient().sendEmail(json);
        } catch (OrganisaatioViestintaException ve) {
            LOG.error("Could not send email. {} {}", ve, json);
        }
    }

    // pikaratkaisuna Messages.properties-tiedostosta.
    // Jos halutaan hakea lokalisointipalvelusta, voi katsoa mallia toteutukselle esim e-perusteet LokalisointiService
    private String getMessage(String key) {
        return messageSource.getMessage(key, null, new Locale("fi", "FI"));
    }
}
