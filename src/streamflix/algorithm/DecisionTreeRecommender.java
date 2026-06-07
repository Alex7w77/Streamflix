package streamflix.algorithm;

import streamflix.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de un Árbol de Decisión para el sistema de recomendación.
 * 
 * El árbol clasifica usuarios en perfiles basándose en sus características
 * (edad, tiempo de visualización, tipo de suscripción) y determina qué
 * géneros de películas son más apropiados para cada perfil.
 * 
 * Algoritmo: CART (Classification and Regression Trees) simplificado
 * Criterio de división: Índice de Gini
 * 
 * Índice Gini = 1 - Σ(pi²)
 * donde pi es la proporción de cada clase en el nodo.
 */
public class DecisionTreeRecommender {

    private DecisionTreeNode root;
    private List<Movie> movies;
    private List<User> users;
    private List<Rating> ratings;
    private int maxDepth;
    private int minSamplesLeaf;

    public DecisionTreeRecommender(List<Movie> movies, List<User> users,
                                    List<Rating> ratings, int maxDepth, int minSamplesLeaf) {
        this.movies = movies;
        this.users = users;
        this.ratings = ratings;
        this.maxDepth = maxDepth;
        this.minSamplesLeaf = minSamplesLeaf;
    }

    /**
     * Construye el árbol de decisión basado en los datos de los usuarios.
     * Utiliza el índice Gini para encontrar la mejor división en cada nodo.
     */
    public void buildTree() {
        // Preparar datos de entrenamiento: cada usuario con su género favorito
        List<Map<String, Object>> trainingData = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> sample = new HashMap<>();
            sample.put("age", user.getAge());
            sample.put("watchTime", user.getAverageWatchTime());
            sample.put("subscription", user.getSubscriptionType());
            sample.put("moviesWatched", user.getTotalMoviesWatched());

            // Determinar género favorito basado en calificaciones
            String favoriteGenre = determineFavoriteGenre(user);
            sample.put("favoriteGenre", favoriteGenre);

            trainingData.add(sample);
        }

        // Construir árbol recursivamente
        root = buildTreeRecursive(trainingData, 0);
    }

    /**
     * Construye el árbol de decisión recursivamente.
     * 
     * Proceso recursivo:
     * 1. Caso base: si la profundidad es máxima o hay pocas muestras, crear hoja
     * 2. Encontrar la mejor división (atributo + umbral) usando Gini
     * 3. Dividir los datos y recurrir en cada subconjunto
     * 
     * @param data Datos de entrenamiento para este nodo
     * @param depth Profundidad actual en el árbol
     * @return Nodo del árbol de decisión
     */
    private DecisionTreeNode buildTreeRecursive(List<Map<String, Object>> data, int depth) {
        // CASO BASE 1: Profundidad máxima alcanzada
        if (depth >= maxDepth) {
            return createLeafNode(data, depth);
        }

        // CASO BASE 2: Muy pocas muestras
        if (data.size() <= minSamplesLeaf) {
            return createLeafNode(data, depth);
        }

        // CASO BASE 3: Todas las muestras tienen la misma clase
        Set<String> uniqueClasses = data.stream()
                .map(d -> (String) d.get("favoriteGenre"))
                .collect(Collectors.toSet());
        if (uniqueClasses.size() == 1) {
            return createLeafNode(data, depth);
        }

        // CASO RECURSIVO: Encontrar la mejor división
        BestSplit bestSplit = findBestSplit(data);

        if (bestSplit == null) {
            return createLeafNode(data, depth);
        }

        // Dividir datos
        List<Map<String, Object>> leftData = new ArrayList<>();
        List<Map<String, Object>> rightData = new ArrayList<>();

        for (Map<String, Object> sample : data) {
            if (evaluateCondition(sample, bestSplit.attribute, bestSplit.threshold)) {
                leftData.add(sample);
            } else {
                rightData.add(sample);
            }
        }

        // Verificar que la división sea válida
        if (leftData.isEmpty() || rightData.isEmpty()) {
            return createLeafNode(data, depth);
        }

        // Crear nodo interno
        String condition = bestSplit.attribute + " <= " + String.format("%.1f", bestSplit.threshold);
        DecisionTreeNode node = new DecisionTreeNode(bestSplit.attribute, condition, bestSplit.threshold);
        node.setDepth(depth);
        node.setGiniIndex(bestSplit.gini);

        // LLAMADAS RECURSIVAS
        node.setLeft(buildTreeRecursive(leftData, depth + 1));
        node.setRight(buildTreeRecursive(rightData, depth + 1));

        return node;
    }

    /**
     * Encuentra la mejor división para un nodo dado.
     * Evalúa todas las combinaciones posibles de atributo/umbral.
     */
    private BestSplit findBestSplit(List<Map<String, Object>> data) {
        BestSplit best = null;
        double bestGini = 1.0;

        // Atributos numéricos a evaluar
        String[] attributes = {"age", "watchTime", "moviesWatched"};

        for (String attr : attributes) {
            // Obtener valores únicos y ordenarlos
            List<Double> values = data.stream()
                    .map(d -> ((Number) d.get(attr)).doubleValue())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // Probar umbrales entre valores consecutivos
            for (int i = 0; i < values.size() - 1; i++) {
                double threshold = (values.get(i) + values.get(i + 1)) / 2.0;

                List<Map<String, Object>> left = new ArrayList<>();
                List<Map<String, Object>> right = new ArrayList<>();

                for (Map<String, Object> sample : data) {
                    double val = ((Number) sample.get(attr)).doubleValue();
                    if (val <= threshold) {
                        left.add(sample);
                    } else {
                        right.add(sample);
                    }
                }

                if (left.isEmpty() || right.isEmpty()) continue;

                // Calcular Gini ponderado
                double gini = calculateWeightedGini(left, right, data.size());

                if (gini < bestGini) {
                    bestGini = gini;
                    best = new BestSplit(attr, threshold, gini);
                }
            }
        }

        return best;
    }

    /**
     * Calcula el índice Gini para un conjunto de datos.
     * Gini = 1 - Σ(pi²)
     */
    private double calculateGini(List<Map<String, Object>> data) {
        if (data.isEmpty()) return 0.0;

        Map<String, Long> classCounts = data.stream()
                .collect(Collectors.groupingBy(
                        d -> (String) d.get("favoriteGenre"),
                        Collectors.counting()
                ));

        double gini = 1.0;
        for (long count : classCounts.values()) {
            double proportion = (double) count / data.size();
            gini -= proportion * proportion;
        }

        return gini;
    }

    /**
     * Calcula el Gini ponderado de una división.
     */
    private double calculateWeightedGini(List<Map<String, Object>> left,
                                          List<Map<String, Object>> right, int totalSize) {
        double leftWeight = (double) left.size() / totalSize;
        double rightWeight = (double) right.size() / totalSize;

        return leftWeight * calculateGini(left) + rightWeight * calculateGini(right);
    }

    /**
     * Evalúa una condición numérica.
     */
    private boolean evaluateCondition(Map<String, Object> sample, String attribute, double threshold) {
        double value = ((Number) sample.get(attribute)).doubleValue();
        return value <= threshold;
    }

    /**
     * Crea un nodo hoja con los géneros más frecuentes en los datos.
     */
    private DecisionTreeNode createLeafNode(List<Map<String, Object>> data, int depth) {
        Map<String, Long> genreCounts = data.stream()
                .collect(Collectors.groupingBy(
                        d -> (String) d.get("favoriteGenre"),
                        Collectors.counting()
                ));

        // Ordenar géneros por frecuencia
        List<String> topGenres = genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        String label = "Perfil: " + String.join(", ", topGenres);
        DecisionTreeNode leaf = new DecisionTreeNode(topGenres, label);
        leaf.setDepth(depth);
        return leaf;
    }

    /**
     * Determina el género favorito de un usuario basándose en sus calificaciones.
     */
    private String determineFavoriteGenre(User user) {
        Map<String, Double> genreScores = new HashMap<>();
        Map<String, Integer> genreCounts = new HashMap<>();

        for (Map.Entry<Integer, Double> entry : user.getRatings().entrySet()) {
            Movie movie = movies.stream()
                    .filter(m -> m.getId() == entry.getKey())
                    .findFirst()
                    .orElse(null);

            if (movie != null) {
                genreScores.merge(movie.getGenre(), entry.getValue(), Double::sum);
                genreCounts.merge(movie.getGenre(), 1, Integer::sum);
            }
        }

        // Calcular promedio por género
        String bestGenre = "Drama"; // default
        double bestAvg = 0;

        for (Map.Entry<String, Double> entry : genreScores.entrySet()) {
            double avg = entry.getValue() / genreCounts.get(entry.getKey());
            if (avg > bestAvg) {
                bestAvg = avg;
                bestGenre = entry.getKey();
            }
        }

        return bestGenre;
    }

    /**
     * Clasifica un usuario recorriendo el árbol de decisión de forma recursiva.
     * 
     * @param user Usuario a clasificar
     * @return Lista de géneros recomendados
     */
    public List<String> classify(User user) {
        return classifyRecursive(root, user);
    }

    /**
     * Recorrido recursivo del árbol para clasificar un usuario.
     */
    private List<String> classifyRecursive(DecisionTreeNode node, User user) {
        // CASO BASE: nodo hoja alcanzado
        if (node.isLeaf()) {
            return node.getRecommendedGenres();
        }

        // CASO RECURSIVO: evaluar condición y descender
        double value;
        switch (node.getAttribute()) {
            case "age": value = user.getAge(); break;
            case "watchTime": value = user.getAverageWatchTime(); break;
            case "moviesWatched": value = user.getTotalMoviesWatched(); break;
            default: value = 0; break;
        }

        if (value <= node.getThreshold()) {
            return classifyRecursive(node.getLeft(), user);
        } else {
            return classifyRecursive(node.getRight(), user);
        }
    }

    /**
     * Genera recomendaciones usando el árbol de decisión.
     */
    public List<Recommendation> recommend(User user, int numRecommendations) {
        List<String> recommendedGenres = classify(user);
        List<Recommendation> recommendations = new ArrayList<>();

        // Buscar películas de los géneros recomendados que el usuario no ha visto
        for (Movie movie : movies) {
            if (!user.hasWatched(movie.getId()) && recommendedGenres.contains(movie.getGenre())) {
                double predictedScore = movie.getAverageRating();
                // Ajustar por posición del género en la lista de recomendados
                int genreRank = recommendedGenres.indexOf(movie.getGenre());
                double bonus = (recommendedGenres.size() - genreRank) * 0.2;
                predictedScore = Math.min(5.0, predictedScore + bonus);

                double confidence = 1.0 / (1.0 + genreRank);
                recommendations.add(new Recommendation(
                    movie, predictedScore, "Arbol de Decision", confidence
                ));
            }
        }

        recommendations.sort((a, b) -> Double.compare(b.getPredictedScore(), a.getPredictedScore()));
        return recommendations.subList(0, Math.min(numRecommendations, recommendations.size()));
    }

    /**
     * Imprime la estructura del árbol de decisión de forma visual.
     */
    public void printTree() {
        System.out.println("\n=== ARBOL DE DECISION ===");
        printTreeRecursive(root, "", true);
    }

    /**
     * Impresión recursiva del árbol con formato visual.
     */
    private void printTreeRecursive(DecisionTreeNode node, String prefix, boolean isLast) {
        if (node == null) return;

        String connector = isLast ? "+-- " : "|-- ";
        String extension = isLast ? "    " : "|   ";

        if (node.isLeaf()) {
            System.out.println(prefix + connector + "[HOJA] " + node.getLabel());
        } else {
            System.out.println(prefix + connector + "[" + node.getCondition()
                    + "] (Gini=" + String.format("%.3f", node.getGiniIndex()) + ")");
            printTreeRecursive(node.getLeft(), prefix + extension, false);
            printTreeRecursive(node.getRight(), prefix + extension, true);
        }
    }

    /**
     * Clase interna para almacenar la mejor división encontrada.
     */
    private static class BestSplit {
        String attribute;
        double threshold;
        double gini;

        BestSplit(String attribute, double threshold, double gini) {
            this.attribute = attribute;
            this.threshold = threshold;
            this.gini = gini;
        }
    }
}
