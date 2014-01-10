/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
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
 * European Union Public Licence for more details.
 */
package fi.vm.sade.organisaatio.revised.ui.component.mainview;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.ui.model.OrganisaatioModel;

/**
 * Apuluokka organisaation näkymään liittyvän logiikan yksinkertaistamiseksi.
 * 
 * TODO integroi {@link OrganisaatioModel}:iin
 * 
 * @author Timo Santasalo / Teknokala Ky
 */
public class OrganisaatioModelWrapper {

	private final List<OrganisaatioPerustieto> descendants = new ArrayList<OrganisaatioPerustieto>();
	
	private final List<Entry<MonikielinenTekstiTyyppi, List<Entry<MonikielinenTekstiTyyppi, Object>>>> dynaamisetYhteystiedot = new ArrayList<Map.Entry<MonikielinenTekstiTyyppi,List<Entry<MonikielinenTekstiTyyppi,Object>>>>();

	private final String oid;
	
	private OrganisaatioDTO org;
	
	private ImportState imported = ImportState.NONE;
	private Date importedDate;
		
	public OrganisaatioModelWrapper(String oid, List<OrganisaatioPerustieto> filter) {
		this.oid = oid;
		for (OrganisaatioPerustieto opt : filter) {
			if (oid==null/* || opt.getParentOidPath().contains(oid)*/) {
				descendants.add(opt);
			}
		}
	}
	
	public OrganisaatioModelWrapper(OrganisaatioDTO org, List<OrganisaatioPerustieto> filter) {
		this(org.getOid(), filter);
		this.org = org;
	}
	
	public void load(OrganisaatioService srv) {
		dynaamisetYhteystiedot.clear();
		if (oid==null) {
			return;
		}
		
		if (org==null) {
			org = srv.findByOid(oid);
		}
		
		if (org.getYtjPaivitysPvm()!=null) {
			imported = ImportState.YTJ;
			importedDate = org.getYtjPaivitysPvm();
		} else if (org.getTuontiPvm()!=null) {
			imported = ImportState.KOULUTA;
			importedDate = org.getTuontiPvm();
		} else {
			imported = ImportState.NONE;
			importedDate = null;
		}
		
		Map<String, Object> ytdValues = new TreeMap<String, Object>();
		for (YhteystietoArvoDTO yta : org.getYhteystietoArvos()) {
			ytdValues.put(yta.getKenttaOid(), yta.getArvo());
		}
		
		// huom! hakuparametriä ei käsitellä mitenkään; kutsu palauttaa kaiken..
		for (YhteystietojenTyyppiDTO yd : srv.findYhteystietojenTyyppis(null)) {
			boolean isUsed = false;
			for (YhteystietoElementtiDTO ed : yd.getAllLisatietokenttas()) {
				if (ytdValues.containsKey(ed.getOid())) {
					isUsed = true;
					break;
				}
			}
			
			if (isUsed) {
				List<Entry<MonikielinenTekstiTyyppi, Object>> vals = new ArrayList<Entry<MonikielinenTekstiTyyppi,Object>>();
				
				for (YhteystietoElementtiDTO ed : yd.getAllLisatietokenttas()) {
					Object ov = ytdValues.get(ed.getOid());
					if (ov==null) {
						continue;
					}
					vals.add(new Tuple<MonikielinenTekstiTyyppi, Object>(getTekstiFor(ed), ov));
				}
				dynaamisetYhteystiedot.add(new Tuple<MonikielinenTekstiTyyppi, List<Entry<MonikielinenTekstiTyyppi, Object>>>(yd.getNimi(), vals));
			}
		}
		
		
		/*for (Entry<MonikielinenTekstiTyyppi, List<Entry<MonikielinenTekstiTyyppi, Object>>> e : dynaamisetYhteystiedot) {
			System.err.println(e.getKey().getTeksti().get(0).getValue());
			for (Entry<MonikielinenTekstiTyyppi, Object> ev : e.getValue()) {
				System.err.println(" - " + ev.getKey().getTeksti().get(0).getValue()+" = "+ev.getValue());
			}
		}*/
	}
	
	
	
	private void appendTeksti(MonikielinenTekstiTyyppi ret, String code, String value) {
		if (value==null) {
			return;
		}
		Teksti txt = new Teksti();
		txt.setKieliKoodi(code);
		txt.setValue(value);
		ret.getTeksti().add(txt);
	}
	
	private MonikielinenTekstiTyyppi getTekstiFor(YhteystietoElementtiDTO ed) {
		MonikielinenTekstiTyyppi ret = new MonikielinenTekstiTyyppi();
		
		// TODO kielikoodit olisi hyvä hakea jostain vakiosta/enumeraatiosta jos sellainen olisi...
		appendTeksti(ret, "fi", ed.getNimi());
		appendTeksti(ret, "sv", ed.getNimiSv());
		return ret;
	}
	
	/**
	 * Yhteystietotyyppi -> Kenttä -> Arvo (joko {@link String} tai {@link YhteystietoDTO}).
	 * 
	 * @return
	 */
	public List<Entry<MonikielinenTekstiTyyppi, List<Entry<MonikielinenTekstiTyyppi, Object>>>> getDynaamisetYhteystiedot() {
		return dynaamisetYhteystiedot;
	}
	
	public OrganisaatioDTO get() {
		return org;
	}
	
	public List<OrganisaatioPerustieto> getChildren() {
		return descendants == null ? new ArrayList<OrganisaatioPerustieto>() : descendants;
	}

	public ImportState getImported() {
		return imported;
	}
	
	public Date getImportedDate() {
		return importedDate;
	}
		
	public void setDescendants(List<OrganisaatioPerustieto> descendants) {
	     this.descendants.clear();
	     for (OrganisaatioPerustieto curOrg : descendants) {
	         this.descendants.add(curOrg);
	     }
	}
	
	public OsoiteDTO getOsoite(OsoiteTyyppi tp) {
		for (YhteystietoDTO ytd : org.getYhteystiedot()) {
			if (ytd instanceof OsoiteDTO) {
				OsoiteDTO o = (OsoiteDTO) ytd;
				if (tp==o.getOsoiteTyyppi()) {
					return o;
				}
			}
		}
		return null;
	}

        public OsoiteDTO getHakutoimistoOsoite(OsoiteTyyppi tp) {
            for (YhteystietoDTO ytd : org.getKuvailevatTiedot().getHakutoimisto().getOpintotoimistoYhteystiedot()) {
                if (ytd instanceof OsoiteDTO) {
                    OsoiteDTO o = (OsoiteDTO) ytd;
                    if (tp==o.getOsoiteTyyppi()) {
                        return o;
                    }
                }
            }
            return null;
        }

        
	private static class Tuple<K, V> implements Entry<K, V> {
		
		private final K key;
		private final V value;
		
		public Tuple(K key, V value) {
			super();
			this.key = key;
			this.value = value;
		}
		
		public K getKey() {
			return key;
		}
		
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			return null;
		}
		
	}	
	
	public static enum ImportState {
		NONE,
		YTJ,
		KOULUTA;
	}
	
}
