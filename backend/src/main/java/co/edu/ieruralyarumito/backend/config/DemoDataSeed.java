package co.edu.ieruralyarumito.backend.config;

import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.RolUsuario;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Carga datos de demostración solo cuando APP_SEED_DEMO=true.
@Component
@Order(2)
@ConditionalOnProperty(name = "app.seed.demo", havingValue = "true")
public class DemoDataSeed implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DemoDataSeed.class);

    private final UsuarioRepository usuarioRepository;
    private final AreaRepository areaRepository;
    private final DocenteRepository docenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfigurableApplicationContext applicationContext;
    private final String rectorEmail;
    private final String rectorPassword;
    private final boolean exitAfter;

    public DemoDataSeed(
            UsuarioRepository usuarioRepository,
            AreaRepository areaRepository,
            DocenteRepository docenteRepository,
            PasswordEncoder passwordEncoder,
            ConfigurableApplicationContext applicationContext,
            @Value("${app.seed.email}") String rectorEmail,
            @Value("${app.seed.password}") String rectorPassword,
            @Value("${app.seed.exit-after:false}") boolean exitAfter) {
        this.usuarioRepository = usuarioRepository;
        this.areaRepository = areaRepository;
        this.docenteRepository = docenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationContext = applicationContext;
        this.rectorEmail = rectorEmail;
        this.rectorPassword = rectorPassword;
        this.exitAfter = exitAfter;
    }

    @Override
    public void run(ApplicationArguments args) {
        asegurarRector();
        List<Area> areas = asegurarAreas();
        int creados = sembrarDocentes(areas);

        logger.info(
                "Seed de demostración listo. Áreas: {}. Docentes nuevos: {}.",
                areas.size(),
                creados);

        if (exitAfter) {
            int code = SpringApplication.exit(applicationContext, () -> 0);
            System.exit(code);
        }
    }

    private void asegurarRector() {
        if (usuarioRepository.existsByCorreo(rectorEmail)) {
            return;
        }

        Usuario rector = new Usuario();
        rector.setCorreo(rectorEmail);
        rector.setContrasenaHash(passwordEncoder.encode(rectorPassword));
        rector.setRol(RolUsuario.RECTOR);
        rector.setActivo(true);
        usuarioRepository.save(rector);
    }

    private List<Area> asegurarAreas() {
        return List.of(
                area("MAT", "Matemáticas", true, false),
                area("LC", "Lengua Castellana", true, false),
                area("CN", "Ciencias Naturales", true, false),
                area("CS", "Ciencias Sociales", true, false),
                area("ING", "Inglés", true, false),
                area("EF", "Educación Física", true, false)
        );
    }

    private Area area(
            String codigo,
            String nombre,
            boolean obligatoria,
            boolean soloMedia) {
        return areaRepository.findByCodigo(codigo).orElseGet(() -> {
            Area nueva = new Area();
            nueva.setCodigo(codigo);
            nueva.setNombre(nombre);
            nueva.setObligatoria(obligatoria);
            nueva.setSoloMedia(soloMedia);
            nueva.setActiva(true);
            return areaRepository.save(nueva);
        });
    }

    private int sembrarDocentes(List<Area> areas) {
        Area matematicas = areas.get(0);
        Area lengua = areas.get(1);
        Area naturales = areas.get(2);
        Area sociales = areas.get(3);
        Area ingles = areas.get(4);
        Area educacionFisica = areas.get(5);

        List<Docente> semilla = List.of(
                docente("Carlos Andrés", "Pérez Gómez", "1234567890",
                        "carlos.perez@yarumito.edu.co", "3001234567",
                        TipoVinculacion.PLANTA, matematicas, EstadoDocente.ACTIVO,
                        "12344", LocalDate.of(2018, 2, 12), "2A",
                        22, 10, false, LocalDate.of(2018, 3, 1)),
                docente("Carlos Andrés", "López", "1032456781",
                        "carlos.lopez@yarumito.edu.co", "3014567901",
                        TipoVinculacion.PROVISIONAL, lengua, EstadoDocente.ACTIVO,
                        "11890", LocalDate.of(2021, 1, 20), "1B",
                        20, 8, false, LocalDate.of(2021, 2, 1)),
                docente("Dianara Marcela", "Ruíz", "1023578944",
                        "dianara.ruiz@yarumito.edu.co", "3104569820",
                        TipoVinculacion.PLANTA, naturales, EstadoDocente.ACTIVO,
                        "13002", LocalDate.of(2017, 8, 5), "3A",
                        22, 10, false, LocalDate.of(2017, 9, 1)),
                docente("Jorge Hernán", "Torres", "1012345678",
                        "jorge.torres@yarumito.edu.co", "3157892340",
                        TipoVinculacion.CONTRATO, sociales, EstadoDocente.RETIRADO,
                        "10911", LocalDate.of(2015, 4, 18), "2B",
                        16, 4, false, LocalDate.of(2015, 5, 1)),
                docente("Laura Sofía", "Martínez", "1009876543",
                        "laura.martinez@yarumito.edu.co", "3206541188",
                        TipoVinculacion.PROVISIONAL, ingles, EstadoDocente.ACTIVO,
                        "14120", LocalDate.of(2022, 7, 11), "1A",
                        18, 6, false, LocalDate.of(2022, 8, 1)),
                docente("Miguel Ángel", "Pérez", "9876543210",
                        "miguel.perez@yarumito.edu.co", "3009876543",
                        TipoVinculacion.PLANTA, educacionFisica, EstadoDocente.RETIRADO,
                        "10045", LocalDate.of(2014, 3, 9), "3B",
                        22, 10, false, LocalDate.of(2014, 4, 1)),
                docente("Ana María", "Gómez", "1098765432",
                        "ana.gomez@yarumito.edu.co", "3112223344",
                        TipoVinculacion.PLANTA, matematicas, EstadoDocente.ACTIVO,
                        "15003", LocalDate.of(2019, 6, 14), "2A",
                        22, 8, false, LocalDate.of(2019, 7, 1)),
                docente("Pedro Luis", "Ramírez", "1087654321",
                        "pedro.ramirez@yarumito.edu.co", "3123334455",
                        TipoVinculacion.PLANTA, lengua, EstadoDocente.LICENCIA,
                        "12770", LocalDate.of(2016, 11, 2), "2C",
                        22, 10, false, LocalDate.of(2016, 12, 1)),
                docente("Camila Andrea", "Soto", "1076543210",
                        "camila.soto@yarumito.edu.co", "3134445566",
                        TipoVinculacion.PROVISIONAL, naturales, EstadoDocente.ACTIVO,
                        "16088", LocalDate.of(2023, 1, 16), "1A",
                        16, 4, true, LocalDate.of(2023, 2, 1)),
                docente("Andrés Felipe", "Muñoz", "1065432109",
                        "andres.munoz@yarumito.edu.co", "3145556677",
                        TipoVinculacion.PLANTA, ingles, EstadoDocente.ACTIVO,
                        "13321", LocalDate.of(2018, 9, 7), "3A",
                        22, 10, false, LocalDate.of(2018, 10, 1)),
                docente("Valentina", "Ríos", "1054321098",
                        "valentina.rios@yarumito.edu.co", "3156667788",
                        TipoVinculacion.CONTRATO, educacionFisica, EstadoDocente.ACTIVO,
                        "17102", LocalDate.of(2024, 2, 21), "1B",
                        12, 4, false, LocalDate.of(2024, 3, 1)),
                docente("Sebastián", "Hoyos", "1043210987",
                        "sebastian.hoyos@yarumito.edu.co", "3167778899",
                        TipoVinculacion.PLANTA, sociales, EstadoDocente.LICENCIA,
                        "11954", LocalDate.of(2017, 5, 30), "2A",
                        22, 10, false, LocalDate.of(2017, 6, 15)),
                docente("Mariana", "López Cano", "1032109876",
                        "mariana.lopez@yarumito.edu.co", "3178889900",
                        TipoVinculacion.PROVISIONAL, matematicas, EstadoDocente.ACTIVO,
                        "18230", LocalDate.of(2020, 8, 19), "1C",
                        20, 6, false, LocalDate.of(2020, 9, 1)),
                docente("Julián David", "Restrepo", "1021098765",
                        "julian.restrepo@yarumito.edu.co", "3189990011",
                        TipoVinculacion.PLANTA, lengua, EstadoDocente.ACTIVO,
                        "12567", LocalDate.of(2019, 3, 4), "2B",
                        22, 8, false, LocalDate.of(2019, 4, 1)),
                docente("Paola Andrea", "Castaño", "1010987654",
                        "paola.castano@yarumito.edu.co", "3191112233",
                        TipoVinculacion.CONTRATO, naturales, EstadoDocente.ACTIVO,
                        "19001", LocalDate.of(2024, 7, 8), "1A",
                        14, 4, true, LocalDate.of(2024, 8, 1))
        );

        int creados = 0;
        for (Docente docente : semilla) {
            if (docenteRepository.existsByNumeroDocumento(docente.getNumeroDocumento())) {
                continue;
            }
            docenteRepository.save(docente);
            creados++;
        }
        return creados;
    }

    private Docente docente(
            String nombres,
            String apellidos,
            String numeroDocumento,
            String correo,
            String telefono,
            TipoVinculacion vinculacion,
            Area area,
            EstadoDocente estado,
            String decreto,
            LocalDate fechaDecreto,
            String escalafon,
            int horas,
            int extra,
            boolean exclusivoMedia,
            LocalDate fechaVinculacion) {
        Docente docente = new Docente();
        docente.setNombres(nombres);
        docente.setApellidos(apellidos);
        docente.setTipoDocumento("CC");
        docente.setNumeroDocumento(numeroDocumento);
        docente.setCorreoInstitucional(correo);
        docente.setTelefono(telefono);
        docente.setTipoVinculacion(vinculacion);
        docente.setAreaNombramiento(area);
        docente.setEstado(estado);
        docente.setNumeroDecreto(decreto);
        docente.setFechaDecreto(fechaDecreto);
        docente.setEscalafon(escalafon);
        docente.setHorasSemanalesContratadas(horas);
        docente.setMaxHorasExtra(extra);
        docente.setEsExclusivoMediaTecnica(exclusivoMedia);
        docente.setFechaVinculacion(fechaVinculacion);
        return docente;
    }
}
