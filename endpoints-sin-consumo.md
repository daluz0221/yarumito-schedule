# Endpoints expuestos y su consumo en el frontend

El frontend consume todos los endpoints de este inventario. Ya no queda ninguno sin conectar.

## Conectados

- [x] `GET /api/v1/sedes`
- [x] `POST /api/v1/sedes`
- [x] `GET /api/v1/sedes/{id}`
- [x] `PUT /api/v1/sedes/{id}`
- [x] `GET /api/v1/grados`
- [x] `POST /api/v1/grados`
- [x] `GET /api/v1/grados/{id}`
- [x] `PUT /api/v1/grados/{id}`
- [x] `GET /api/v1/anios-escolares`
- [x] `POST /api/v1/anios-escolares`
- [x] `GET /api/v1/anios-escolares/{id}`
- [x] `PUT /api/v1/anios-escolares/{id}`
- [x] `GET /api/v1/planes-estudio`
- [x] `POST /api/v1/planes-estudio`
- [x] `GET /api/v1/planes-estudio/{id}`
- [x] `PUT /api/v1/planes-estudio/{id}`
- [x] `GET /api/v1/grupos`
- [x] `POST /api/v1/grupos`
- [x] `GET /api/v1/grupos/{id}`
- [x] `PUT /api/v1/grupos/{id}`
- [x] `GET /api/v1/grupos/siguiente-codigo`
- [x] `POST /api/v1/aulas`
- [x] `GET /api/v1/aulas?sedeId=`
- [x] `GET /api/v1/aulas/{id}`
- [x] `PUT /api/v1/aulas/{id}`
- [x] `POST /api/v1/aulas/{id}/nueva-version`
- [x] `DELETE /api/v1/aulas/{id}`
- [x] `POST /api/v1/turnos`
- [x] `GET /api/v1/turnos`
- [x] `GET /api/v1/turnos/{id}`
- [x] `PUT /api/v1/turnos/{id}`
- [x] `POST /api/v1/franjas-horarias`
- [x] `GET /api/v1/franjas-horarias?turnoId=`
- [x] `GET /api/v1/franjas-horarias/{id}`
- [x] `PUT /api/v1/franjas-horarias/{id}`
- [x] `GET /api/v1/catalogos/areas`
- [x] `GET /api/v1/catalogos/asignaturas`
- [x] `GET /api/v1/catalogos/regimenes-docentes`
- [x] `GET /api/v1/catalogos/escalafones`

### Cómo probar la integración

1. Levanta frontend (`http://localhost:5173`) y backend (`http://localhost:8080`). Con Docker: `docker compose --profile app up --build`.
2. Entra con `rector@yarumito.edu.co` / `Admin123!`.
3. Abre **Sedes** en el menú (o entra a `/dashboard/sedes`).
4. Pulsa **+ Nueva sede**. Escribe nombre, código y dirección, marca **Esta es la sede principal** y pulsa **Guardar sede**. La sede debe aparecer en la tabla, con nombre y código en mayúsculas.
5. Pulsa **Editar**. El formulario debe llenarse con los datos guardados (esa carga es `GET /api/v1/sedes/{id}`).
6. Cambia la dirección y pulsa **Guardar cambios**. La tabla debe mostrar la dirección nueva.
7. Crea otra sede con el mismo código: debe mostrarse «Ya existe una sede con ese código». Márcala también como principal: debe mostrarse «Ya existe otra sede principal registrada».
8. Abre **Grados** en el menú (o entra a `/dashboard/grados`).
9. Pulsa **+ Nuevo grado**, elige **6° Sexto** y pulsa **Guardar grado**. La tabla debe mostrar Sexto, tipo Básica, prioridad 5 y 30 horas.
10. Pulsa **Editar**. El selector debe quedar en 6° (esa carga es `GET /api/v1/grados/{id}`).
11. Cámbialo a **10° Décimo** y pulsa **Guardar cambios**. La fila debe pasar a Décimo, tipo Media, prioridad 1 y 37 horas.
12. Intenta crear otro 10°: el formulario debe avisar «Ya existe un grado de ese nivel» y no enviarlo.
13. Abre **Año lectivo y grupos** en el menú (o entra a `/dashboard/anio-lectivo`).
14. Pulsa **+ Nuevo año lectivo**. Deja el año sugerido, las fechas, el estado **Planeación** y sin marcarlo como actual. Pulsa **Crear año lectivo**. El año debe aparecer en el selector, con estado Planeación y año actual No.
15. Con ese año seleccionado, pulsa **Editar fechas**. El formulario debe traer el año y las fechas guardadas (esa carga es `GET /api/v1/anios-escolares/{id}`).
16. Cambia la fecha de finalización y pulsa **Guardar cambios**. La tarjeta debe mostrar la fecha nueva.
17. Crea otro año con el mismo número: debe mostrarse «Ya existe un año escolar registrado para …» y no enviarlo.
18. En el año en planeación, marca **Este es el año escolar actual** sin cambiar el estado a Activo: debe mostrarse «Solo un año escolar en estado ACTIVO puede ser marcado como actual». Si lo pasas a Activo y lo marcas como actual, la tarjeta debe quedar en Activo y año actual Sí. Si ya había otro año actual, debe mostrarse «Ya existe otro año escolar marcado como actual».
19. **Activar año** y **Cerrar año** también actualizan el año (`PUT`). Con un año ya activo, abre otro en planeación y pulsa **Activar**: debe mostrarse «Ya existe otro año escolar marcado como actual» y el año actual no cambia. Cerrar un año activo lo deja en Cerrado y ya no se puede modificar.
20. Abre **Plan de estudios** (o entra a `/dashboard/plan-estudios`). Elige un año y un grado que ya existan.
21. Pulsa **+ Agregar asignatura**. Elige una asignatura, escribe las horas (por ejemplo 4) y una observación. Puedes dejar el turno en **Sin asignar**. Pulsa **Agregar al plan**. La fila debe aparecer con el área de esa asignatura, esas horas y la observación. Si no elegiste turno, la columna queda en «—».
22. Pulsa **Editar**. El formulario debe traer la asignatura, las horas y la observación (esa carga es `GET /api/v1/planes-estudio/{id}`).
23. Cambia las horas y la observación y pulsa **Guardar cambios**. La tabla debe mostrar los valores nuevos.
24. Intenta agregar la misma asignatura al mismo año y grado: debe mostrarse «La asignatura ya está registrada para ese grado en el año escolar indicado» y no enviarla.
25. En **Año lectivo y grupos**, elige un año que ya tenga un grado (por ejemplo 2026 y Décimo).
26. Pulsa **+ Agregar grupo**. Elige el grado: el código se llena solo con el siguiente disponible; también puedes cambiarlo. Elige la sede, una cantidad de estudiantes (por ejemplo 20) y el estado **Activo**. Deja el aula en **Sin asignar** o elige un aula activa de la sede. Pulsa **Guardar grupo**. La fila debe aparecer con ese código, el grado, la sede y esos estudiantes.
27. Pulsa **Editar**. El formulario debe traer el código, el grado, la sede, los estudiantes y el estado (esa carga es `GET /api/v1/grupos/{id}`).
28. Cambia la cantidad de estudiantes (por ejemplo a 28) y pulsa **Guardar cambios**. La fila debe mostrar la cantidad nueva.
29. Crea otro grupo con otro código (por ejemplo 1002). Ábrelo en **Editar** y cambia el código al del primero: debe mostrarse «Ya existe otro grupo con ese código en el año escolar indicado» y no enviarlo.
30. Pulsa otra vez **+ Agregar grupo** y elige el mismo grado. El código debe avanzar al siguiente libre (con 1001, 1002 y 1003 ya creados en Décimo, debe sugerir 1004) y debe verse «Sugerido para este grado y año. Puedes cambiarlo.» Esa carga es `GET /api/v1/grupos/siguiente-codigo`.
31. Abre **Editar** en un grupo ya guardado. El código debe seguir siendo el del grupo y no reemplazarse por una sugerencia.
32. Abre **Aulas** en el menú (o entra a `/dashboard/aulas`). Elige una sede. La tabla es `GET /api/v1/aulas?sedeId=`.
33. Pulsa **+ Nueva aula**. Escribe un nombre (por ejemplo A-101), elige el tipo **Aula**, una capacidad (por ejemplo 35) y pulsa **Guardar aula**. La fila debe aparecer como Activa.
34. Pulsa **Editar**. El formulario debe traer el nombre, el tipo, la sede, la capacidad y el estado (esa carga es `GET /api/v1/aulas/{id}`). Cambia la capacidad a 40 y pulsa **Guardar cambios**. La fila debe mostrar 40.
35. Intenta crear otra aula activa con el mismo nombre en la misma sede: debe mostrarse «Ya existe un aula activa con ese nombre en la sede indicada» y no enviarla.
36. Pulsa **Nueva versión** sin cambiar nombre, tipo ni sede: debe mostrarse «El cambio indicado no requiere una nueva versión del aula». Cambia el tipo a **Laboratorio** y pulsa **Registrar versión**. La anterior queda Inactiva y aparece una nueva Activa.
37. Pulsa **Eliminar** y confirma. Debe mostrarse «No se puede eliminar físicamente el aula porque el sistema no puede garantizar que nunca haya sido utilizada» y la fila sigue en la tabla.
38. En **Año lectivo y grupos**, abre **+ Agregar grupo**. El aula habitual debe listar las aulas activas de la sede (por ejemplo «A-101 · Laboratorio») y ya no las opciones fijas A-101, A-102 y B-201.
39. Abre **Turnos** en el menú (o entra a `/dashboard/turnos`). La tabla es `GET /api/v1/turnos`.
40. Pulsa **+ Nuevo turno**. Escribe **Mañana**, hora de inicio **07:00**, hora de finalización **12:30**, duración de clase **55**, clases por día **6**, clases antes del descanso **3** y descanso **30**. Pulsa **Guardar turno**. La fila debe aparecer con ese horario.
41. Pulsa **Editar**. El formulario debe traer el nombre, las horas y la estructura (esa carga es `GET /api/v1/turnos/{id}`). Cambia la hora de finalización a **13:00** y pulsa **Guardar cambios**. La fila debe mostrar 07:00 – 13:00.
42. Intenta crear otro turno llamado **mañana**: debe mostrarse «Ya existe un turno con ese nombre» y no enviarlo. Si la hora de inicio no es anterior a la de finalización, debe mostrarse «La hora de inicio debe ser anterior a la hora de finalización». Si las clases antes del descanso superan las del día, debe mostrarse «Las clases antes del descanso no pueden superar las clases por día».
43. En **Plan de estudios**, abre **Editar** en una asignatura (por ejemplo Álgebra de 2026 · Décimo). El campo **Turno** debe listar **Mañana**. Elígela y pulsa **Guardar cambios**. La fila debe pasar de «—» a **Mañana**.
44. Abre **Franjas horarias** (o entra a `/dashboard/franjas`). Elige el turno **Mañana**. La tabla es `GET /api/v1/franjas-horarias?turnoId=`.
45. Pulsa **+ Nueva franja**. El formulario propone la primera clase: número **1**, orden **1**, **07:00** a **07:55** y **1** hora académica. Pulsa **Guardar franja**. La fila debe aparecer como Académica.
46. Pulsa **Editar**. El formulario debe traer el turno, el número, el orden y el horario (esa carga es `GET /api/v1/franjas-horarias/{id}`). Cambia el número a **2** y pulsa **Guardar cambios**. La fila debe mostrar Clase 2.
47. Abre otra franja y repite el número **2** o el orden **1**: debe mostrarse «Ya existe una franja académica con ese número en el turno» o «Ya existe una franja con ese orden en el turno», y no enviarla. Si la clase no dura 55 min, debe mostrarse «La duración de la franja académica debe coincidir con la configurada en el turno». Si el horario se cruza con otra franja, debe mostrarse «La franja horaria se solapa con otra franja del mismo turno». Si la hora de inicio no es anterior a la de finalización, debe mostrarse «La hora de inicio debe ser anterior a la hora de finalización».
48. Marca **Esta franja es un descanso**. El número queda en **0**, las horas en **0** y la duración pasa a 30 min. Con horario **07:55** a **08:25** y orden **2**, pulsa **Guardar franja**. La fila debe aparecer como Descanso. Al editarla, el descanso debe venir marcado.
49. Abre **Áreas y asignaturas** y pulsa **+ Nueva área**. **Área del catálogo** debe listar las 8 áreas fijas (esa carga es `GET /api/v1/catalogos/areas`). Elige **Matemáticas (MAT)**: el nombre y el código se llenan solos. Como el área ya existe, debe mostrarse «Ya existe un área con este código.» y no hay que guardarla. Pulsa **Cancelar**.
50. Selecciona el área **Matemáticas** y pulsa **+ Nueva asignatura**. **Asignatura del catálogo** debe listar solo las de esa área: Matemáticas Generales, Geometría y Estadística (esa carga es `GET /api/v1/catalogos/asignaturas?area=MAT`). Elige **Geometría**: el nombre queda en Geometría, el código en **MAT-GEO** y la abreviatura en **GEOM**. No la guardes, porque ya existe. Pulsa **Cancelar**.
51. Abre **Docentes** y entra a **Registrar docente** (`/dashboard/docentes/registrar`). **Régimen docente** debe listar los cinco regímenes (esa carga es `GET /api/v1/catalogos/regimenes-docentes`). Elige **Decreto Ley 2277 de 1979**: **Escalafón** lista Grado 1 a Grado 14. Cámbialo a **Decreto Ley 1278 de 2002**: la lista pasa a Grado 1–3 con niveles A–D (1A, 1B, …, 3D) y el escalafón anterior se limpia. Con **Régimen especial indígena - SEIP** la lista pasa a Nivel I–VI. Con **Sin escalafón / No aplica** o con el régimen etnoeducador, Escalafón queda deshabilitado en «Sin escalafón» y el aviso dice «Este régimen no asigna un escalafón.» Esas listas son `GET /api/v1/catalogos/escalafones?regimen=`.
52. El régimen solo arma las opciones: al guardar un docente se persiste el escalafón elegido, no el régimen. Esta prueba no crea un docente.

Los otros endpoints marcados se ven en pantallas que ya existían: el listado de grados, años y sedes en **Año lectivo y grupos** y en el formulario de grupo; el listado del plan en **Plan de estudios**; la creación y la edición de un grupo en **Año lectivo y grupos**.
