/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package fi.vm.sade.organisaatio.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fi.vm.sade.organisaatio.resource.ApiException;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DateParam extends AbstractParam<Date> {

    public DateParam(String param) throws ApiException {
        super(param);
    }

    @Override
    protected Date parse(String param) throws ApiException {
        if (StringUtils.isEmpty(param)) {
            return null;
        }

        String datePattern = "yyyy-MM-dd";
        String datetimePattern = "yyyy-MM-dd HH:mm";
        SimpleDateFormat dateFormat;

        if(param.length() == datetimePattern.length()) {
            dateFormat = new SimpleDateFormat(datetimePattern);
        } else {
            dateFormat = new SimpleDateFormat(datePattern);
        }

        try {
            return dateFormat.parse(param);
        } catch (ParseException e) {
            throw new ApiException(new ResponseEntity<>(
                    "Couldn't parse date string: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST));
        }

    }
}
