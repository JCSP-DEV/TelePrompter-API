package juancarlos.tfg.teleprompter.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

/**
 * Configuration class for file upload settings.
 * Configures multipart file upload capabilities and sets maximum file size limits.
 *
 
 */
@Configuration
public class FileUploadConfig {

    /**
     * Creates a multipart resolver bean for handling file uploads.
     *
     
     * @return A new StandardServletMultipartResolver instance
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /**
     * Creates a multipart configuration element with size limits.
     * Sets maximum file size and request size to 10MB.
     *
     * @return A MultipartConfigElement with configured size limits
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));
        return factory.createMultipartConfig();
    }
} 