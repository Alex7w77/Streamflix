package streamflix.algorithm;

import streamflix.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sistema de Recomendación Híbrido que combina múltiples algoritmos.
 * 
 * Combina los resultados de:
 * 1. Filtrado Colaborativo (peso: 0.40)
 * 2. Árbol de Decisión (peso: 0.30)
 * 3. Basado en Contenido (peso: 0.30)
 * 
 * La combinación ponderada produce recomendaciones más precisas
 * que cualquier algoritmo individual.
 */
public class HybridRecommender {

    private CollaborativeFilteringRecommender collaborativeFilter;
    private DecisionTreeRecommender decisionTree;
    private ContentBasedFilter contentBasedFilter;
    private List<Movie> movies;
    private List<User> users;

    // Pesos para cada algoritmo
    private double weightCollaborative = 0.40;
    private double weightDecisionTree = 0.30;
    private double weightContentBased = 0.30;

    public HybridRecommender(List<Movie> movies, List<User> users, List<Rating> ratings) {
        this.movies = movies;
        this.users = users;

        // Inicializar sub-sistemas
        this.collaborativeFilter = new CollaborativeFilteringRecommender(movies, users, ratings, 5);
        this.decisionTree = new DecisionTreeRecommender(movies, users, ratings, 4, 3);
        this.contentBasedFilter = new ContentBasedFilter(movies, users, ratings);

        // Construir el árbol de decisión
        decisionTree.buildTree();
    }

    /**
     * Genera recomendaciones híbridas combinando todos los algoritmos.
     * 
     * @param userId ID del usuario
     * @param numRecommendations Cantidad de recomendaciones
     * @return Lista de recomendaciones híbridas
     */
    public List<Recommendation> recommend(int userId, int numRecommendations) {
        User user = users.stream().filter(u -> u.getId() == userId).findFirst().orElse(null);
        if (user == null) return new ArrayList<>();

        // Obtener recomendaciones de cada algoritmo
        List<Recommendation> cfRecs = collaborativeFilter.recommend(userId, numRecommendations * 2);
        List<Recommendation> dtRecs = decisionTree.recommend(user, numRecommendations * 2);
        List<Recommendation> cbRecs = contentBasedFilter.recommend(user, numRecommendations * 2);

        // Combinar puntajes ponderados
        Map<Integer, Double> combinedScores = new HashMap<>();
        Map<Integer, Movie> movieMap = new HashMap<>();
        Map<Integer, Integer> methodCount = new HashMap<>();

        // Agregar puntajes del filtrado colaborativo
        for (Recommendation rec : cfRecs) {
            int movieId = rec.getMovie().getId();
            combinedScores.merge(movieId, rec.getPredictedScore() * weightCollaborative, Double::sum);
            movieMap.put(movieId, rec.getMovie());
            methodCount.merge(movieId, 1, Integer::sum);
        }

        // Agregar puntajes del árbol de decisión
        for (Recommendation rec : dtRecs) {
            int movieId = rec.getMovie().getId();
            combinedScores.merge(movieId, rec.getPredictedScore() * weightDecisionTree, Double::sum);
            movieMap.put(movieId, rec.getMovie());
            methodCount.merge(movieId, 1, Integer::sum);
        }

        // Agregar puntajes del basado en contenido
        for (Recommendation rec : cbRecs) {
            int movieId = rec.getMovie().getId();
            combinedScores.merge(movieId, rec.getPredictedScore() * weightContentBased, Double::sum);
            movieMap.put(movieId, rec.getMovie());
            methodCount.merge(movieId, 1, Integer::sum);
        }

        // Crear recomendaciones finales
        List<Recommendation> hybridRecs = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : combinedScores.entrySet()) {
            int movieId = entry.getKey();
            double score = entry.getValue();
            int methods = methodCount.getOrDefault(movieId, 1);

            // Mayor confianza si más métodos coinciden
            double confidence = (double) methods / 3.0;

            hybridRecs.add(new Recommendation(
                movieMap.get(movieId), score, "Hibrido (" + methods + " metodos)", confidence
            ));
        }

        // Usar Merge Sort recursivo para ordenar
        hybridRecs = RecursiveUtils.mergeSortRecommendations(hybridRecs);

        return hybridRecs.subList(0, Math.min(numRecommendations, hybridRecs.size()));
    }

    /**
     * Obtiene las recomendaciones individuales de cada algoritmo para comparación.
     */
    public Map<String, List<Recommendation>> getIndividualRecommendations(int userId, int n) {
        User user = users.stream().filter(u -> u.getId() == userId).findFirst().orElse(null);
        if (user == null) return new HashMap<>();

        Map<String, List<Recommendation>> results = new LinkedHashMap<>();
        results.put("Filtrado Colaborativo", collaborativeFilter.recommend(userId, n));
        results.put("Arbol de Decision", decisionTree.recommend(user, n));
        results.put("Basado en Contenido", contentBasedFilter.recommend(user, n));
        results.put("Hibrido", recommend(userId, n));

        return results;
    }

    // Getters para sub-sistemas
    public CollaborativeFilteringRecommender getCollaborativeFilter() { return collaborativeFilter; }
    public DecisionTreeRecommender getDecisionTree() { return decisionTree; }
    public ContentBasedFilter getContentBasedFilter() { return contentBasedFilter; }
}
