package fi.vm.sade.organisaatio.model.dto;

import com.querydsl.core.annotations.QueryProjection;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;

import java.util.Date;

/**
 * 
 * @author Markus
 *
 */
public class OrgPerustieto {

	private String oid;
    private long version;
    private Date alkuPvm;
    private Date lakkautusPvm;
    private MonikielinenTeksti nimi;
    private String parentOidPath;
    private String ytunnus;
    private String oppilaitosKoodi;
    
    @QueryProjection
    public OrgPerustieto(String oid, long version, Date alkuPvm, Date lakkautusPvm,
    					MonikielinenTeksti nimi, String ytunnus, String oppilaitosKoodi, 
    					String parentOidPath) {
    	this.oid = oid;
    	this.version = version;
    	this.alkuPvm = alkuPvm;
    	this.lakkautusPvm = lakkautusPvm;
    	this.nimi = nimi;
    	this.ytunnus = ytunnus;
    	this.oppilaitosKoodi = oppilaitosKoodi;
		this.parentOidPath = parentOidPath;
    }
	
    public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	public Date getAlkuPvm() {
		return alkuPvm;
	}
	public void setAlkuPvm(Date alkuPvm) {
		this.alkuPvm = alkuPvm;
	}
	public Date getLakkautusPvm() {
		return lakkautusPvm;
	}
	public void setLakkautusPvm(Date lakkautusPvm) {
		this.lakkautusPvm = lakkautusPvm;
	}
	
	public String getParentOidPath() {
		return parentOidPath;
	}
	public void setParentOidPath(String parentOidPath) {
		this.parentOidPath = parentOidPath;
	}
	public String getYtunnus() {
		return ytunnus;
	}
	public void setYtunnus(String ytunnus) {
		this.ytunnus = ytunnus;
	}
	public String getOppilaitosKoodi() {
		return oppilaitosKoodi;
	}
	public void setOppilaitosKoodi(String oppilaitosKoodi) {
		this.oppilaitosKoodi = oppilaitosKoodi;
	}

	public MonikielinenTeksti getNimi() {
		return nimi;
	}

	public void setNimi(MonikielinenTeksti nimi) {
		this.nimi = nimi;
	}
	
}
