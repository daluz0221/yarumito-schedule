
package co.edu.ieruralyarumito.backend.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Resultado del informe de perfiles profesionales
 * e idoneidades docentes.
 */
public record InformePerfilProfesionalResponse(
        LocalDate fechaReferencia,
        long cantidadDocentes,
        long cantidadTitulos,
        long cantidadIdoneidades,
        List<DetalleDocente> docentes
) {

    public InformePerfilProfesionalResponse {
        docentes = List.copyOf(docentes);
    }

    public record DetalleDocente(
            UUID docenteId,
            String nombreCompleto,
            String areaNombramiento,
            List<InformeTituloProfesionalDetalleResponse> titulos,
            List<InformeIdoneidadDetalleResponse> idoneidades
    ) {
        public DetalleDocente {
            titulos = List.copyOf(titulos);
            idoneidades = List.copyOf(idoneidades);
        }
    }
}
