package streamflix.algorithm;

import streamflix.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Funciones recursivas para el procesamiento de datos del sistema de recomendación.
 * 
 * Implementa diversas operaciones recursivas para:
 * - Búsqueda binaria recursiva en listas ordenadas
 * - Ordenamiento por mezcla (Merge Sort) recursivo
 * - Cálculo recursivo de similitud entre usuarios
 * - Recorrido recursivo de estructuras jerárquicas de géneros
 * - Filtrado recursivo de películas por múltiples criterios
 */
public class RecursiveUtils {

    /**
     * Búsqueda binaria recursiva de una película por su rating promedio.
     * Busca la película más cercana a un rating objetivo.
     * 
     * Complejidad: O(log n)
     * 
     * @param movies Lista de películas ordenadas por rating
     * @param targetRating Rating objetivo a buscar
     * @param left Índice izquierdo
     * @param right Índice derecho
     * @return Índice de la película más cercana al rating objetivo
     */
    public static int binarySearchByRating(List<Movie> movies, double targetRating,
                                            int left, int right) {
        // CASO BASE: Rango reducido a un elemento
        if (left >= right) {
            return left;
        }

        // CASO BASE: Dos elementos contiguos
        if (right - left == 1) {
            double diffLeft = Math.abs(movies.get(left).getAverageRating() - targetRating);
            double diffRight = Math.abs(movies.get(right).getAverageRating() - targetRating);
            return diffLeft <= diffRight ? left : right;
        }

        // CASO RECURSIVO
        int mid = left + (right - left) / 2;
        double midRating = movies.get(mid).getAverageRating();

        if (Math.abs(midRating - targetRating) < 0.01) {
            return mid; // Encontrado exacto
        } else if (midRating < targetRating) {
            return binarySearchByRating(movies, targetRating, mid, right);
        } else {
            return binarySearchByRating(movies, targetRating, left, mid);
        }
    }

    /**
     * Merge Sort recursivo para ordenar películas por puntaje predicho.
     * 
     * Complejidad: O(n log n)
     * 
     * @param recommendations Lista de recomendaciones a ordenar
     * @return Lista ordenada de mayor a menor puntaje predicho
     */
    public static List<Recommendation> mergeSortRecommendations(List<Recommendation> recommendations) {
        // CASO BASE: Lista de 0 o 1 elementos ya está ordenada
        if (recommendations.size() <= 1) {
            return new ArrayList<>(recommendations);
        }

        // CASO RECURSIVO: Dividir, ordenar cada mitad, y combinar
        int mid = recommendations.size() / 2;
        List<Recommendation> left = mergeSortRecommendations(
                recommendations.subList(0, mid));
        List<Recommendation> right = mergeSortRecommendations(
                recommendations.subList(mid, recommendations.size()));

        return merge(left, right);
    }

    /**
     * Combina dos listas ordenadas en una sola lista ordenada.
     */
    private static List<Recommendation> merge(List<Recommendation> left,
                                                List<Recommendation> right) {
        List<Recommendation> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            // Orden descendente por puntaje predicho
            if (left.get(i).getPredictedScore() >= right.get(j).getPredictedScore()) {
                result.add(left.get(i));
                i++;
            } else {
                result.add(right.get(j));
                j++;
            }
        }

        while (i < left.size()) { result.add(left.get(i)); i++; }
        while (j < right.size()) { result.add(right.get(j)); j++; }

        return result;
    }

    /**
     * Calcula recursivamente la similitud acumulada entre un usuario
     * y una lista de usuarios candidatos.
     * 
     * @param targetRatings Calificaciones del usuario objetivo
     * @param candidates Lista de usuarios candidatos
     * @param index Índice actual en la recursión
     * @param allRatings Mapa de calificaciones de todos los usuarios
     * @return Suma acumulada de similitudes
     */
    public static double recursiveSimilaritySum(Map<Integer, Double> targetRatings,
                                                  List<User> candidates, int index,
                                                  Map<Integer, Map<Integer, Double>> allRatings) {
        // CASO BASE: No hay más candidatos
        if (index >= candidates.size()) {
            return 0.0;
        }

        // CASO RECURSIVO: Calcular similitud con el candidato actual + resto
        User candidate = candidates.get(index);
        Map<Integer, Double> candidateRatings = allRatings.getOrDefault(candidate.getId(), new HashMap<>());

        double similarity = calculatePearsonCorrelation(targetRatings, candidateRatings);

        return similarity + recursiveSimilaritySum(targetRatings, candidates, index + 1, allRatings);
    }

    /**
     * Correlación de Pearson entre dos conjuntos de calificaciones.
     */
    private static double calculatePearsonCorrelation(Map<Integer, Double> ratings1,
                                                        Map<Integer, Double> ratings2) {
        Set<Integer> common = new HashSet<>(ratings1.keySet());
        common.retainAll(ratings2.keySet());

        if (common.size() < 2) return 0.0;

        double mean1 = common.stream().mapToDouble(ratings1::get).average().orElse(0);
        double mean2 = common.stream().mapToDouble(ratings2::get).average().orElse(0);

        double numerator = 0, denom1 = 0, denom2 = 0;
        for (int id : common) {
            double diff1 = ratings1.get(id) - mean1;
            double diff2 = ratings2.get(id) - mean2;
            numerator += diff1 * diff2;
            denom1 += diff1 * diff1;
            denom2 += diff2 * diff2;
        }

        double denominator = Math.sqrt(denom1) * Math.sqrt(denom2);
        return denominator == 0 ? 0 : numerator / denominator;
    }

    /**
     * Filtra películas recursivamente aplicando múltiples criterios en secuencia.
     * 
     * Cada nivel de recursión aplica un filtro diferente:
     * - Nivel 0: Filtro por género
     * - Nivel 1: Filtro por año mínimo
     * - Nivel 2: Filtro por rating mínimo
     * - Nivel 3: Filtro por duración máxima
     * 
     * @param movies Lista de películas a filtrar
     * @param criteria Array de criterios [genero, anoMinimo, ratingMinimo, duracionMaxima]
     * @param level Nivel actual del filtro recursivo
     * @return Lista filtrada de películas
     */
    public static List<Movie> recursiveFilter(List<Movie> movies, Object[] criteria, int level) {
        // CASO BASE: Todos los filtros aplicados
        if (level >= criteria.length || movies.isEmpty()) {
            return new ArrayList<>(movies);
        }

        // CASO RECURSIVO: Aplicar filtro del nivel actual
        List<Movie> filtered;

        switch (level) {
            case 0: // Filtro por género
                String genre = (String) criteria[0];
                if (genre != null && !genre.isEmpty()) {
                    filtered = movies.stream()
                            .filter(m -> m.getGenre().equals(genre))
                            .collect(Collectors.toList());
                } else {
                    filtered = new ArrayList<>(movies);
                }
                break;

            case 1: // Filtro por año mínimo
                int minYear = (int) criteria[1];
                filtered = movies.stream()
                        .filter(m -> m.getYear() >= minYear)
                        .collect(Collectors.toList());
                break;

            case 2: // Filtro por rating mínimo
                double minRating = (double) criteria[2];
                filtered = movies.stream()
                        .filter(m -> m.getAverageRating() >= minRating)
                        .collect(Collectors.toList());
                break;

            case 3: // Filtro por duración máxima
                int maxDuration = (int) criteria[3];
                filtered = movies.stream()
                        .filter(m -> m.getDurationMinutes() <= maxDuration)
                        .collect(Collectors.toList());
                break;

            default:
                filtered = new ArrayList<>(movies);
                break;
        }

        // Recursión al siguiente nivel de filtro
        return recursiveFilter(filtered, criteria, level + 1);
    }

    /**
     * Genera recursivamente un árbol de categorías de géneros y subgéneros.
     * Útil para la navegación jerárquica del catálogo.
     * 
     * @param movies Lista de películas
     * @param genreHierarchy Mapa de género -> subgéneros
     * @param genres Lista de géneros a procesar
     * @param index Índice actual
     * @return Representación textual del árbol de categorías
     */
    public static String buildGenreTreeRecursive(List<Movie> movies,
                                                   Map<String, Set<String>> genreHierarchy,
                                                   List<String> genres, int index) {
        // CASO BASE
        if (index >= genres.size()) {
            return "";
        }

        String genre = genres.get(index);
        Set<String> subGenres = genreHierarchy.getOrDefault(genre, new HashSet<>());

        StringBuilder sb = new StringBuilder();
        sb.append(genre).append("\n");

        for (String sub : subGenres) {
            long count = movies.stream()
                    .filter(m -> m.getGenre().equals(genre) && m.getSubGenre().equals(sub))
                    .count();
            sb.append("  |-- ").append(sub).append(" (").append(count).append(" peliculas)\n");
        }

        // RECURSIÓN: Procesar siguiente género
        sb.append(buildGenreTreeRecursive(movies, genreHierarchy, genres, index + 1));

        return sb.toString();
    }

    /**
     * Calcula recursivamente el promedio ponderado de calificaciones
     * de una lista de películas, dando más peso a las más recientes.
     * 
     * @param ratings Lista de calificaciones ordenadas por timestamp
     * @param index Índice actual
     * @param decayFactor Factor de decaimiento (0.0 a 1.0)
     * @return Promedio ponderado
     */
    public static double weightedAverageRecursive(List<Rating> ratings, int index, double decayFactor) {
        // CASO BASE
        if (index >= ratings.size()) {
            return 0.0;
        }

        // CASO BASE: Último elemento
        if (index == ratings.size() - 1) {
            return ratings.get(index).getScore();
        }

        // CASO RECURSIVO: peso actual + (decay * promedio del resto)
        double currentScore = ratings.get(index).getScore();
        double restAverage = weightedAverageRecursive(ratings, index + 1, decayFactor);

        return currentScore * (1 - decayFactor) + restAverage * decayFactor;
    }
}
