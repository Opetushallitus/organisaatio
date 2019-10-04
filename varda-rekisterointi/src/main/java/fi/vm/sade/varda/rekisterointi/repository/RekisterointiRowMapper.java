package fi.vm.sade.varda.rekisterointi.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.varda.rekisterointi.model.Kayttaja;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.springframework.jdbc.core.RowMapper;

import java.io.IOException;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RekisterointiRowMapper implements RowMapper<Rekisterointi> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(); // injektio ei mahdollista? :(

    @Override
    public Rekisterointi mapRow(ResultSet resultSet, int i) throws SQLException {
        if (resultSet == null) throw new IllegalStateException("Result set is null");
        Long id = resultSet.getLong(1);
        ObjectNode organisaatio = getOrganisaatio(resultSet);
        Set<String> kunnat = getStringSet(resultSet, 3);
        Set<String> sahkopostit = getStringSet(resultSet, 4);
        String toimintamuoto = resultSet.getString(5);
        Kayttaja kayttaja = getKayttaja(resultSet);
        LocalDateTime vastaanotettu = resultSet.getTimestamp(12).toLocalDateTime();
        Rekisterointi.Tila tila = Rekisterointi.Tila.valueOf(resultSet.getString(13));
        return new Rekisterointi(id, organisaatio, kunnat, sahkopostit, toimintamuoto, kayttaja, vastaanotettu, tila);
    }

    private ObjectNode getOrganisaatio(ResultSet rs) throws SQLException {
        if (rs == null) return null;
        String json = rs.getString(2);
        if (json == null) return null;
        try {
            return (ObjectNode) OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    private Set<String> getStringSet(ResultSet resultSet, int index) throws SQLException {
        Array array = resultSet.getArray(index);
        if (array == null) return Collections.emptySet();
        String[] entries = (String[]) array.getArray();
        Set<String> results = new HashSet<>(Set.of(entries));
        return results;
    }

    private Kayttaja getKayttaja(ResultSet rs) throws SQLException {
        Kayttaja kayttaja = new Kayttaja();
        kayttaja.id = rs.getLong(6);
        kayttaja.etunimi = rs.getString(7);
        kayttaja.sukunimi = rs.getString(8);
        kayttaja.sahkoposti = rs.getString(9);
        kayttaja.asiointikieli = rs.getString(10);
        kayttaja.saateteksti = rs.getString(11);
        return kayttaja;
    }
}
