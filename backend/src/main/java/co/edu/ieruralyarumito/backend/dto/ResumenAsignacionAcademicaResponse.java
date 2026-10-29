package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;

public class ResumenAsignacionAcademicaResponse {
    private String grupo;
    private String grado;
    private int horasRequeridas;
    private int horasAsignadas;
    private EstadoAsignacionAcademica estado;

    public ResumenAsignacionAcademicaResponse() {
    }

    public ResumenAsignacionAcademicaResponse(String grupo, String grado, int horasRequeridas, int horasAsignadas, EstadoAsignacionAcademica estado) {
        this.grupo = grupo;
        this.grado = grado;
        this.horasRequeridas = horasRequeridas;
        this.horasAsignadas = horasAsignadas;
        this.estado = estado;
    }

    public String getGrupo() {return grupo;}
    public String getGrado() {return grado;}
    public int getHorasRequeridas() {return horasRequeridas;}
    public int getHorasAsignadas() {return horasAsignadas;}
    public EstadoAsignacionAcademica getEstado() {return estado;}

    public void setGrupo(String grupo) {this.grupo = grupo;}
    public void setGrado(String grado) {this.grado = grado;}
    public void setHorasRequeridas(int horasRequeridas) {this.horasRequeridas = horasRequeridas;}
    public void setHorasAsignadas(int horasAsignadas) {this.horasAsignadas = horasAsignadas;}
    public void setEstado(EstadoAsignacionAcademica estado) {this.estado = estado;}
}
