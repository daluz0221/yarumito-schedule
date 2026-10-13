package co.edu.ieruralyarumito.backend.entity;

import java.util.UUID;

import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Column;

@Entity
@Table (
    name = "claseProgramada",
    schema = "asignacionyhorario"    
)
public class ClaseProgramada {
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "horario_id")
    private Horario horarioId;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "asignacion_academica_id")
    private AsignacionAcademica asignacionAcademicaId;
    
    @Column (name = "dia", nullable = false)
    private DiasSemana dia;

    
}
