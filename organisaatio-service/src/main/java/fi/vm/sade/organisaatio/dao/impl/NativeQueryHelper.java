package fi.vm.sade.organisaatio.dao.impl;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Component
public class NativeQueryHelper {

    private final ResourceLoader resourceLoader;

    public NativeQueryHelper(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String getSqlQueryAsString(String location, Charset charset) {
        try {
            Resource resource = resourceLoader.getResource(location);
            try (InputStream inputStream = resource.getInputStream()) {
                return StreamUtils.copyToString(inputStream, charset);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
