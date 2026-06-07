package streamflix.model;

/**
 * Modelo de datos para representar una calificación de un usuario a una película.
 * Incluye metadatos como timestamp y tiempo de visualización.
 */
public class Rating {
    private int userId;
    private int movieId;
    private double score; // 1.0 - 5.0
    private long timestamp;
    private int watchTimeMinutes; // tiempo que realmente vio la película
    private boolean completedViewing; // si terminó de ver la película

    public Rating(int userId, int movieId, double score, long timestamp,
                  int watchTimeMinutes, boolean completedViewing) {
        this.userId = userId;
        this.movieId = movieId;
        this.score = score;
        this.timestamp = timestamp;
        this.watchTimeMinutes = watchTimeMinutes;
        this.completedViewing = completedViewing;
    }

    public Rating(int userId, int movieId, double score) {
        this(userId, movieId, score, System.currentTimeMillis(), 0, false);
    }

    // Getters y Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getWatchTimeMinutes() { return watchTimeMinutes; }
    public void setWatchTimeMinutes(int watchTimeMinutes) { this.watchTimeMinutes = watchTimeMinutes; }

    public boolean isCompletedViewing() { return completedViewing; }
    public void setCompletedViewing(boolean completedViewing) { this.completedViewing = completedViewing; }

    @Override
    public String toString() {
        return String.format("Rating[user=%d, movie=%d, score=%.1f, completed=%s]",
                userId, movieId, score, completedViewing);
    }
}
