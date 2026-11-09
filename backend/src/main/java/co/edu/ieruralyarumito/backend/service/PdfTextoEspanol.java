package co.edu.ieruralyarumito.backend.service;

import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.text.Normalizer;

/**
 * HU-04 / SCRUM-31.
 *
 * Prepara texto institucional para las fuentes PDF.
 *
 * Conserva los caracteres españoles admitidos por
 * la fuente: vocales acentuadas, eñe, diéresis, etc.
 *
 * No elimina tildes ni altera deliberadamente
 * nombres propios o denominaciones académicas.
 */
public final class PdfTextoEspanol {

    private PdfTextoEspanol() {
        // Clase de utilidad.
    }

    /**
     * Normaliza a Unicode NFC y verifica que todos
     * los caracteres puedan escribirse con la fuente.
     *
     * @throws IOException si la fuente no puede
     * representar algún carácter del texto.
     */
    public static String preparar(
            String texto,
            PDFont fuente) throws IOException {

        if (texto == null) {
            return "";
        }

        if (fuente == null) {
            throw new IllegalArgumentException(
                    "La fuente PDF es obligatoria"
            );
        }

        String normalizado = Normalizer.normalize(
                texto,
                Normalizer.Form.NFC
        );

        StringBuilder resultado = new StringBuilder();

        for (int indice = 0;
             indice < normalizado.length();) {

            int codigo = normalizado.codePointAt(indice);
            indice += Character.charCount(codigo);

            // Evita caracteres de control en el texto PDF.
            if (Character.isISOControl(codigo)) {
                resultado.append(' ');
                continue;
            }

            String caracter = new String(
                    Character.toChars(codigo)
            );

            try {
                fuente.encode(caracter);
            } catch (IllegalArgumentException e) {
                throw new IOException(
                        "La fuente PDF no admite el caracter "
                                + "Unicode U+"
                                + Integer.toHexString(codigo)
                                .toUpperCase(),
                        e
                );
            }

            resultado.append(caracter);
        }

        return resultado.toString();
    }
}
