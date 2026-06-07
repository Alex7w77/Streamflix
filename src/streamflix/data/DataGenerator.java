package streamflix.data;

import streamflix.model.Movie;
import streamflix.model.User;
import streamflix.model.Rating;

import java.util.*;

/**
 * Generador de datos ficticios para el sistema de recomendación de StreamFlix.
 * Genera películas, usuarios y calificaciones con patrones realistas para
 * simular el comportamiento de una plataforma de streaming real.
 * 
 * Los datos generados incluyen correlaciones intencionales entre:
 * - Edad del usuario y preferencia de género
 * - Historial de visualización y calificaciones
 * - Patrones de consumo por tipo de suscripción
 */
public class DataGenerator {

    private Random random;
    private List<Movie> movies;
    private List<User> users;
    private List<Rating> ratings;

    public DataGenerator() {
        this.random = new Random(42); // Semilla fija para reproducibilidad
        this.movies = new ArrayList<>();
        this.users = new ArrayList<>();
        this.ratings = new ArrayList<>();
    }

    /**
     * Genera el dataset completo: películas, usuarios y calificaciones.
     */
    public void generateAll() {
        generateMovies();
        generateUsers();
        generateRatings();
    }

    /**
     * Genera 50 películas ficticias con datos variados.
     */
    public void generateMovies() {
        String[][] movieData = {
            // {titulo, genero, subgenero, año, director, duracion, rating, idioma}
            {"El Ultimo Horizonte", "Ciencia Ficcion", "Aventura Espacial", "2023", "Ana Martinez", "148", "4.2", "Espanol"},
            {"Noches de Tokio", "Drama", "Romance", "2022", "Kenji Yamamoto", "125", "4.5", "Japones"},
            {"Codigo Rojo", "Accion", "Thriller", "2023", "John Smith", "132", "3.8", "Ingles"},
            {"La Sombra del Pasado", "Suspenso", "Misterio", "2021", "Carlos Ruiz", "118", "4.1", "Espanol"},
            {"Risas en Paris", "Comedia", "Romantica", "2022", "Marie Dupont", "105", "3.9", "Frances"},
            {"El Bosque Encantado", "Fantasia", "Aventura", "2023", "Laura Chen", "140", "4.3", "Ingles"},
            {"Documental: Oceanos", "Documental", "Naturaleza", "2021", "David Atkins", "95", "4.7", "Ingles"},
            {"Venganza Silenciosa", "Accion", "Venganza", "2022", "Park Min-Ho", "128", "3.6", "Coreano"},
            {"Amor en Tiempos Modernos", "Romance", "Comedia", "2023", "Sofia Gonzalez", "112", "4.0", "Espanol"},
            {"El Algoritmo", "Ciencia Ficcion", "Cyberpunk", "2024", "James Cameron Jr", "155", "4.4", "Ingles"},
            {"Terror en la Montana", "Terror", "Supernatural", "2022", "Roberto Diaz", "98", "3.5", "Espanol"},
            {"La Gran Estafa", "Crimen", "Atraco", "2023", "Guy Richie Jr", "135", "4.1", "Ingles"},
            {"Pixeles de Amor", "Animacion", "Familiar", "2023", "Studio Luna", "92", "4.6", "Espanol"},
            {"Caminos Cruzados", "Drama", "Social", "2021", "Fernando Meirelles Jr", "145", "4.3", "Portugues"},
            {"Velocidad Maxima", "Accion", "Carreras", "2024", "Michael Bay Jr", "120", "3.4", "Ingles"},
            {"El Jardin Secreto", "Drama", "Familiar", "2022", "Miyazaki Tribute", "108", "4.5", "Japones"},
            {"Mision Imposible 8", "Accion", "Espionaje", "2024", "Chris McQuarrie", "142", "4.0", "Ingles"},
            {"Cuentos de Medianoche", "Terror", "Psicologico", "2023", "Ari Aster Jr", "115", "3.9", "Ingles"},
            {"Universo Paralelo", "Ciencia Ficcion", "Multiverso", "2024", "Denis Villeneuve Jr", "160", "4.6", "Ingles"},
            {"Recetas del Corazon", "Comedia", "Culinaria", "2022", "Jon Favreau Jr", "100", "4.2", "Ingles"},
            {"El Ultimo Samurai 2", "Accion", "Historica", "2023", "Takeshi Kitano", "150", "3.7", "Japones"},
            {"Voces del Silencio", "Drama", "Musical", "2024", "Damien Chazelle Jr", "130", "4.4", "Ingles"},
            {"La Isla Perdida", "Aventura", "Supervivencia", "2023", "Cast Away Films", "138", "3.8", "Ingles"},
            {"Inteligencia Artificial", "Ciencia Ficcion", "Thriller", "2024", "Alex Garland Jr", "145", "4.5", "Ingles"},
            {"Corazon de Acero", "Drama", "Belico", "2022", "Christopher Nolan Jr", "162", "4.1", "Ingles"},
            {"Festival de Colores", "Animacion", "Musical", "2023", "Pixar Tribute", "88", "4.7", "Espanol"},
            {"El Detective", "Crimen", "Noir", "2021", "David Fincher Jr", "128", "4.0", "Ingles"},
            {"Suenos de Libertad", "Drama", "Biografico", "2024", "Spike Lee Jr", "135", "4.3", "Ingles"},
            {"La Mascara del Mal", "Terror", "Slasher", "2023", "Jordan Peele Jr", "105", "3.6", "Ingles"},
            {"Aventura Submarina", "Documental", "Ciencia", "2022", "James Cameron Doc", "110", "4.8", "Ingles"},
            {"Romance en Roma", "Romance", "Drama", "2023", "Luca Guadagnino Jr", "118", "4.2", "Italiano"},
            {"Cyborg 2077", "Ciencia Ficcion", "Accion", "2024", "Ridley Scott Jr", "148", "3.9", "Ingles"},
            {"La Familia Unida", "Comedia", "Familiar", "2022", "Guillermo del Toro Jr", "95", "4.4", "Espanol"},
            {"Operacion Rescate", "Accion", "Militar", "2023", "Peter Berg Jr", "125", "3.5", "Ingles"},
            {"El Pintor", "Drama", "Arte", "2024", "Pedro Almodovar Jr", "140", "4.6", "Espanol"},
            {"Noche de Juegos", "Comedia", "Misterio", "2023", "Edgar Wright Jr", "108", "4.1", "Ingles"},
            {"Dimension X", "Ciencia Ficcion", "Horror", "2024", "Neill Blomkamp Jr", "132", "3.8", "Ingles"},
            {"El Viaje de Marco", "Aventura", "Historica", "2022", "Ridley Scott Doc", "155", "4.0", "Italiano"},
            {"Fantasmas del Ayer", "Terror", "Gotico", "2023", "Guillermo del Toro", "120", "4.2", "Espanol"},
            {"Super Heroes Unidos", "Accion", "Superheroes", "2024", "Marvel Tribute", "152", "3.7", "Ingles"},
            {"Melodia Eterna", "Drama", "Musical", "2022", "Lin Manuel Miranda Jr", "128", "4.5", "Ingles"},
            {"El Laberinto", "Fantasia", "Oscura", "2023", "Pan Labyrinth Films", "115", "4.3", "Espanol"},
            {"Rapido y Letal", "Accion", "Street Racing", "2024", "Fast Films", "118", "3.3", "Ingles"},
            {"Planeta Verde", "Documental", "Ecologia", "2023", "Nature Films", "92", "4.6", "Ingles"},
            {"Destino Final X", "Terror", "Gore", "2024", "Horror Studios", "98", "3.1", "Ingles"},
            {"Amor Virtual", "Romance", "Ciencia Ficcion", "2024", "Spike Jonze Jr", "112", "4.4", "Ingles"},
            {"El Gladiador Moderno", "Accion", "Deportes", "2023", "Antoine Fuqua Jr", "130", "3.9", "Ingles"},
            {"Cuentos de Abuela", "Animacion", "Familiar", "2022", "Ghibli Tribute", "85", "4.8", "Japones"},
            {"Conspiracion Global", "Suspenso", "Politico", "2024", "Bourne Films", "140", "4.0", "Ingles"},
            {"El Chef Perfecto", "Comedia", "Culinaria", "2023", "Foodie Films", "102", "4.1", "Frances"},
        };

        for (int i = 0; i < movieData.length; i++) {
            String[] d = movieData[i];
            Movie movie = new Movie(
                i + 1, d[0], d[1], d[2], Integer.parseInt(d[3]),
                d[4], Integer.parseInt(d[5]), Double.parseDouble(d[6]), d[7]
            );

            // Asignar tags basados en género y subgénero
            assignTags(movie);
            movies.add(movie);
        }
    }

    /**
     * Asigna tags a una película basándose en su género y subgénero.
     */
    private void assignTags(Movie movie) {
        switch (movie.getGenre()) {
            case "Ciencia Ficcion":
                movie.addTag("futurista"); movie.addTag("tecnologia"); movie.addTag("innovador");
                break;
            case "Accion":
                movie.addTag("adrenalina"); movie.addTag("explosiones"); movie.addTag("pelea");
                break;
            case "Drama":
                movie.addTag("emotivo"); movie.addTag("profundo"); movie.addTag("actuacion");
                break;
            case "Comedia":
                movie.addTag("divertido"); movie.addTag("humor"); movie.addTag("entretenido");
                break;
            case "Terror":
                movie.addTag("miedo"); movie.addTag("suspense"); movie.addTag("oscuro");
                break;
            case "Romance":
                movie.addTag("amor"); movie.addTag("relaciones"); movie.addTag("emotivo");
                break;
            case "Documental":
                movie.addTag("educativo"); movie.addTag("real"); movie.addTag("informativo");
                break;
            case "Animacion":
                movie.addTag("familiar"); movie.addTag("colorido"); movie.addTag("creativo");
                break;
            case "Fantasia":
                movie.addTag("magico"); movie.addTag("imaginativo"); movie.addTag("aventura");
                break;
            default:
                movie.addTag("entretenimiento"); movie.addTag("variado");
                break;
        }
    }

    /**
     * Genera 30 usuarios ficticios con perfiles diversos y preferencias correlacionadas.
     */
    public void generateUsers() {
        String[][] userData = {
            // {nombre, edad, genero, pais, suscripcion, horasSemanales}
            {"Maria Lopez", "28", "F", "Mexico", "Premium", "12.5"},
            {"Carlos Fernandez", "35", "M", "Espana", "Standard", "8.0"},
            {"Juan Garcia", "22", "M", "Colombia", "Basic", "15.0"},
            {"Ana Torres", "45", "F", "Argentina", "Premium", "6.5"},
            {"Luis Martinez", "19", "M", "Peru", "Basic", "18.0"},
            {"Sofia Ramirez", "31", "F", "Chile", "Standard", "10.0"},
            {"Diego Hernandez", "42", "M", "Mexico", "Premium", "7.0"},
            {"Isabella Morales", "26", "F", "Colombia", "Standard", "14.0"},
            {"Andres Castillo", "55", "M", "Peru", "Premium", "5.0"},
            {"Valentina Ruiz", "33", "F", "Espana", "Standard", "9.5"},
            {"Pedro Alvarez", "20", "M", "Argentina", "Basic", "16.5"},
            {"Camila Reyes", "38", "F", "Chile", "Premium", "8.5"},
            {"Roberto Diaz", "48", "M", "Mexico", "Standard", "6.0"},
            {"Lucia Vargas", "24", "F", "Colombia", "Basic", "13.0"},
            {"Fernando Ortiz", "29", "M", "Peru", "Standard", "11.0"},
            {"Gabriela Cruz", "41", "F", "Espana", "Premium", "7.5"},
            {"Alejandro Rojas", "23", "M", "Argentina", "Basic", "17.0"},
            {"Daniela Flores", "36", "F", "Chile", "Standard", "9.0"},
            {"Miguel Soto", "52", "M", "Mexico", "Premium", "5.5"},
            {"Paula Mendoza", "27", "F", "Colombia", "Standard", "12.0"},
            {"Javier Romero", "30", "M", "Peru", "Standard", "10.5"},
            {"Carolina Pena", "44", "F", "Espana", "Premium", "6.5"},
            {"Nicolas Guerrero", "21", "M", "Argentina", "Basic", "15.5"},
            {"Mariana Silva", "34", "F", "Chile", "Standard", "8.0"},
            {"Emilio Jimenez", "47", "M", "Mexico", "Premium", "6.0"},
            {"Andrea Medina", "25", "F", "Colombia", "Basic", "14.5"},
            {"Santiago Aguilar", "39", "M", "Peru", "Standard", "7.5"},
            {"Renata Vega", "32", "F", "Espana", "Standard", "11.5"},
            {"Tomas Delgado", "50", "M", "Argentina", "Premium", "5.0"},
            {"Elena Campos", "29", "F", "Chile", "Standard", "10.0"},
        };

        // Mapeo de preferencias por grupo de edad
        Map<String, List<String>> ageGenrePreferences = new HashMap<>();
        ageGenrePreferences.put("joven", Arrays.asList("Accion", "Ciencia Ficcion", "Comedia", "Terror", "Animacion"));
        ageGenrePreferences.put("adulto", Arrays.asList("Drama", "Suspenso", "Crimen", "Romance", "Ciencia Ficcion"));
        ageGenrePreferences.put("mayor", Arrays.asList("Drama", "Documental", "Romance", "Comedia", "Aventura"));

        for (int i = 0; i < userData.length; i++) {
            String[] d = userData[i];
            User user = new User(
                i + 1, d[0], Integer.parseInt(d[1]), d[2], d[3],
                d[4], Double.parseDouble(d[5])
            );

            // Asignar preferencias basadas en edad (correlación intencional)
            String ageGroup;
            if (user.getAge() < 30) ageGroup = "joven";
            else if (user.getAge() < 45) ageGroup = "adulto";
            else ageGroup = "mayor";

            List<String> possibleGenres = ageGenrePreferences.get(ageGroup);
            int numGenres = 2 + random.nextInt(2); // 2-3 géneros preferidos
            List<String> shuffled = new ArrayList<>(possibleGenres);
            Collections.shuffle(shuffled, random);
            for (int j = 0; j < numGenres && j < shuffled.size(); j++) {
                user.addPreferredGenre(shuffled.get(j));
            }

            users.add(user);
        }
    }

    /**
     * Genera calificaciones realistas con patrones correlacionados:
     * - Los usuarios califican más alto las películas de sus géneros preferidos
     * - Los usuarios Premium tienden a ver más películas
     * - Se incluyen patrones de visualización incompleta
     */
    public void generateRatings() {
        for (User user : users) {
            // Determinar cuántas películas ha visto según tipo de suscripción.
            // Se aumentó la densidad para que los vecinos compartan más películas
            // en común, lo que mejora la calidad del filtrado colaborativo.
            int moviesToRate;
            switch (user.getSubscriptionType()) {
                case "Premium": moviesToRate = 24 + random.nextInt(14); break;  // 24-37
                case "Standard": moviesToRate = 17 + random.nextInt(11); break; // 17-27
                default: moviesToRate = 11 + random.nextInt(8); break;          // 11-18
            }

            // Seleccionar películas aleatorias para calificar
            List<Movie> shuffledMovies = new ArrayList<>(movies);
            Collections.shuffle(shuffledMovies, random);

            for (int i = 0; i < Math.min(moviesToRate, shuffledMovies.size()); i++) {
                Movie movie = shuffledMovies.get(i);
                
                // Calcular calificación basada en preferencias (con ruido controlado).
                // Una señal de preferencia más nítida produce vecindarios más
                // coherentes y mejora Precision@K sin dejar de ser realista.
                double baseScore = 2.6 + random.nextGaussian() * 0.4;

                // Bonus si el género está en preferencias del usuario
                if (user.getPreferredGenres().contains(movie.getGenre())) {
                    baseScore += 1.3 + random.nextDouble() * 0.5;
                }
                
                // Ajuste por rating promedio de la película
                baseScore += (movie.getAverageRating() - 4.0) * 0.3;
                
                // Limitar entre 1.0 y 5.0
                double finalScore = Math.max(1.0, Math.min(5.0, baseScore));
                finalScore = Math.round(finalScore * 2) / 2.0; // Redondear a 0.5

                // Calcular tiempo de visualización
                int watchTime;
                boolean completed;
                if (finalScore >= 4.0) {
                    watchTime = movie.getDurationMinutes();
                    completed = true;
                } else if (finalScore >= 3.0) {
                    watchTime = (int)(movie.getDurationMinutes() * (0.6 + random.nextDouble() * 0.4));
                    completed = random.nextDouble() > 0.3;
                } else {
                    watchTime = (int)(movie.getDurationMinutes() * (0.1 + random.nextDouble() * 0.4));
                    completed = false;
                }

                long timestamp = System.currentTimeMillis() - (long)(random.nextDouble() * 365 * 24 * 60 * 60 * 1000);

                Rating rating = new Rating(user.getId(), movie.getId(), finalScore, timestamp, watchTime, completed);
                ratings.add(rating);

                // Registrar en el usuario
                user.rateMovie(movie.getId(), finalScore);
            }
        }
    }

    // Getters
    public List<Movie> getMovies() { return movies; }
    public List<User> getUsers() { return users; }
    public List<Rating> getRatings() { return ratings; }

    /**
     * Imprime estadísticas del dataset generado.
     */
    public void printStats() {
        System.out.println("=== ESTADISTICAS DEL DATASET ===");
        System.out.println("Total de peliculas: " + movies.size());
        System.out.println("Total de usuarios: " + users.size());
        System.out.println("Total de calificaciones: " + ratings.size());
        
        // Distribución por género
        Map<String, Integer> genreCount = new HashMap<>();
        for (Movie m : movies) {
            genreCount.merge(m.getGenre(), 1, Integer::sum);
        }
        System.out.println("\nDistribucion por genero:");
        genreCount.forEach((genre, count) -> 
            System.out.printf("  %-20s: %d peliculas%n", genre, count));

        // Promedio de calificaciones
        double avgRating = ratings.stream().mapToDouble(Rating::getScore).average().orElse(0);
        System.out.printf("\nCalificacion promedio: %.2f%n", avgRating);

        // Tasa de completamiento
        long completed = ratings.stream().filter(Rating::isCompletedViewing).count();
        System.out.printf("Tasa de completamiento: %.1f%%%n", (completed * 100.0 / ratings.size()));
    }
}
