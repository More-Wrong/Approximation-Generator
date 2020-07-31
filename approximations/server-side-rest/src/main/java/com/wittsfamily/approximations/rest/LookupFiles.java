package com.wittsfamily.approximations.rest;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "lookup")
@Configuration
public class LookupFiles {
    private List<LookupFile> files;

    public LookupFiles(List<LookupFile> files) {
        this.files = files;
    }

    public LookupFiles() {

    }

    public List<LookupFile> getFiles() {
        return files;
    }

    public void setFiles(List<LookupFile> files) {
        this.files = files;
    }

    @ConfigurationProperties(prefix = "lookup.files")
    @Configuration
    public static class LookupFile {
        private String name;
        private String location;

        public LookupFile(String name, String location) {
            this.name = name;
            this.location = location;
        }

        public LookupFile() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

    }
}
