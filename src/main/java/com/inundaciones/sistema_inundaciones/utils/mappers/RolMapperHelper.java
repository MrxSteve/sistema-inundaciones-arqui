package com.inundaciones.sistema_inundaciones.utils.mappers;

import com.inundaciones.sistema_inundaciones.models.entities.Rol;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RolMapperHelper {
    public List<String> rolesToStringList(List<Rol> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Rol::getNombre)
                .collect(Collectors.toList());
    }
}
