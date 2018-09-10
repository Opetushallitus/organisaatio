/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package fi.vm.sade.organisaatio.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang.StringUtils;

public class DateParam extends AbstractParam<Date> {

    public DateParam(String param) throws WebApplicationException {
        super(param);
    }

    @Override
    protected Date parse(String param) throws Throwable {
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
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
                    .entity("Couldn't parse date string: " + e.getMessage())
                    .build());
        }

    }
}
