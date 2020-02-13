/*
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * OVT-8420
 * Aiemmin ECTS tiedot ovat olleet vain yhdellä kielellä.
 * Kopioidaan vanhat ECTS tiedot suomenkieliseen monkilieliseen tekstiin.
 *
 * @author hpyy
 */
public class V055__UpdateECTS extends BaseJavaMigration {

    private static final Logger LOG = LoggerFactory.getLogger(V055__UpdateECTS.class);
    private Map<String, Map<String, Object>> _organisations = new HashMap<String, Map<String, Object>>();
    private int _numUpdated = 0;

    public void migrate(Context context) throws Exception {
        LOG.info("migrate()...");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(
                new SingleConnectionDataSource(context.getConnection(), true));

        // Get all organisaatiometadatas where there are strings to process
        List<Map> resultSet = jdbcTemplate.query("SELECT id,hakutoimistoectsemail,hakutoimistoectsnimi,hakutoimistoectspuhelin,hakutoimistoectstehtavanimike FROM organisaatiometadata WHERE hakutoimistoectsemail<>'' OR hakutoimistoectsnimi<>'' OR hakutoimistoectspuhelin<>'' OR hakutoimistoectstehtavanimike<>''", new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map r = new HashMap<String, Object>();

                ResultSetMetaData metadata = rs.getMetaData();
                for (int i = 1; i <= metadata.getColumnCount(); i++) {
                    String cname = metadata.getColumnName(i);
                    int ctype = metadata.getColumnType(i);
                    
                    switch (ctype) {
                        case Types.VARCHAR: // hakutoimistoectsemail,hakutoimistoectsnimi,hakutoimistoectspuhelin,hakutoimistoectstehtavanimike
                            r.put(cname, rs.getString(cname));
                            break;

                        case Types.BIGINT: // id
                            r.put(cname, rs.getInt(cname));
                            break;

                        default:
                            break;
                    }
                }

                LOG.debug("  read from db : organisaatiometadata = {}", r);
                
                return r;
            }
        });
        
        // Move strings to monikielinenteksti_values
        for (Map orgmd : resultSet) {
            
            handleOrganisaatiometadata(orgmd, jdbcTemplate);
            
        }

        LOG.info("migrate()... done.");
    }
    
    private int getNextHibernateSequence(JdbcTemplate jdbcTemplate) {
        // Returns next global id
        List<Map> resultSet = jdbcTemplate.query("SELECT nextval('public.hibernate_sequence')", new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map r = new HashMap<String, Object>();

                ResultSetMetaData metadata = rs.getMetaData();
                for (int i = 1; i <= metadata.getColumnCount(); i++) {
                    String cname = metadata.getColumnName(i);
                    int ctype = metadata.getColumnType(i);
                    
                    switch (ctype) {
                        case Types.BIGINT: // id
                            r.put(cname, rs.getInt(cname));
                            break;

                        default:
                            break;
                    }
                }

                return r;
            }
        });
        
        for (Map m : resultSet) {
            return (int) m.get("nextval");
        }
        return 0;
    }
    
    private int insertNewMkt(String teksti, JdbcTemplate jdbcTemplate) {
        int mkt_id = getNextHibernateSequence(jdbcTemplate);

        if (mkt_id == 0) {
            LOG.error("Could not get mkt_id.");
            return 0;
        } else {
            LOG.debug("mkt_id: {}", mkt_id);

            // Create new monikielinenteksti
            int result = jdbcTemplate.update("INSERT INTO monikielinenteksti (id,version) VALUES (?, ?)", mkt_id, 0);
            if (result < 0 || result > 1) {
                LOG.error("Failed to INSERT monikielinenteksti: {}.", mkt_id);
            } else {
                LOG.info("INSERTED monikielinenteksti: {}.", mkt_id);
            }
            // Create new monikielinenteksti_value
            result = jdbcTemplate.update("INSERT INTO monikielinenteksti_values (id,value,key,index) VALUES (?, ?, ?, ?)", mkt_id, teksti, "kieli_fi#1", 0);
            if (result < 0 || result > 1) {
                LOG.error("Failed to INSERT monikielinenteksti_values: {}.", mkt_id);
            } else {
                LOG.info("INSERTED monikielinenteksti_values: {}.", mkt_id);
            }
        }
        return mkt_id;
    }
    
    private void handleOrganisaatiometadata(Map md, JdbcTemplate jdbcTemplate) {
        int id = (int) md.get("id");
        String hakutoimistoectsemail = (String) md.get("hakutoimistoectsemail");
        String hakutoimistoectsnimi = (String) md.get("hakutoimistoectsnimi");
        String hakutoimistoectspuhelin = (String) md.get("hakutoimistoectspuhelin");
        String hakutoimistoectstehtavanimike = (String) md.get("hakutoimistoectstehtavanimike");
        int email_mkt_id = 0;
        int nimi_mkt_id = 0;
        int puhelin_mkt_id = 0;
        int tehtavanimike_mkt_id = 0;
        
        if (hakutoimistoectsemail != null && hakutoimistoectsemail.length() > 0) {
            email_mkt_id = insertNewMkt(hakutoimistoectsemail, jdbcTemplate);
        }
        if (hakutoimistoectsnimi != null && hakutoimistoectsnimi.length() > 0) {
            nimi_mkt_id = insertNewMkt(hakutoimistoectsnimi, jdbcTemplate);
        }
        if (hakutoimistoectspuhelin != null && hakutoimistoectspuhelin.length() > 0) {
            puhelin_mkt_id = insertNewMkt(hakutoimistoectspuhelin, jdbcTemplate);
        }
        if (hakutoimistoectstehtavanimike != null && hakutoimistoectstehtavanimike.length() > 0) {
            tehtavanimike_mkt_id = insertNewMkt(hakutoimistoectstehtavanimike, jdbcTemplate);
        }
        
        String update = "";
        
        if (email_mkt_id != 0) {
            update += "hakutoimistoectsemailmkt="+email_mkt_id;
        }
        if (nimi_mkt_id != 0) {
            if (update.length() > 0) {
                update += ",";
            }
            update += "hakutoimistoectsnimimkt=" +nimi_mkt_id;
        }
        if (puhelin_mkt_id != 0) {
            if (update.length() > 0) {
                update += ",";
            }
            update += "hakutoimistoectspuhelinmkt="+puhelin_mkt_id;
        }
        if (tehtavanimike_mkt_id != 0) {
            if (update.length() > 0) {
                update += ",";
            }
            update += "hakutoimistoectstehtavanimikemkt="+tehtavanimike_mkt_id;
        }
        
        // Update the metadata
        int result = jdbcTemplate.update("UPDATE organisaatiometadata SET " + update + " WHERE id=?", id);
        if (result < 0 || result > 1) {
            LOG.error("Failed to UPDATE organisaatiometadata: {}.", update);
        } else {
            LOG.info("UPDATED organisaatiometadata for id {}: {}.", id, update);
        }
    }
}
