package streamflix.algorithm;

import streamflix.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del algoritmo de Filtrado Colaborativo basado en usuarios.
 * 
 * Este algoritmo identifica usuarios con gustos similares (vecinos cercanos)
 * y recomienda películas que esos usuarios similares han disfrutado,
 * pero que el usuario objetivo aún no ha visto.
 * 
 * Técnica principal: Similitud del Coseno entre vectores de calificaciones.
 * 
 * Fórmula de similitud del coseno:
 * sim(A,B) = (A · B) / (||A|| * ||B||)
 * donde A y B son los vectores de calificaciones de dos usuarios.
 */
public class CollaborativeFilteringRecommender {

    private List<Movie> movies;
    private List<User> users;
    private List<Rating> ratings;
    private Map<Integer, Map<Integer, Double>> userRatingsMatrix; // userId -> {movieId -> rating}
    private Map<Integer, Double> userMeans;  // userId -> calificación media del usuario
    private double globalMean;               // media global (fallback para usuarios nuevos)
    private int numNeighbors;                // Número de vecinos a considerar (K)

    public CollaborativeFilteringRecommender(List<Movie> movies, List<User> users,
                                              List<Rating> ratings, int numNeighbors) {
        this.movies = movies;
        this.users = users;
        this.ratings = ratings;
        this.numNeighbors = numNeighbors;
        this.userRatingsMatrix = new HashMap<>();
        this.userMeans = new HashMap<>();
        buildRatingsMatrix();
        computeUserMeans();
    }

    /**
     * Construye la matriz de calificaciones usuario-película.
     */
    private void buildRatingsMatrix() {
        for (Rating rating : ratings) {
            userRatingsMatrix
                .computeIfAbsent(rating.getUserId(), k -> new HashMap<>())
                .put(rating.getMovieId(), rating.getScore());
        }
    }

    /**
     * Pre-calcula la calificación media de cada usuario y la media global.
     * Estas medias se utilizan en la predicción centrada (mean-centered),
     * que corrige el sesgo de usuarios "generosos" o "exigentes" y mejora
     * notablemente la precisión (MAE) frente a la media ponderada simple.
     */
    private void computeUserMeans() {
        double sumAll = 0.0;
        int countAll = 0;
        for (Map.Entry<Integer, Map<Integer, Double>> e : userRatingsMatrix.entrySet()) {
            Collection<Double> vals = e.getValue().values();
            double sum = 0.0;
            for (double v : vals) sum += v;
            userMeans.put(e.getKey(), vals.isEmpty() ? 0.0 : sum / vals.size());
            sumAll += sum;
            countAll += vals.size();
        }
        this.globalMean = countAll > 0 ? sumAll / countAll : 3.0;
    }

    /** Devuelve la media de un usuario, o la media global si no tiene historial. */
    private double meanOf(int userId) {
        return userMeans.getOrDefault(userId, globalMean);
    }

    /**
     * Calcula la similitud del coseno entre dos usuarios.
     * Solo considera películas que ambos usuarios han calificado.
     * 
     * @param userId1 ID del primer usuario
     * @param userId2 ID del segundo usuario
     * @return Valor de similitud entre -1 y 1
     */
    public double cosineSimilarity(int userId1, int userId2) {
        Map<Integer, Double> ratings1 = userRatingsMatrix.getOrDefault(userId1, new HashMap<>());
        Map<Integer, Double> ratings2 = userRatingsMatrix.getOrDefault(userId2, new HashMap<>());

        // Encontrar películas en común
        Set<Integer> commonMovies = new HashSet<>(ratings1.keySet());
        commonMovies.retainAll(ratings2.keySet());

        if (commonMovies.isEmpty()) {
            return 0.0; // Sin películas en común, similaridad = 0
        }

        // Calcular producto punto y magnitudes
        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (int movieId : commonMovies) {
            double r1 = ratings1.get(movieId);
            double r2 = ratings2.get(movieId);
            dotProduct += r1 * r2;
            magnitude1 += r1 * r1;
            magnitude2 += r2 * r2;
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }

    /**
     * Encuentra los K vecinos más similares al usuario dado.
     * 
     * @param targetUserId ID del usuario objetivo
     * @return Lista de pares (userId, similitud) ordenados por similitud descendente
     */
    public List<Map.Entry<Integer, Double>> findNearestNeighbors(int targetUserId) {
        Map<Integer, Double> similarities = new HashMap<>();

        for (User user : users) {
            if (user.getId() != targetUserId) {
                double similarity = cosineSimilarity(targetUserId, user.getId());
                if (similarity > 0) { // Solo considerar similitudes positivas
                    similarities.put(user.getId(), similarity);
                }
            }
        }

        // Ordenar por similitud descendente y tomar los K primeros
        return similarities.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(numNeighbors)
                .collect(Collectors.toList());
    }

    /**
     * Genera recomendaciones para un usuario usando filtrado colaborativo.
     * 
     * Algoritmo:
     * 1. Encontrar los K usuarios más similares
     * 2. Recopilar películas que los vecinos han visto pero el usuario objetivo no
     * 3. Calcular un puntaje ponderado para cada película candidata
     * 4. Ordenar por puntaje y devolver las mejores N recomendaciones
     * 
     * @param targetUserId ID del usuario objetivo
     * @param numRecommendations Número de recomendaciones a generar
     * @return Lista de recomendaciones ordenadas por puntaje predicho
     */
    public List<Recommendation> recommend(int targetUserId, int numRecommendations) {
        List<Map.Entry<Integer, Double>> neighbors = findNearestNeighbors(targetUserId);
        Map<Integer, Double> targetRatings = userRatingsMatrix.getOrDefault(targetUserId, new HashMap<>());

        // Calcular puntajes ponderados para películas no vistas
        Map<Integer, Double> weightedScores = new HashMap<>();
        Map<Integer, Double> similaritySum = new HashMap<>();

        double targetMean = meanOf(targetUserId);

        for (Map.Entry<Integer, Double> neighbor : neighbors) {
            int neighborId = neighbor.getKey();
            double similarity = neighbor.getValue();
            double neighborMean = meanOf(neighborId);

            Map<Integer, Double> neighborRatings = userRatingsMatrix.getOrDefault(neighborId, new HashMap<>());

            for (Map.Entry<Integer, Double> movieRating : neighborRatings.entrySet()) {
                int movieId = movieRating.getKey();
                double rating = movieRating.getValue();

                // Solo considerar películas que el usuario objetivo NO ha visto
                if (!targetRatings.containsKey(movieId)) {
                    // Predicción CENTRADA: ponderamos la desviación del vecino
                    // respecto a SU media, no la calificación absoluta.
                    weightedScores.merge(movieId, similarity * (rating - neighborMean), Double::sum);
                    similaritySum.merge(movieId, Math.abs(similarity), Double::sum);
                }
            }
        }

        // Calcular predicciones finales (fórmula mean-centered)
        // pred(u,i) = media(u) + Σ sim(u,v)·(r(v,i) − media(v)) / Σ |sim(u,v)|
        List<Recommendation> recommendations = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : weightedScores.entrySet()) {
            int movieId = entry.getKey();
            double weightedDeviation = entry.getValue();
            double simSum = similaritySum.get(movieId);

            if (simSum > 0) {
                double predictedRating = targetMean + (weightedDeviation / simSum);
                // Acotar al rango válido de calificaciones [1, 5]
                predictedRating = Math.max(1.0, Math.min(5.0, predictedRating));
                Movie movie = findMovieById(movieId);

                if (movie != null) {
                    double confidence = Math.min(simSum / numNeighbors, 1.0);
                    recommendations.add(new Recommendation(
                        movie, predictedRating, "Filtrado Colaborativo", confidence
                    ));
                }
            }
        }

        // Ordenar por puntaje predicho descendente
        recommendations.sort((a, b) -> Double.compare(b.getPredictedScore(), a.getPredictedScore()));

        return recommendations.subList(0, Math.min(numRecommendations, recommendations.size()));
    }

    /**
     * Busca una película por su ID.
     */
    private Movie findMovieById(int movieId) {
        return movies.stream()
                .filter(m -> m.getId() == movieId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Imprime la matriz de similitud entre todos los usuarios (para análisis).
     */
    public void printSimilarityMatrix() {
        System.out.println("\n=== MATRIZ DE SIMILITUD (Top 5 pares mas similares) ===");
        List<double[]> pairs = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            for (int j = i + 1; j < users.size(); j++) {
                double sim = cosineSimilarity(users.get(i).getId(), users.get(j).getId());
                if (sim > 0) {
                    pairs.add(new double[]{users.get(i).getId(), users.get(j).getId(), sim});
                }
            }
        }

        pairs.sort((a, b) -> Double.compare(b[2], a[2]));

        for (int i = 0; i < Math.min(5, pairs.size()); i++) {
            double[] pair = pairs.get(i);
            User u1 = users.get((int)pair[0] - 1);
            User u2 = users.get((int)pair[1] - 1);
            System.out.printf("  %s <-> %s : Similitud = %.4f%n",
                    u1.getName(), u2.getName(), pair[2]);
        }
    }
}
