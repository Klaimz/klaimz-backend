package com.klaimz.service;


import com.klaimz.model.SystemProperties;
import com.klaimz.repo.SysPropertiesRepo;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SystemPropertiesService {

    @Inject
    private SysPropertiesRepo systemPropertiesRepo;


    public void setSystemProperty(String id, String value) {
        systemPropertiesRepo.update(new SystemProperties(id, value));
    }


    public String getSystemProperty(String id) {
        return systemPropertiesRepo.findById(id).map(p -> p.getValue()).orElse(null);
    }
}
