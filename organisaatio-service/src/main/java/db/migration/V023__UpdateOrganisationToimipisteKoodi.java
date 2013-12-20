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
package db.migration;

import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * OVT-4954 "Natural key"
 * <pre>
 * Luontaiset avaimet:
 * Koulutustoimija: y-tunnus
 * Oppilaitos: oppilaitosnumero
 * Toimipiste(1.taso):oppilaitosnumero+toimipisteenjärjestysnumero(concatenoituna) 1234501, 1234502 jne.
 *
 * Syvemmälle tasolle ei ole mitään sellaista avainta joka voitaisiin johtaa nykytilasta, mutta voisiko hoitaa lisäämällä perään järjestysnumero?
 * Toimipiste(2.taso):oppilaitosnumero+toimipisteenjärjestysnumero+toimipisteenjno
 *
 * Hierarkian muutos: Jos oikeasti siirretään organisaatio toisen alle, niin:
 * Espoo - > Latokasken lukio(12345) - > toimipiste 1 (1234501)
 * siirtyy olemaan
 * Espoo - > Tapiolan lukio (54321)- > toimipiste 1 (5432101)
 * ja muutos historioidaan 5432101 -> on ollut 1234501 ajanhetkenä t=0
 * Muu organisaatio: Tähän ei ole olemassa mitään selkeää vastausta. Ehkä ei tarvita.
 * Oppisopimustoimipiste: Nämä kiinnitetään suoraan koulutustoimijan alle. Voisi olla siis esim. y-tunnus+toimipisteenjärjestysnumero.
 * Tähänkään ei ole mitään ilmeistä vastausta...y-tunnukset, oppilaitosnumerot, oppilaitosnumerot+toimipisteenjno:t ovat vakiintuneita ja
 * tunnettuja tapoja kaikki muu pitäisi keksiä nyt.
 * </pre>
 *
 * @author mlyly
 */
public class V023__UpdateOrganisationToimipisteKoodi implements SpringJdbcMigration {

    private static final Logger LOG = LoggerFactory.getLogger(V023__UpdateOrganisationToimipisteKoodi.class);
    private Map<String, Map<String, Object>> _organisations = new HashMap<String, Map<String, Object>>();
    private int _numUpdated = 0;

    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        LOG.info("migrate()...");

        // Get all organisations
        List<Map> resultSet = jdbcTemplate.query("SELECT * FROM organisaatio o", new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map r = new HashMap<String, Object>();

                ResultSetMetaData metadata = rs.getMetaData();
                for (int i = 1; i <= metadata.getColumnCount(); i++) {
                    String cname = metadata.getColumnName(i);
                    int ctype = metadata.getColumnType(i);

                    switch (ctype) {
                        case Types.VARCHAR:
                            r.put(cname, rs.getString(cname));
                            break;

                        default:
                            break;
                    }
                }

                LOG.debug("  read from db : org = {}", r);

                _organisations.put((String) r.get("oid"), r);
                return r;
            }
        });

        // Generate and update initial values for toimipistekoodis
        for (Map org : resultSet) {
            if (isToimipiste(org, jdbcTemplate)) {
                String tpKoodi = calculateToimipisteKoodi(org, jdbcTemplate);
                updateToimipisteKoodi(org, tpKoodi, jdbcTemplate);
            }
        }

        LOG.info("  Processed {} organisations, updated {} Opetuspistes", _organisations.size(), _numUpdated);

        LOG.info("migrate()... done.");
    }

    private boolean isToimipiste(Map org, JdbcTemplate jdbcTemplate) {
        String organisaatiotyypitstr = (String) org.get("organisaatiotyypitstr");
        return organisaatiotyypitstr != null && organisaatiotyypitstr.contains("Opetuspiste");
    }

    private boolean isOppilaitos(Map org) {
        String organisaatiotyypitstr = (String) org.get("organisaatiotyypitstr");
        return organisaatiotyypitstr != null && organisaatiotyypitstr.contains("Oppilaitos");
    }

    private String calculateToimipisteKoodi(Map org, JdbcTemplate jdbcTemplate) {
        String result = null;

        // Find parent who is Oppilaitos
        String oppilaitoskoodi = findParentOppilaitoskoodi(org, jdbcTemplate);
        if (oppilaitoskoodi != null) {
            String jarjestysnumero = (String) org.get("opetuspisteenjarjnro");
            if (jarjestysnumero == null) {
                // TODO what to do here, - no koulutus "opetuspisteenjarjnro" available
                jarjestysnumero = "01";

                LOG.error("  Opetuspiste does not have opetuspisteenjarjnro field: org = {}", org);
            }

            result = oppilaitoskoodi + jarjestysnumero;
        }

        return result;
    }

    private void updateToimipisteKoodi(Map org, String tpKoodi, JdbcTemplate jdbcTemplate) {
        String oid = (String) org.get("oid");
        int result = jdbcTemplate.update("UPDATE organisaatio SET toimipisteKoodi = ? WHERE oid = ?", tpKoodi, oid);

        _numUpdated++;

        if (result < 0 || result > 1) {
            LOG.error("TP KOODI UPDATED ON {} ORGS for oid = {}", result, oid);
        } else {
            LOG.debug("  tp koodi updated for {} to {}", oid, tpKoodi);
        }
    }

    private String findParentOppilaitoskoodi(Map org, JdbcTemplate jdbcTemplate) {

        String parentOidPath = (String) org.get("parentoidpath");
        if (parentOidPath == null) {
            return null;
        }

        String[] parentOids = parentOidPath.split("\\|");

        for (int i = parentOids.length - 1; i >= 0; i--) {
            String parentOid = parentOids[i];

            Map<String, Object> parentOrg = _organisations.get(parentOid);
            if (parentOrg != null) {
                if (isOppilaitos(parentOrg)) {
                    // LOG.error("  parent Oppilaitos match! oidpath = {} - parent = {}", parentOidPath, parentOrg);
                    return (String) parentOrg.get("oppilaitoskoodi");
                }
            }
        }

        LOG.error("  Opetuspiste does not have Oppilaitos parent? org= ", org);

        return (String) null;
    }
}
