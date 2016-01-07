package fi.vm.sade.organisaatio.helper;

import java.util.Date;

import org.junit.Assert;

import org.junit.Test;

public class JaxbConverterTest {

    @Test
    public void testRoundtrip() {
        Date orig = new Date(0);
        String dateString = JaxbConverter.printDate(orig);
        Date converted = JaxbConverter.parseDate(dateString);
        Assert.assertEquals(orig, converted);
    }

}
