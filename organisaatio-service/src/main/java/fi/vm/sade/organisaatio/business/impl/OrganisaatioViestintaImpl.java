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
import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;
import fi.vm.sade.organisaatio.model.YtjVirhe;
import fi.vm.sade.viestintapalvelu.api.message.MessageData;
import fi.vm.sade.viestintapalvelu.api.message.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrganisaatioViestintaImpl implements OrganisaatioViestinta {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final Gson gson;

    @Value("$viestintapalvelu.email")
    private String email;

    @Value("$host.virkailija")
    private String hostUri;

    @Autowired
    private OrganisaatioViestintaClient viestintaClient;

    public OrganisaatioViestintaImpl() {
        gson = new GsonBuilder().create();
    }

    private OrganisaatioViestintaClient getClient() {
        if (viestintaClient == null) {
            viestintaClient = new OrganisaatioViestintaClient();
        }
//        viestintaClient.setReauthorize(reauthorize);
        return viestintaClient;
    }

    @Override
    public void sendPaivitysLokiViestintaEmail(YtjPaivitysLoki ytjPaivitysLoki) {
        String msgContent = "YTJ-Tietojen haku " + ytjPaivitysLoki.getPaivitysaika().toString() + " ";
        if(ytjPaivitysLoki.getPaivitysTila().equals("ok")) {
            msgContent += "onnistui";
            if(!ytjPaivitysLoki.getYtjVirheet().isEmpty()) {
                msgContent += ", " + Integer.toString(ytjPaivitysLoki.getYtjVirheet().size()) + " virheellistä";
            }
            msgContent += "\r\n";
        }
        else {
            msgContent+= "epäonnistui (" + ytjPaivitysLoki.getPaivitysTila() + ")\r\n";

        }

        for(YtjVirhe ytjVirhe : ytjPaivitysLoki.getYtjVirheet()) {
            msgContent += "<a href=\"https://" + hostUri + "/organisaatio-ui/html/index.html#/organisaatiot/"
                    + ytjVirhe.getOid() +"\">" + ytjVirhe.getOrgNimi() + "</a>\r\n";
        }

        msgContent += "\r\n<a href=\"https://" + hostUri
                + "/organisaatio-ui/html/index.html#/organisaatiot/ilmoitukset\">YTJ-päivitykset</a>";

        sendStringViestintaEmail(msgContent);
    }

    @Override
    public void sendStringViestintaEmail(String msgContent) {
        sendEmail(msgContent, new ArrayList<String>(){{add(email);}});
    }

    @Override
    public void sendEmail(String msgContent, List<String> receiverEmails) {
        String templateName = "osoitepalvelu_email";
        List<Receiver> receiverList = new ArrayList<>();
        Map<String, Object> commonReplacements = new HashMap<>();
        for(String receiverEmail : receiverEmails) {
            Receiver receiver = new Receiver(null, receiverEmail, null, null, null);
            receiverList.add(receiver);
        }
        commonReplacements.put("sisalto", msgContent);
        MessageData messageData = new MessageData(templateName, receiverList, commonReplacements);
        String json = gson.toJson(messageData);
        try {
            getClient().post(json, "messagesendMessageViaAsiointiTiliOrEmail");
        } catch (OrganisaatioViestintaException ve) {
            LOG.error("Could not send email.");
        }
    }
}
