package streamflix.model;

/**
 * Representa una recomendación generada por el sistema.
 * Incluye la película recomendada, el puntaje de predicción y el método utilizado.
 */
public class Recommendation {
    private Movie movie;
    private double predictedScore;
    private String method; // "CollaborativeFiltering", "DecisionTree", "ContentBased", "Hybrid"
    private double confidence;

    public Recommendation(Movie movie, double predictedScore, String method, double confidence) {
        this.movie = movie;
        this.predictedScore = predictedScore;
        this.method = method;
        this.confidence = confidence;
    }

    public Movie getMovie() { return movie; }
    public double getPredictedScore() { return predictedScore; }
    public String getMethod() { return method; }
    public double getConfidence() { return confidence; }

    @Override
    public String toString() {
        return String.format("  -> '%s' (Prediccion: %.2f, Confianza: %.1f%%, Metodo: %s)",
                movie.getTitle(), predictedScore, confidence * 100, method);
    }
}
