package fi.vm.sade.rekisterointi.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Html5PathsController {

  @RequestMapping(value = "/**/{[path:[^.]*}")
  public String redirect() {
    return "forward:/index.html";
  }
}
