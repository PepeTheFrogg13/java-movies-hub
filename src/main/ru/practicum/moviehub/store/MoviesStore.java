package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MoviesStore {

    private Long idIterator;

    private final HashMap<Long, Movie> movieList;

    public MoviesStore() {
        this.idIterator = 0L;
        movieList = new HashMap<>();
    }

    public Movie addMovie(String title, int year){
        Long id = ++idIterator;
        Movie movie = new Movie(id,title,year);
        movieList.put(id,movie);
        return movie;
    }

    public List<Movie> findAll() {
        return new ArrayList<>(movieList.values());
    }

    public Optional<Movie> findById(Long id) {
        return Optional.ofNullable(movieList.get(id));
    }

    public List<Movie> findByYear(Integer year){
        return movieList.values()
                .stream()
                .filter(m -> m.getYear().equals(year))
                .toList();
    }

    public void deleteById(Long id){
        movieList.remove(id);
    }

    public void clearList(){
        movieList.clear();
    }

}