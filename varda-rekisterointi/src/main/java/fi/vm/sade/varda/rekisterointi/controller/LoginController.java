package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.User;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.nio.charset.Charset;

@Controller
@Scope("session")
public class LoginController {

    private final OphProperties properties;
    private final User user;

    public LoginController(OphProperties properties, User user) {
        this.properties = properties;
        this.user = user;
    }

    @GetMapping("/initsession")
    public View login(@RequestHeader("nationalidentificationnumber") String nationalIdentificationNumber,
                      @RequestHeader("firstname") String givenName,
                      @RequestHeader("sn") String surname) {
        Charset iso8859 = Charset.forName("ISO-8859-1");
        Charset utf8 = Charset.forName("UTF-8");

        user.nationalIdentificationNumber = nationalIdentificationNumber;
        user.givenName = new String(givenName.getBytes(iso8859), utf8);
        user.surname = new String(surname.getBytes(iso8859), utf8);

        String redirectUrl = properties.url("varda-rekisterointi.index");
        return new RedirectView(redirectUrl);
    }

}
