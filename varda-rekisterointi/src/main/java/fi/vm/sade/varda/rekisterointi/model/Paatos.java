package fi.vm.sade.varda.rekisterointi.model;

import org.springframework.data.annotation.Id;

import java.sql.Timestamp;

public class Paatos {
    @Id
    public Long id;
    public boolean hyvaksytty;
    public Timestamp paatetty;
    public Kayttaja paattaja;
    public String perustelu;
}
