import { api } from "./client";
function listarActividadesDocente(docenteId, signal) {
  return api.get(`/api/docentes/${docenteId}/actividades`, {
    signal
  });
}
function asignarActividadDocente(docenteId, payload) {
  return api.post(
    `/api/docentes/${docenteId}/actividades`,
    payload
  );
}
export {
  asignarActividadDocente,
  listarActividadesDocente
};
