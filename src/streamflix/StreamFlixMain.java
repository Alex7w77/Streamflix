package streamflix;

import streamflix.data.DataGenerator;
import streamflix.model.*;
import streamflix.algorithm.*;
import streamflix.validation.CrossValidator;
import streamflix.analysis.PatternAnalyzer;
import streamflix.util.ConsoleUI;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ===============================================================================
 *  STREAMFLIX - Sistema de Recomendacion de Peliculas con Inteligencia Artificial
 * ===============================================================================
 * 
 * Clase principal que demuestra el funcionamiento completo del sistema de
 * recomendacion personalizado para la plataforma StreamFlix.
 * 
 * El sistema integra:
 * 1. Filtrado Colaborativo (similitud de coseno entre usuarios)
 * 2. Arbol de Decision (clasificacion por perfil de usuario)
 * 3. Filtrado Basado en Contenido (similitud entre peliculas)
 * 4. Sistema Hibrido (combinacion ponderada de los tres metodos)
 * 5. Funciones Recursivas (merge sort, busqueda binaria, filtrado, recorrido de arbol)
 * 6. Validacion Cruzada (K-Fold Cross Validation)
 * 7. Analisis de Patrones (correlaciones y comportamientos)
 * 
 * @author StreamFlix AI Team
 * @version 2.0
 */
public class StreamFlixMain {

    public static void main(String[] args) {
        ConsoleUI.banner("STREAMFLIX  -  Sistema de Recomendacion con IA",
                "Trabajo Final - Inteligencia Artificial en Java");

        // =====================================================================
        // FASE 1: GENERACION DE DATOS
        // =====================================================================
        ConsoleUI.section(1, "Generacion de Datos Ficticios");

        DataGenerator dataGen = new DataGenerator();
        dataGen.generateAll();
        dataGen.printStats();

        List<Movie> movies = dataGen.getMovies();
        List<User> users = dataGen.getUsers();
        List<Rating> ratings = dataGen.getRatings();

        // Mostrar muestra de datos
        System.out.println("\n--- Muestra de Peliculas ---");
        for (int i = 0; i < 5; i++) {
            Movie m = movies.get(i);
            System.out.printf("  [%d] '%s' (%s/%s, %d) - Rating: %.1f - Tags: %s%n",
                    m.getId(), m.getTitle(), m.getGenre(), m.getSubGenre(),
                    m.getYear(), m.getAverageRating(), m.getTags());
        }

        System.out.println("\n--- Muestra de Usuarios ---");
        for (int i = 0; i < 5; i++) {
            User u = users.get(i);
            System.out.printf("  [%d] %s (Edad: %d, %s, %s) - Generos: %s - Vistas: %d%n",
                    u.getId(), u.getName(), u.getAge(), u.getCountry(), u.getSubscriptionType(),
                    u.getPreferredGenres(), u.getTotalMoviesWatched());
        }

        // =====================================================================
        // FASE 2: ANALISIS DE PATRONES Y CORRELACIONES
        // =====================================================================
        ConsoleUI.section(2, "Analisis de Patrones y Correlaciones");

        PatternAnalyzer analyzer = new PatternAnalyzer(movies, users, ratings);
        analyzer.runFullAnalysis();

        // =====================================================================
        // FASE 3: FILTRADO COLABORATIVO
        // =====================================================================
        ConsoleUI.section(3, "Filtrado Colaborativo (User-Based)");

        CollaborativeFilteringRecommender cfRecommender =
                new CollaborativeFilteringRecommender(movies, users, ratings, 5);

        // Mostrar matriz de similitud
        cfRecommender.printSimilarityMatrix();

        // Generar recomendaciones para usuario de ejemplo
        int targetUserId = 1;
        User targetUser = users.get(targetUserId - 1);
        System.out.printf("\nRecomendaciones para: %s (ID: %d)%n", targetUser.getName(), targetUserId);
        System.out.printf("Generos preferidos: %s%n", targetUser.getPreferredGenres());
        System.out.println("Vecinos mas similares:");

        List<Map.Entry<Integer, Double>> neighbors = cfRecommender.findNearestNeighbors(targetUserId);
        for (Map.Entry<Integer, Double> neighbor : neighbors) {
            User n = users.get(neighbor.getKey() - 1);
            System.out.printf("  - %s (Similitud: %.4f, Generos: %s)%n",
                    n.getName(), neighbor.getValue(), n.getPreferredGenres());
        }

        List<Recommendation> cfRecs = cfRecommender.recommend(targetUserId, 5);
        System.out.println("\nTop 5 recomendaciones (Filtrado Colaborativo):");
        for (Recommendation rec : cfRecs) {
            System.out.println(rec);
        }

        // =====================================================================
        // FASE 4: ARBOL DE DECISION
        // =====================================================================
        ConsoleUI.section(4, "Arbol de Decision");

        DecisionTreeRecommender dtRecommender =
                new DecisionTreeRecommender(movies, users, ratings, 4, 3);
        dtRecommender.buildTree();
        dtRecommender.printTree();

        // Clasificar algunos usuarios
        System.out.println("\nClasificacion de usuarios por el arbol:");
        for (int i = 0; i < 5; i++) {
            User user = users.get(i);
            List<String> genres = dtRecommender.classify(user);
            System.out.printf("  %s (Edad: %d, Horas/sem: %.1f) -> Generos: %s%n",
                    user.getName(), user.getAge(), user.getAverageWatchTime(), genres);
        }

        List<Recommendation> dtRecs = dtRecommender.recommend(targetUser, 5);
        System.out.println("\nTop 5 recomendaciones (Arbol de Decision) para " + targetUser.getName() + ":");
        for (Recommendation rec : dtRecs) {
            System.out.println(rec);
        }

        // =====================================================================
        // FASE 5: FILTRADO BASADO EN CONTENIDO
        // =====================================================================
        ConsoleUI.section(5, "Filtrado Basado en Contenido");

        ContentBasedFilter cbFilter = new ContentBasedFilter(movies, users, ratings);

        // Mostrar perfil del usuario
        Map<String, Double> userProfile = cbFilter.buildUserProfile(targetUser);
        System.out.printf("\nPerfil de preferencias de %s:%n", targetUser.getName());
        userProfile.entrySet().stream()
                .filter(e -> !e.getKey().startsWith("tag:"))
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> System.out.printf("  %-20s: %.2f%n", e.getKey(), e.getValue()));

        List<Recommendation> cbRecs = cbFilter.recommend(targetUser, 5);
        System.out.println("\nTop 5 recomendaciones (Basado en Contenido):");
        for (Recommendation rec : cbRecs) {
            System.out.println(rec);
        }

        // =====================================================================
        // FASE 6: SISTEMA HIBRIDO
        // =====================================================================
        ConsoleUI.section(6, "Sistema Hibrido de Recomendacion");

        HybridRecommender hybrid = new HybridRecommender(movies, users, ratings);

        // Comparar todos los métodos
        System.out.println("\n--- Comparacion de Metodos para " + targetUser.getName() + " ---");
        Map<String, List<Recommendation>> allRecs = hybrid.getIndividualRecommendations(targetUserId, 5);

        for (Map.Entry<String, List<Recommendation>> entry : allRecs.entrySet()) {
            System.out.printf("\n[%s]%n", entry.getKey());
            for (Recommendation rec : entry.getValue()) {
                System.out.println(rec);
            }
        }

        // Recomendaciones para múltiples usuarios
        System.out.println("\n--- Recomendaciones Hibridas para Diferentes Usuarios ---");
        int[] sampleUsers = {1, 5, 10, 15, 20};
        for (int uid : sampleUsers) {
            User user = users.get(uid - 1);
            List<Recommendation> recs = hybrid.recommend(uid, 3);
            System.out.printf("\n%s (Edad: %d, %s):%n", user.getName(), user.getAge(), user.getSubscriptionType());
            for (Recommendation rec : recs) {
                System.out.println(rec);
            }
        }

        // =====================================================================
        // FASE 7: FUNCIONES RECURSIVAS
        // =====================================================================
        ConsoleUI.section(7, "Demostracion de Funciones Recursivas");

        // 7.1 Búsqueda binaria recursiva
        System.out.println("\n--- Busqueda Binaria Recursiva ---");
        List<Movie> sortedMovies = new ArrayList<>(movies);
        sortedMovies.sort(Comparator.comparingDouble(Movie::getAverageRating));

        double targetRating = 4.3;
        int foundIndex = RecursiveUtils.binarySearchByRating(sortedMovies, targetRating, 0, sortedMovies.size() - 1);
        Movie foundMovie = sortedMovies.get(foundIndex);
        System.out.printf("Buscando rating %.1f -> Encontrado: '%s' (Rating: %.1f)%n",
                targetRating, foundMovie.getTitle(), foundMovie.getAverageRating());

        // 7.2 Merge Sort recursivo
        System.out.println("\n--- Merge Sort Recursivo de Recomendaciones ---");
        List<Recommendation> unsorted = new ArrayList<>(hybrid.recommend(1, 10));
        Collections.shuffle(unsorted);
        System.out.println("Antes del merge sort (desordenadas):");
        unsorted.subList(0, 3).forEach(r -> System.out.printf("  %.2f - %s%n",
                r.getPredictedScore(), r.getMovie().getTitle()));
        System.out.println("  ...");

        List<Recommendation> sorted = RecursiveUtils.mergeSortRecommendations(unsorted);
        System.out.println("Despues del merge sort (ordenadas):");
        sorted.subList(0, 3).forEach(r -> System.out.printf("  %.2f - %s%n",
                r.getPredictedScore(), r.getMovie().getTitle()));
        System.out.println("  ...");

        // 7.3 Filtrado recursivo multicriteria
        System.out.println("\n--- Filtrado Recursivo Multicriteria ---");
        Object[] criteria = {"Ciencia Ficcion", 2023, 4.0, 160};
        List<Movie> filtered = RecursiveUtils.recursiveFilter(movies, criteria, 0);
        System.out.printf("Filtro: Genero='Ciencia Ficcion', Ano>=2023, Rating>=4.0, Duracion<=160%n");
        System.out.printf("Resultado: %d peliculas encontradas%n", filtered.size());
        for (Movie m : filtered) {
            System.out.printf("  - '%s' (%d, Rating: %.1f, %d min)%n",
                    m.getTitle(), m.getYear(), m.getAverageRating(), m.getDurationMinutes());
        }

        // 7.4 Promedio ponderado recursivo
        System.out.println("\n--- Promedio Ponderado Recursivo ---");
        List<Rating> userRatings = ratings.stream()
                .filter(r -> r.getUserId() == 1)
                .sorted(Comparator.comparingLong(Rating::getTimestamp).reversed())
                .limit(10)
                .collect(Collectors.toList());

        double weightedAvg = RecursiveUtils.weightedAverageRecursive(userRatings, 0, 0.7);
        double simpleAvg = userRatings.stream().mapToDouble(Rating::getScore).average().orElse(0);
        System.out.printf("Promedio simple de calificaciones: %.2f%n", simpleAvg);
        System.out.printf("Promedio ponderado (peso a recientes): %.2f%n", weightedAvg);

        // =====================================================================
        // FASE 8: VALIDACION CRUZADA
        // =====================================================================
        ConsoleUI.section(8, "Validacion Cruzada y Evaluacion del Modelo");

        CrossValidator validator = new CrossValidator(movies, users, ratings, 5);
        validator.compareWithBaseline();

        // =====================================================================
        // FASE 9: REPORTE DE SATISFACCION
        // =====================================================================
        ConsoleUI.section(9, "Metricas de Satisfaccion");

        analyzer.generateSatisfactionReport();

        // =====================================================================
        // FASE 10: INFRAESTRUCTURA Y COSTOS
        // =====================================================================
        ConsoleUI.section(10, "Infraestructura Tecnologica y Costos");

        printInfrastructureReport();

        // =====================================================================
        // FASE 11: CASOS DE EXITO
        // =====================================================================
        ConsoleUI.section(11, "Casos de Exito en La Industria");

        printSuccessCases();

        // Fin
        ConsoleUI.banner("FIN DEL ANALISIS  -  StreamFlix Recomendador",
                "Ejecucion completada correctamente");
    }

    /**
     * Imprime el reporte de infraestructura tecnológica necesaria.
     */
    private static void printInfrastructureReport() {
        System.out.println("\n--- Infraestructura para Despliegue ---\n");

        System.out.println("1. SERVIDORES DE APLICACION:");
        System.out.println("   - 4 servidores de aplicacion (API REST)");
        System.out.println("   - 2 servidores de procesamiento ML");
        System.out.println("   - 2 servidores de cache (Redis)");
        System.out.println("   - Load balancer (HAProxy/Nginx)");
        System.out.println("   - Especificaciones: 16 vCPU, 64GB RAM, 500GB SSD cada uno");

        System.out.println("\n2. BASE DE DATOS:");
        System.out.println("   - PostgreSQL (datos de usuarios y calificaciones)");
        System.out.println("   - MongoDB (catalogo de peliculas y metadatos)");
        System.out.println("   - Redis (cache de recomendaciones)");
        System.out.println("   - Elasticsearch (busqueda y filtrado)");

        System.out.println("\n3. ALMACENAMIENTO:");
        System.out.println("   - 2TB para base de datos principal");
        System.out.println("   - 500GB para modelos ML entrenados");
        System.out.println("   - 100GB para logs y metricas");
        System.out.println("   - Backups automaticos diarios en S3");

        System.out.println("\n4. SERVICIOS EN LA NUBE (AWS):");
        System.out.println("   - EC2 para servidores de aplicacion");
        System.out.println("   - RDS para PostgreSQL");
        System.out.println("   - ElastiCache para Redis");
        System.out.println("   - SageMaker para entrenamiento de modelos");
        System.out.println("   - CloudWatch para monitoreo");

        System.out.println("\n--- Costos Estimados (Mensuales) ---\n");

        String[][] costs = {
            {"Servidores de aplicacion (4x EC2 m5.4xlarge)", "$2,480"},
            {"Servidores ML (2x EC2 p3.2xlarge)", "$4,960"},
            {"Base de datos (RDS + MongoDB Atlas)", "$1,800"},
            {"Cache Redis (ElastiCache)", "$520"},
            {"Almacenamiento (EBS + S3)", "$340"},
            {"CDN y transferencia de datos", "$680"},
            {"Monitoreo y logging", "$200"},
            {"Licencias de software", "$500"},
        };

        double total = 0;
        System.out.printf("  %-50s | %-12s%n", "Concepto", "Costo/mes");
        System.out.println("  " + "-".repeat(65));
        for (String[] cost : costs) {
            System.out.printf("  %-50s | %-12s%n", cost[0], cost[1]);
            total += Double.parseDouble(cost[1].replace("$", "").replace(",", ""));
        }
        System.out.println("  " + "-".repeat(65));
        System.out.printf("  %-50s | $%,.0f%n", "TOTAL MENSUAL", total);
        System.out.printf("  %-50s | $%,.0f%n", "TOTAL ANUAL (estimado)", total * 12);

        System.out.println("\n  Costo de implementacion inicial (unico): $45,000 - $65,000");
        System.out.println("  Incluye: Desarrollo, pruebas, migracion y capacitacion del equipo");
    }

    /**
     * Imprime casos de éxito de la industria.
     */
    private static void printSuccessCases() {
        System.out.println("\n--- Casos de Exito en la Industria ---\n");

        System.out.println("1. NETFLIX:");
        System.out.println("   - Algoritmo: Filtrado colaborativo + deep learning");
        System.out.println("   - Resultado: El 80% del contenido visto proviene de recomendaciones");
        System.out.println("   - Impacto: Ahorro estimado de $1 billon/anio en retencion de usuarios");
        System.out.println("   - Tecnologia: Algoritmos propios + AWS");

        System.out.println("\n2. SPOTIFY:");
        System.out.println("   - Algoritmo: NLP + filtrado colaborativo + analisis de audio");
        System.out.println("   - Resultado: 'Discover Weekly' genera 40% mas engagement");
        System.out.println("   - Impacto: Incremento del 25% en tiempo de escucha");
        System.out.println("   - Tecnologia: TensorFlow + Apache Spark");

        System.out.println("\n3. AMAZON PRIME VIDEO:");
        System.out.println("   - Algoritmo: Item-to-item collaborative filtering");
        System.out.println("   - Resultado: 35% de ventas provienen de recomendaciones");
        System.out.println("   - Impacto: Incremento del 29% en ingresos por usuario");
        System.out.println("   - Tecnologia: Amazon Personalize + SageMaker");

        System.out.println("\n4. YOUTUBE:");
        System.out.println("   - Algoritmo: Deep neural networks + reinforcement learning");
        System.out.println("   - Resultado: 70% del tiempo de visualizacion de recomendaciones");
        System.out.println("   - Impacto: Incremento del 20% en tiempo de sesion");
        System.out.println("   - Tecnologia: TensorFlow + Google Cloud");

        System.out.println("\n5. DISNEY+:");
        System.out.println("   - Algoritmo: Content-based + collaborative filtering");
        System.out.println("   - Resultado: Perfiles personalizados aumentan engagement 45%");
        System.out.println("   - Impacto: Reduccion del churn rate en 15%");
        System.out.println("   - Tecnologia: AWS + algoritmos propios");
    }
}
