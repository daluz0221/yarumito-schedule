package co.edu.ieruralyarumito.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// Datos requeridos para finalizar la vigencia de una idoneidad.
public class FinalizarVigenciaIdoneidadRequest {

    // Fecha en la que termina la vigencia de la idoneidad.
    @NotNull(message = "La fecha de finalización de vigencia es obligatoria")
    private LocalDate vigenteHasta;

    public LocalDate getVigenteHasta() {
        return vigenteHasta;
    }

    public void setVigenteHasta(LocalDate vigenteHasta) {
        this.vigenteHasta = vigenteHasta;
    }
}
