package com.test.mysede.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa a un socio comunitario y sus beneficiarios asociados.
 */
public class SocioComunitario {
    private String id;
    private final String nombre;
    private final List<Beneficiario> beneficiarios;

    public SocioComunitario(String nombre) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre del socio comunitario es obligatorio");
        this.beneficiarios = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public List<Beneficiario> getBeneficiarios() {
        return Collections.unmodifiableList(beneficiarios);
    }

    public void agregarBeneficiario(Beneficiario beneficiario) {
        beneficiarios.add(Objects.requireNonNull(beneficiario));
    }

    public void agregarBeneficiarios(List<Beneficiario> nuevosBeneficiarios) {
        for (Beneficiario beneficiario : Objects.requireNonNull(nuevosBeneficiarios)) {
            agregarBeneficiario(beneficiario);
        }
    }
}