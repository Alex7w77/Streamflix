package streamflix.util;

import java.util.List;

/**
 * Utilidad de presentacion en consola para StreamFlix.
 *
 * <p>Centraliza el "look &amp; feel" de la salida por terminal: banners,
 * encabezados de seccion, tablas con bordes y colores ANSI. El objetivo es
 * producir una salida limpia y legible que facilite la lectura de resultados
 * durante la ejecucion del sistema de recomendacion.</p>
 *
 * <p>Se utilizan exclusivamente caracteres ASCII para el dibujo de cajas, de
 * modo que la salida se vea correctamente en cualquier terminal (incluida la
 * consola de Windows con codificacion cp1252), evitando los simbolos "?" que
 * aparecen al imprimir caracteres Unicode no soportados.</p>
 *
 * <p>Los colores ANSI se activan solo cuando la salida es una terminal
 * interactiva ({@code System.console() != null}) y la variable de entorno
 * {@code NO_COLOR} no esta definida; en caso contrario se omiten para no
 * ensuciar archivos o pipes con codigos de escape.</p>
 *
 * @author Equipo StreamFlix
 * @version 2.0
 */
public final class ConsoleUI {

    // ----- Paleta ANSI (se desactiva si no hay terminal interactiva) -----
    private static final boolean COLOR = (System.console() != null)
            && (System.getenv("NO_COLOR") == null);

    private static final String RESET  = COLOR ? "\u001B[0m"  : "";
    private static final String BOLD   = COLOR ? "\u001B[1m"  : "";
    private static final String DIM    = COLOR ? "\u001B[2m"  : "";
    private static final String CYAN   = COLOR ? "\u001B[36m" : "";
    private static final String BLUE   = COLOR ? "\u001B[34m" : "";
    private static final String GREEN  = COLOR ? "\u001B[32m" : "";
    private static final String YELLOW = COLOR ? "\u001B[33m" : "";
    private static final String MAGENTA= COLOR ? "\u001B[35m" : "";

    private static final int WIDTH = 78;

    private ConsoleUI() { /* clase de utilidades, no instanciable */ }

    /**
     * Imprime el banner principal de la aplicacion dentro de un marco.
     *
     * @param title    titulo central
     * @param subtitle subtitulo (puede ser {@code null} o vacio)
     */
    public static void banner(String title, String subtitle) {
        String top   = "+" + repeat("=", WIDTH - 2) + "+";
        String empty = "|" + repeat(" ", WIDTH - 2) + "|";
        System.out.println();
        System.out.println(CYAN + BOLD + top + RESET);
        System.out.println(CYAN + BOLD + empty + RESET);
        System.out.println(CYAN + BOLD + framed(title) + RESET);
        if (subtitle != null && !subtitle.isEmpty()) {
            System.out.println(CYAN + framed(subtitle) + RESET);
        }
        System.out.println(CYAN + BOLD + empty + RESET);
        System.out.println(CYAN + BOLD + top + RESET);
        System.out.println();
    }

    /**
     * Encabezado numerado de fase, p. ej. {@code "[ FASE 4 ] Filtrado Colaborativo"}.
     *
     * @param number numero de fase
     * @param title  titulo descriptivo
     */
    public static void section(int number, String title) {
        String label = "[ FASE " + number + " ] ";
        String head = label + title + " ";
        String line = repeat("=", Math.max(3, WIDTH - head.length()));
        System.out.println();
        System.out.println(BLUE + BOLD + label + RESET + BOLD + title + " " + RESET
                + DIM + BLUE + line + RESET);
    }

    /** Subtitulo discreto dentro de una seccion. */
    public static void subsection(String title) {
        System.out.println(MAGENTA + "  >> " + BOLD + title + RESET);
    }

    /** Linea de informacion clave: etiqueta + valor resaltado. */
    public static void keyValue(String label, String value) {
        System.out.printf("    %-34s %s%s%s%n", label, GREEN + BOLD, value, RESET);
    }

    /** Mensaje de exito. */
    public static void success(String msg) {
        System.out.println(GREEN + "  [OK] " + RESET + msg);
    }

    /** Mensaje informativo. */
    public static void info(String msg) {
        System.out.println(CYAN + "  - " + RESET + msg);
    }

    /** Nota / advertencia suave. */
    public static void note(String msg) {
        System.out.println(YELLOW + "  ! " + RESET + msg);
    }

    /**
     * Imprime una tabla con bordes ASCII, alineando cada columna al ancho de
     * su contenido mas largo.
     *
     * @param headers titulos de columna
     * @param rows    filas de datos (cada fila con el mismo numero de columnas)
     */
    public static void table(String[] headers, List<String[]> rows) {
        int cols = headers.length;
        int[] w = new int[cols];
        for (int c = 0; c < cols; c++) w[c] = headers[c].length();
        for (String[] r : rows)
            for (int c = 0; c < cols; c++)
                w[c] = Math.max(w[c], r[c] == null ? 0 : r[c].length());

        String sep = rule(w);
        System.out.println("    " + DIM + sep + RESET);
        System.out.println("    " + row(headers, w, true));
        System.out.println("    " + DIM + sep + RESET);
        for (String[] r : rows) System.out.println("    " + row(r, w, false));
        System.out.println("    " + DIM + sep + RESET);
    }

    // ----- helpers internos -----

    private static String row(String[] cells, int[] w, boolean header) {
        StringBuilder sb = new StringBuilder(DIM + "|" + RESET);
        for (int c = 0; c < w.length; c++) {
            String v = cells[c] == null ? "" : cells[c];
            String padded = " " + v + repeat(" ", w[c] - v.length()) + " ";
            sb.append(header ? (BOLD + CYAN + padded + RESET) : padded);
            sb.append(DIM + "|" + RESET);
        }
        return sb.toString();
    }

    private static String rule(int[] w) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : w) {
            sb.append(repeat("-", width + 2));
            sb.append("+");
        }
        return sb.toString();
    }

    /** Encuadra y centra un texto entre barras verticales al ancho del banner. */
    private static String framed(String text) {
        int inner = WIDTH - 2;
        if (text.length() > inner) text = text.substring(0, inner);
        int pad = (inner - text.length()) / 2;
        int rest = inner - text.length() - pad;
        return "|" + repeat(" ", pad) + text + repeat(" ", rest) + "|";
    }

    private static String repeat(String s, int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }
}
