package com.klaimz.test;


import com.klaimz.model.Role;
import com.klaimz.repo.RoleRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

@MicronautTest(startApplication = false,environments = "awesome")
public class SimpleRepositoryTest {


    @Inject
    private RoleRepository roleRepository;


    @Test
    public void testFindByKeyIn() {
        List<String> keys = List.of("ADMIN");
        List<Role> roles = roleRepository.findByKeyIn(keys);
        assert roles.isEmpty();
    }


}
