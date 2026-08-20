/*
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
package fi.vm.sade.tarjonta.shared.types;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @see fi.vm.sade.tarjonta.service.types.TarjontaTila
 * @author Timo Santasalo / Teknokala Ky
 * @author Jukka Raanamo
 */
public enum TarjontaTila {
    POISTETTU,
    LUONNOS,
    VALMIS,
    JULKAISTU,
    PERUTTU,
    KOPIOITU,
    PUUTTEELLINEN;
    
    /**
     * Privaatti jotta ei  mene sekaisin enum-arvojen kanssa
     * @see #cancellableValues() 
     */
    private static final TarjontaTila[] CANCELLABLE_DATA;
    
    /**
     * Privaatti jotta ei  mene sekaisin enum-arvojen kanssa
     * @see #publicValues()
     */
    private static final TarjontaTila[] PUBLIC_DATA;
    
    static {
    	List<TarjontaTila> cd = new ArrayList<TarjontaTila>();
    	List<TarjontaTila> pd = new ArrayList<TarjontaTila>();
    	
    	for (TarjontaTila tt : values()) {
    		if (tt.isCancellable()) {
    			cd.add(tt);
    		}
    		if (tt.isPublic()) {
    			pd.add(tt);
    		}    		
    	}
    	
    	
    	CANCELLABLE_DATA = cd.toArray(new TarjontaTila[cd.size()]);
    	PUBLIC_DATA = pd.toArray(new TarjontaTila[pd.size()]);
    }

    
    /**
     * Tilasiirtymäsääntö.
     * @return Tosi, jos tilasiirtymä on sallittu.
     */
    public boolean acceptsTransitionTo(TarjontaTila tt) {
    	if (tt==this) {
    		return true;
    	}
    	switch (tt) {
    	case VALMIS:
    		return this==LUONNOS || this==KOPIOITU || this==PERUTTU || this==PUUTTEELLINEN;
    	case PERUTTU:
    		return this==JULKAISTU;
    	case JULKAISTU:
    		return this==VALMIS || this==PERUTTU;    	
        case POISTETTU:
            return isRemovable();
        case LUONNOS:
            return this==KOPIOITU || this==PUUTTEELLINEN;
        default:
    		return false;
    	}
    }
    
    @Deprecated // muokattavuuslogiikka ei ole tilasidonnaista
    public boolean isMutable() {
    	return true; // this==LUONNOS || this==KOPIOITU;
    }

    //muokattu OVT-8135 mukaisesti
    public boolean isRemovable() {
        return this==LUONNOS || this==KOPIOITU || this==VALMIS || this==PUUTTEELLINEN;
    }
    
    /**
     * @see #cancellableValues()
     */
    public boolean isCancellable() {
    	return this==JULKAISTU || this==VALMIS;
    }
    
    /**
     * @see #publicValues()
     */
    public boolean isPublic() {
    	return this==JULKAISTU || this==PERUTTU;
    }
    
    /**
     * @see #isCancellable()
     */
    public static TarjontaTila[] cancellableValues() {
		return CANCELLABLE_DATA;
	}

    /**
     * @see #isPublic()
     */
    public static TarjontaTila[] publicValues() {
		return PUBLIC_DATA;
	}
    
    public static TarjontaTila valueOf(fi.vm.sade.tarjonta.service.types.TarjontaTila tt) {
    	return valueOf(tt.toString());
    }
    
    public fi.vm.sade.tarjonta.service.types.TarjontaTila asDto() {
    	return fi.vm.sade.tarjonta.service.types.TarjontaTila.valueOf(toString());
    }
    
}

