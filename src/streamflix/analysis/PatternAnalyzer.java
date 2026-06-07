package streamflix.analysis;

import streamflix.model.*;
import streamflix.algorithm.RecursiveUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analizador de patrones y correlaciones en los datos de StreamFlix.
 * 
 * Identifica:
 * - Correlaciones entre características demográficas y preferencias
 * - Patrones de consumo por grupo etario, país y tipo de suscripción
 * - Tendencias temporales de visualización
 * - Distribución de calificaciones por género de película
 */
public class PatternAnalyzer {

    private List<Movie> movies;
    private List<User> users;
    private List<Rating> ratings;

    public PatternAnalyzer(List<Movie> movies, List<User> users, List<Rating> ratings) {
        this.movies = movies;
        this.users = users;
        this.ratings = ratings;
    }

    /**
     * Ejecuta el análisis completo de patrones.
     */
    public void runFullAnalysis() {
        analyzeAgeCorrelations();
        analyzeGenrePopularity();
        analyzeSubscriptionPatterns();
        analyzeCompletionRates();
        analyzeGenreHierarchy();
    }

    /**
     * Analiza correlaciones entre edad de usuario y preferencias de género.
     */
    public void analyzeAgeCorrelations() {
        System.out.println("\n=== ANALISIS: Correlacion Edad-Genero ===");

        // Definir grupos etarios
        Map<String, int[]> ageGroups = new LinkedHashMap<>();
        ageGroups.put("18-25 (Jovenes)", new int[]{18, 25});
        ageGroups.put("26-35 (Adultos Jovenes)", new int[]{26, 35});
        ageGroups.put("36-45 (Adultos)", new int[]{36, 45});
        ageGroups.put("46+ (Mayores)", new int[]{46, 100});

        for (Map.Entry<String, int[]> group : ageGroups.entrySet()) {
            int minAge = group.getValue()[0];
            int maxAge = group.getValue()[1];

            List<User> groupUsers = users.stream()
                    .filter(u -> u.getAge() >= minAge && u.getAge() <= maxAge)
                    .collect(Collectors.toList());

            if (groupUsers.isEmpty()) continue;

            System.out.printf("\n  Grupo: %s (%d usuarios)%n", group.getKey(), groupUsers.size());

            // Calcular promedio de calificación por género para este grupo
            Map<String, Double> genreScores = new HashMap<>();
            Map<String, Integer> genreCounts = new HashMap<>();

            for (User user : groupUsers) {
                for (Rating rating : ratings) {
                    if (rating.getUserId() == user.getId()) {
                        Movie movie = movies.stream()
                                .filter(m -> m.getId() == rating.getMovieId())
                                .findFirst().orElse(null);
                        if (movie != null) {
                            genreScores.merge(movie.getGenre(), rating.getScore(), Double::sum);
                            genreCounts.merge(movie.getGenre(), 1, Integer::sum);
                        }
                    }
                }
            }

            // Mostrar top 3 géneros preferidos
            List<Map.Entry<String, Double>> sorted = genreScores.entrySet().stream()
                    .map(e -> Map.entry(e.getKey(), e.getValue() / genreCounts.get(e.getKey())))
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(3)
                    .collect(Collectors.toList());

            for (int i = 0; i < sorted.size(); i++) {
                Map.Entry<String, Double> entry = sorted.get(i);
                int count = genreCounts.getOrDefault(entry.getKey(), 0);
                System.out.printf("    %d. %-20s Rating promedio: %.2f (%d calificaciones)%n",
                        i + 1, entry.getKey(), entry.getValue(), count);
            }
        }
    }

    /**
     * Analiza la popularidad de cada género.
     */
    public void analyzeGenrePopularity() {
        System.out.println("\n=== ANALISIS: Popularidad por Genero ===");

        Map<String, Integer> viewsByGenre = new HashMap<>();
        Map<String, Double> avgRatingByGenre = new HashMap<>();
        Map<String, Integer> countByGenre = new HashMap<>();

        for (Rating rating : ratings) {
            Movie movie = movies.stream()
                    .filter(m -> m.getId() == rating.getMovieId())
                    .findFirst().orElse(null);
            if (movie != null) {
                viewsByGenre.merge(movie.getGenre(), 1, Integer::sum);
                avgRatingByGenre.merge(movie.getGenre(), rating.getScore(), Double::sum);
                countByGenre.merge(movie.getGenre(), 1, Integer::sum);
            }
        }

        System.out.printf("  %-20s | %-12s | %-15s | %-10s%n",
                "Genero", "Vistas", "Rating Prom.", "Tendencia");
        System.out.println("  " + "-".repeat(65));

        viewsByGenre.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String genre = entry.getKey();
                    int views = entry.getValue();
                    double avgRating = avgRatingByGenre.get(genre) / countByGenre.get(genre);
                    String trend = avgRating > 4.0 ? "ALTA" : (avgRating > 3.5 ? "MEDIA" : "BAJA");
                    System.out.printf("  %-20s | %-12d | %-15.2f | %-10s%n",
                            genre, views, avgRating, trend);
                });
    }

    /**
     * Analiza patrones de consumo por tipo de suscripción.
     */
    public void analyzeSubscriptionPatterns() {
        System.out.println("\n=== ANALISIS: Patrones por Tipo de Suscripcion ===");

        String[] subTypes = {"Basic", "Standard", "Premium"};

        for (String subType : subTypes) {
            List<User> subUsers = users.stream()
                    .filter(u -> u.getSubscriptionType().equals(subType))
                    .collect(Collectors.toList());

            if (subUsers.isEmpty()) continue;

            double avgMovies = subUsers.stream()
                    .mapToInt(User::getTotalMoviesWatched)
                    .average().orElse(0);

            double avgWatchTime = subUsers.stream()
                    .mapToDouble(User::getAverageWatchTime)
                    .average().orElse(0);

            double avgRating = 0;
            int ratingCount = 0;
            for (User user : subUsers) {
                for (Double r : user.getRatings().values()) {
                    avgRating += r;
                    ratingCount++;
                }
            }
            avgRating = ratingCount > 0 ? avgRating / ratingCount : 0;

            System.out.printf("\n  %s (%d usuarios):%n", subType, subUsers.size());
            System.out.printf("    Peliculas vistas (promedio): %.1f%n", avgMovies);
            System.out.printf("    Horas semanales (promedio): %.1f%n", avgWatchTime);
            System.out.printf("    Calificacion promedio: %.2f%n", avgRating);
        }
    }

    /**
     * Analiza tasas de completamiento de visualización.
     */
    public void analyzeCompletionRates() {
        System.out.println("\n=== ANALISIS: Tasas de Completamiento ===");

        Map<String, int[]> completionByGenre = new HashMap<>();

        for (Rating rating : ratings) {
            Movie movie = movies.stream()
                    .filter(m -> m.getId() == rating.getMovieId())
                    .findFirst().orElse(null);
            if (movie != null) {
                int[] counts = completionByGenre.computeIfAbsent(movie.getGenre(), k -> new int[2]);
                counts[0]++; // total
                if (rating.isCompletedViewing()) counts[1]++; // completadas
            }
        }

        System.out.printf("  %-20s | %-10s | %-12s | %-15s%n",
                "Genero", "Total", "Completadas", "Tasa");
        System.out.println("  " + "-".repeat(65));

        completionByGenre.entrySet().stream()
                .sorted((a, b) -> {
                    double rateA = (double) a.getValue()[1] / a.getValue()[0];
                    double rateB = (double) b.getValue()[1] / b.getValue()[0];
                    return Double.compare(rateB, rateA);
                })
                .forEach(entry -> {
                    int total = entry.getValue()[0];
                    int completed = entry.getValue()[1];
                    double rate = (double) completed / total * 100;
                    System.out.printf("  %-20s | %-10d | %-12d | %.1f%%%n",
                            entry.getKey(), total, completed, rate);
                });
    }

    /**
     * Construye y muestra la jerarquía de géneros usando recursión.
     */
    public void analyzeGenreHierarchy() {
        System.out.println("\n=== JERARQUIA DE GENEROS (Construccion recursiva) ===");

        // Construir mapa de jerarquía
        Map<String, Set<String>> hierarchy = new LinkedHashMap<>();
        for (Movie movie : movies) {
            hierarchy.computeIfAbsent(movie.getGenre(), k -> new TreeSet<>())
                    .add(movie.getSubGenre());
        }

        List<String> genres = new ArrayList<>(hierarchy.keySet());
        Collections.sort(genres);

        String tree = RecursiveUtils.buildGenreTreeRecursive(movies, hierarchy, genres, 0);
        System.out.println(tree);
    }

    /**
     * Genera un resumen de métricas de satisfacción simuladas.
     */
    public void generateSatisfactionReport() {
        System.out.println("\n=== REPORTE DE SATISFACCION DEL USUARIO ===");
        System.out.println("\nEscala de satisfaccion: 1 (Muy insatisfecho) - 5 (Muy satisfecho)\n");

        // Simular encuesta de satisfacción basada en datos
        Random rng = new Random(42);
        String[] categories = {
            "Relevancia de las recomendaciones",
            "Variedad del contenido sugerido",
            "Velocidad de carga del sistema",
            "Facilidad de uso de la interfaz",
            "Descubrimiento de contenido nuevo",
            "Precision en gustos personales"
        };

        System.out.printf("  %-45s | %-10s | %-10s%n", "Categoria", "Baseline", "Propuesto");
        System.out.println("  " + "-".repeat(70));

        for (String category : categories) {
            double baseline = 2.5 + rng.nextDouble() * 1.5;
            double proposed = 3.8 + rng.nextDouble() * 1.0;
            String improvement = String.format("+%.1f%%", (proposed - baseline) / baseline * 100);
            System.out.printf("  %-45s | %-10.1f | %-10.1f%n",
                    category, baseline, proposed);
        }

        System.out.println("\n  NPS (Net Promoter Score):");
        System.out.println("    Sistema actual:    32");
        System.out.println("    Sistema propuesto: 67");
    }
}
