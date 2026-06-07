package streamflix.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Modelo de datos para representar una película en StreamFlix.
 * Contiene información detallada como título, género, director, año, duración y rating promedio.
 */
public class Movie {
    private int id;
    private String title;
    private String genre;
    private String subGenre;
    private int year;
    private String director;
    private int durationMinutes;
    private double averageRating;
    private String language;
    private List<String> tags;

    public Movie(int id, String title, String genre, String subGenre, int year,
                 String director, int durationMinutes, double averageRating, String language) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.subGenre = subGenre;
        this.year = year;
        this.director = director;
        this.durationMinutes = durationMinutes;
        this.averageRating = averageRating;
        this.language = language;
        this.tags = new ArrayList<>();
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getSubGenre() { return subGenre; }
    public void setSubGenre(String subGenre) { this.subGenre = subGenre; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void addTag(String tag) { this.tags.add(tag); }

    @Override
    public String toString() {
        return String.format("Movie[id=%d, title='%s', genre='%s', year=%d, rating=%.1f]",
                id, title, genre, year, averageRating);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Movie movie = (Movie) obj;
        return id == movie.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
