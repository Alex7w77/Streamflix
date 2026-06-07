package streamflix.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Modelo de datos para representar un usuario de StreamFlix.
 * Almacena información demográfica, preferencias y historial de visualización.
 */
public class User {
    private int id;
    private String name;
    private int age;
    private String gender;
    private String country;
    private String subscriptionType; // "Basic", "Standard", "Premium"
    private List<String> preferredGenres;
    private List<Integer> watchHistory; // IDs de películas vistas
    private Map<Integer, Double> ratings; // movieId -> rating
    private double averageWatchTime; // horas promedio por semana
    private int totalMoviesWatched;

    public User(int id, String name, int age, String gender, String country,
                String subscriptionType, double averageWatchTime) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.country = country;
        this.subscriptionType = subscriptionType;
        this.averageWatchTime = averageWatchTime;
        this.preferredGenres = new ArrayList<>();
        this.watchHistory = new ArrayList<>();
        this.ratings = new HashMap<>();
        this.totalMoviesWatched = 0;
    }

    // Métodos de negocio
    public void rateMovie(int movieId, double rating) {
        ratings.put(movieId, rating);
        if (!watchHistory.contains(movieId)) {
            watchHistory.add(movieId);
            totalMoviesWatched++;
        }
    }

    public boolean hasWatched(int movieId) {
        return watchHistory.contains(movieId);
    }

    public double getRatingForMovie(int movieId) {
        return ratings.getOrDefault(movieId, 0.0);
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getSubscriptionType() { return subscriptionType; }
    public void setSubscriptionType(String subscriptionType) { this.subscriptionType = subscriptionType; }

    public List<String> getPreferredGenres() { return preferredGenres; }
    public void setPreferredGenres(List<String> preferredGenres) { this.preferredGenres = preferredGenres; }
    public void addPreferredGenre(String genre) { this.preferredGenres.add(genre); }

    public List<Integer> getWatchHistory() { return watchHistory; }
    public void setWatchHistory(List<Integer> watchHistory) { this.watchHistory = watchHistory; }

    public Map<Integer, Double> getRatings() { return ratings; }
    public void setRatings(Map<Integer, Double> ratings) { this.ratings = ratings; }

    public double getAverageWatchTime() { return averageWatchTime; }
    public void setAverageWatchTime(double averageWatchTime) { this.averageWatchTime = averageWatchTime; }

    public int getTotalMoviesWatched() { return totalMoviesWatched; }
    public void setTotalMoviesWatched(int totalMoviesWatched) { this.totalMoviesWatched = totalMoviesWatched; }

    @Override
    public String toString() {
        return String.format("User[id=%d, name='%s', age=%d, genres=%s, watched=%d]",
                id, name, age, preferredGenres, totalMoviesWatched);
    }
}
