package fi.vm.sade.organisaatio;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class ResourceUtils {

    private ResourceUtils() {
    }

    public static String classPathResourceAsString(String path) {
        return classPathResourceAsString(path, StandardCharsets.UTF_8);
    }

    public static String classPathResourceAsString(String path, Charset charset) {
        return resourceToString(new ClassPathResource(path), charset);
    }

    public static String resourceToString(Resource resource, Charset charset) {
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), charset)) {
            return FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
