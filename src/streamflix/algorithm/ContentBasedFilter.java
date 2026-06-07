package streamflix.algorithm;

import streamflix.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Filtro Basado en Contenido para el sistema de recomendación.
 * 
 * Analiza las características de las películas que un usuario ha disfrutado
 * y busca películas similares basándose en atributos como género, director,
 * año, tags y duración.
 * 
 * A diferencia del filtrado colaborativo (que compara usuarios),
 * este método compara películas entre sí.
 */
public class ContentBasedFilter {

    private List<Movie> movies;
    private List<User> users;
    private List<Rating> ratings;

    public ContentBasedFilter(List<Movie> movies, List<User> users, List<Rating> ratings) {
        this.movies = movies;
        this.users = users;
        this.ratings = ratings;
    }

    /**
     * Calcula la similitud entre dos películas basándose en sus atributos.
     * 
     * Pesos de cada atributo:
     * - Género: 0.35
     * - SubGénero: 0.20
     * - Tags compartidos: 0.20
     * - Proximidad temporal: 0.10
     * - Duración similar: 0.10
     * - Mismo idioma: 0.05
     * 
     * @return Valor de similitud entre 0 y 1
     */
    public double movieSimilarity(Movie m1, Movie m2) {
        double similarity = 0.0;

        // Género (peso: 0.35)
        if (m1.getGenre().equals(m2.getGenre())) {
            similarity += 0.35;
        }

        // SubGénero (peso: 0.20)
        if (m1.getSubGenre().equals(m2.getSubGenre())) {
            similarity += 0.20;
        }

        // Tags compartidos (peso: 0.20)
        Set<String> tags1 = new HashSet<>(m1.getTags());
        Set<String> tags2 = new HashSet<>(m2.getTags());
        Set<String> intersection = new HashSet<>(tags1);
        intersection.retainAll(tags2);
        Set<String> union = new HashSet<>(tags1);
        union.addAll(tags2);
        if (!union.isEmpty()) {
            double jaccardIndex = (double) intersection.size() / union.size();
            similarity += 0.20 * jaccardIndex;
        }

        // Proximidad temporal (peso: 0.10) - películas del mismo periodo
        int yearDiff = Math.abs(m1.getYear() - m2.getYear());
        similarity += 0.10 * Math.max(0, 1.0 - yearDiff / 10.0);

        // Duración similar (peso: 0.10)
        int durationDiff = Math.abs(m1.getDurationMinutes() - m2.getDurationMinutes());
        similarity += 0.10 * Math.max(0, 1.0 - durationDiff / 60.0);

        // Mismo idioma (peso: 0.05)
        if (m1.getLanguage().equals(m2.getLanguage())) {
            similarity += 0.05;
        }

        return similarity;
    }

    /**
     * Construye el perfil de preferencias de un usuario basado en sus calificaciones.
     * 
     * @param user Usuario para el cual construir el perfil
     * @return Mapa de género -> puntaje de preferencia
     */
    public Map<String, Double> buildUserProfile(User user) {
        Map<String, Double> profile = new HashMap<>();
        Map<String, Integer> genreCounts = new HashMap<>();

        for (Map.Entry<Integer, Double> entry : user.getRatings().entrySet()) {
            Movie movie = movies.stream()
                    .filter(m -> m.getId() == entry.getKey())
                    .findFirst()
                    .orElse(null);

            if (movie != null && entry.getValue() >= 3.5) {
                profile.merge(movie.getGenre(), entry.getValue(), Double::sum);
                genreCounts.merge(movie.getGenre(), 1, Integer::sum);

                // También considerar tags
                for (String tag : movie.getTags()) {
                    profile.merge("tag:" + tag, entry.getValue() * 0.5, Double::sum);
                    genreCounts.merge("tag:" + tag, 1, Integer::sum);
                }
            }
        }

        // Normalizar por cantidad
        for (String key : profile.keySet()) {
            int count = genreCounts.getOrDefault(key, 1);
            profile.put(key, profile.get(key) / count);
        }

        return profile;
    }

    /**
     * Genera recomendaciones basadas en contenido.
     * 
     * @param user Usuario objetivo
     * @param numRecommendations Cantidad de recomendaciones
     * @return Lista de recomendaciones
     */
    public List<Recommendation> recommend(User user, int numRecommendations) {
        // Obtener películas bien calificadas por el usuario
        List<Movie> likedMovies = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : user.getRatings().entrySet()) {
            if (entry.getValue() >= 3.5) {
                movies.stream()
                        .filter(m -> m.getId() == entry.getKey())
                        .findFirst()
                        .ifPresent(likedMovies::add);
            }
        }

        // Calcular similitud de cada película no vista con las películas que le gustaron
        Map<Integer, Double> candidateScores = new HashMap<>();

        for (Movie candidate : movies) {
            if (user.hasWatched(candidate.getId())) continue;

            double maxSimilarity = 0;
            double avgSimilarity = 0;

            for (Movie liked : likedMovies) {
                double sim = movieSimilarity(candidate, liked);
                maxSimilarity = Math.max(maxSimilarity, sim);
                avgSimilarity += sim;
            }

            if (!likedMovies.isEmpty()) {
                avgSimilarity /= likedMovies.size();
            }

            // Combinar máxima y promedio para el puntaje final
            double finalScore = 0.6 * maxSimilarity + 0.4 * avgSimilarity;
            candidateScores.put(candidate.getId(), finalScore);
        }

        // Convertir a recomendaciones
        List<Recommendation> recommendations = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : candidateScores.entrySet()) {
            Movie movie = movies.stream()
                    .filter(m -> m.getId() == entry.getKey())
                    .findFirst()
                    .orElse(null);

            if (movie != null) {
                // Escalar puntaje a rango 1-5
                double predictedRating = 1.0 + entry.getValue() * 4.0;
                recommendations.add(new Recommendation(
                    movie, predictedRating, "Basado en Contenido", entry.getValue()
                ));
            }
        }

        recommendations.sort((a, b) -> Double.compare(b.getPredictedScore(), a.getPredictedScore()));
        return recommendations.subList(0, Math.min(numRecommendations, recommendations.size()));
    }
}
