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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic result wrapping for REST apis.
 *
 * Examples:
 * <pre>
 * {
 *   status: "OK",
 *   result: {
 *     …
 *   }
 * }
 * </pre>
 *
 * <pre>
 * {
 *    status: "VALIDATION",
 *    result: {
 *      …
 *    } ,
 *
 *    errors: [
 *      {
 *        errorCode: "VALIDATION",
 *	      errorTarget: "USER/1.2.3.4.5",
 *	      errorField: "password",
 *        errorMessageKey: "password.tooShort",
 *        errorMessageParameters: [ "foobar", 10],
 *	},
 *      {
 *        errorCode: "VALIDATION",
 *        errorTarget: "1.2.3.4.5",
 *	      errorField: "email",
 *        errorMessageKey: "email.reserved",
 *	},
 *    ]
 *}
 * </pre>
 *
 * <pre>
 * {
 *     status: "ERROR",
 *     errors: [
 *       {
 *           errorCode: "ERROR",
 *           errorTarget: "Hakukohde/1.2.3.4.5",
 *           errorMessageKey: "system.error",
 *           errorTechnicalInformation: "java.lang.NullPointerExceltion in Hakukohde.java:752 …"
 *       }
 *     ]
 * }
 * </pre>
 *
 *
 * @author mlyly
 */
public class ResultV1RDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Generic error codes.
     */
    public enum ResultStatus {

        /**
         * Indicates NO error.
         */
        OK,
        /**
         * Information for the user.
         */
        INFO,
        /**
         * Warning for the user.
         */
        WARNING,
        /**
         * Validation errors.
         */
        VALIDATION,
        /**
         * A real "error".
         */
        ERROR,

        /**
         * For those occasions requested resource is not found.
         */
        NOT_FOUND
    };
    /**
     * Default status for the result is OK.
     */
    private ResultStatus _status = ResultStatus.OK;
    /**
     * Actual result contained here.
     */
    private T _result;
    /**
     * "Sub" errors for this one (if this one is a "master" error).
     */
    private List<ErrorV1RDTO> _errors;

    /**
     * Used search params saved here.
     */
    private GenericSearchParamsV1RDTO _params = null;


    private Map<String, Boolean> _accessRights = new HashMap<String, Boolean>();

    public ResultStatus getStatus() {
        return _status;
    }

    public void setStatus(ResultStatus _status) {
        this._status = _status;
    }

    public ResultV1RDTO() {
    }

    public ResultV1RDTO(T result) {
        setResult(result);
    }

    public ResultV1RDTO(T result, ResultStatus status) {
        setResult(result);
        setStatus(status);
    }

    public T getResult() {
        return _result;
    }

    public void setResult(T result) {
        _result = result;
    }

    public List<ErrorV1RDTO> getErrors() {
        return _errors;
    }

    public void setErrors(List<ErrorV1RDTO> _errors) {
        this._errors = _errors;

        if (hasErrors()) {
            setStatus(ResultStatus.ERROR);
        }
    }

    /**
     * Adds an error, marks this result state to be ERROR.
     *
     * @param error
     */
    public void addError(ErrorV1RDTO error) {
        if (error != null) {
            if (getErrors() == null) {
                setErrors(new ArrayList<ErrorV1RDTO>());
            }
            getErrors().add(error);

            // Well, now we have an error...
            setStatus(ResultStatus.ERROR);
        }
    }
    
    public void addTechnicalError(Throwable t) {
        setStatus(ResultV1RDTO.ResultStatus.ERROR);
        ErrorV1RDTO errorRDTO = new ErrorV1RDTO();
        //t.printStackTrace();
        errorRDTO.setErrorTechnicalInformation(t.toString());
        errorRDTO.setErrorCode(ErrorV1RDTO.ErrorCode.ERROR);
        addError(errorRDTO);
    }

    /**
     * Returns true if any "errors" added. ie. getErrors() returns nonempty collection.
     *
     * @return
     */
    public boolean hasErrors() {
        return _errors != null && !_errors.isEmpty();
    }

    /**
     * Return true if any of the errors is given type.
     *
     * @param errorType
     * @return
     */
    public boolean hasErrors(ErrorV1RDTO.ErrorCode errorType) {
        boolean result = false;
        for (ErrorV1RDTO errorV1RDTO : getErrors()) {
            result = result || errorV1RDTO.getErrorCode().equals(errorType);
        }
        return result;
    }

    public GenericSearchParamsV1RDTO getParams() {
        return _params;
    }

    public void setParams(GenericSearchParamsV1RDTO _params) {
        this._params = _params;
    }

    public Map<String, Boolean> getAccessRights() {
        return _accessRights;
    }

    public void setAccessRights(Map<String, Boolean> accessRights) {
        if (accessRights == null) {
            accessRights = new HashMap<String, Boolean>();
        }
        this._accessRights = accessRights;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("[status=");
        sb.append(getStatus() != null ? getStatus().name() : "null");
        sb.append(", errors=[");
        if (hasErrors()) {
            for (ErrorV1RDTO errorV1RDTO : _errors) {
                sb.append(errorV1RDTO.toString());
                sb.append(", ");
            }
        }
        sb.append("], result=");
        sb.append(getResult() != null ? getResult().toString() : "null");
        sb.append("]");
        return sb.toString();
    }

    public static <T>ResultV1RDTO<T> create(ResultStatus status, T t, ErrorV1RDTO error) {
        ResultV1RDTO<T> r = new ResultV1RDTO<T>(t);
        r.addError(error);
        return r;
    }

    public static <T> ResultV1RDTO<T> notFound() {
        ResultV1RDTO<T> r = new ResultV1RDTO<>();
        r.setStatus(ResultStatus.NOT_FOUND);
        return r;
    }





}
