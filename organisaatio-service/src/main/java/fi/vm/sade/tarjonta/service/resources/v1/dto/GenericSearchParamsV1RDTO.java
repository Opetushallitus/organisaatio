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
package fi.vm.sade.tarjonta.service.resources.v1.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Used to specify generic search parameters for common "GET" search methods.
 *
 * <pre>
 *   ...?count=666&startIndex=555&modifiedAfter=1380894708513
 * </pre>
 *
 * @author mlyly
 */
public class GenericSearchParamsV1RDTO implements Serializable {

    private int _startIndex = 0;
    private int _count = 100;
    private long _modifiedBefore = 0;
    private long _modifiedAfter = 0;

    @Override
    public String toString() {
        return "GenericSearchParamsRDTO[startIndex=" + getStartIndex() + ", count=" + getCount() + "]";
    }

    public int getStartIndex() {
        return _startIndex;
    }

    public void setStartIndex(int startIndex) {
        this._startIndex = startIndex;
    }

    public int getCount() {
        return _count;
    }

    public void setCount(int _count) {
        this._count = _count;
    }

    public long getModifiedAfter() {
        return _modifiedAfter;
    }

    /**
     * @return Null if "0", else Date.
     */
    public Date getModifiedAfterAsDate() {
        if (getModifiedAfter() > 0) {
            return new Date(getModifiedAfter());
        } else {
            return null;
        }
    }

    public void setModifiedAfter(long modifiedAfter) {
        this._modifiedAfter = modifiedAfter;
    }

    public long getModifiedBefore() {
        return _modifiedBefore;
    }

    /**
     * @return Null if "0", else Date.
     */
    public Date getModifiedBeforeAsDate() {
        if (getModifiedBefore() > 0) {
            return new Date(getModifiedBefore());
        } else {
            return null;
        }
    }

    public void setModifiedBefore(long modifiedBefore) {
        this._modifiedBefore = modifiedBefore;
    }



}
