package fi.vm.sade.rekisterointi.model;

import java.util.*;

public class OrganisaatioCriteria {

  public boolean aktiiviset;
  public boolean suunnitellut;
  public boolean lakkautetut;
  public List<String> yritysmuoto;
  public List<String> kunta;

  public Map<String, Object> asMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("aktiiviset", aktiiviset);
    map.put("suunnitellut", suunnitellut);
    map.put("lakkautetut", lakkautetut);
    Optional.ofNullable(yritysmuoto).ifPresent(value -> map.put("yritysmuoto", value));
    Optional.ofNullable(kunta).ifPresent(value -> map.put("kunta", value));
    return map;
  }

}
