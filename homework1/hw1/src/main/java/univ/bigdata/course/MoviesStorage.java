package univ.bigdata.course;

import univ.bigdata.course.movie.Movie;
import univ.bigdata.course.movie.MovieReview;
import univ.bigdata.course.providers.MoviesProvider;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Main class which capable to keep all information regarding movies review.
 * Has to implements all methods from @{@link IMoviesStorage} interface.
 * Also presents functionality to answer different user queries, such as:
 * <p>
 * 1. Total number of distinct movies reviewed.
 * 2. Total number of distinct users that produces the review.
 * 3. Average review score for all movies.
 * 4. Average review score per single movie.
 * 5. Most popular movie reviewed by at least "K" unique users
 * 6. Word count for movie review, select top "K" words
 * 7. K most helpful users
 */
public class MoviesStorage implements IMoviesStorage {
	private LinkedList<MovieReview> movieReviews = new LinkedList<MovieReview>();
	private HashMap<String, Movie> movies = new HashMap<String, Movie>();
	
	
    public MoviesStorage(final MoviesProvider provider) {
        while (provider.hasMovie()) {
        	MovieReview mr = provider.getMovie();
        	movieReviews.add(mr);
        	// Also creating a list of Movies
        	Movie m = mr.getMovie();
        	// Creating a map: productId --> Movie
        	movies.put(m.getProductId(), m);	
        }  
        System.out.println("num of movies is: " + movies.size());
    }

    @Override
    public double totalMoviesAverageScore() {
        double sum = 0.0;
        for (Movie m : movies.values()) {
        	sum += m.getScore();
        }
        return sum / movies.size();
    }

    @Override
    public double totalMovieAverage(String productId) {
    	// The score is already computed in the movie object
    	return movies.get(productId).getScore();
    }

    @Override
    public List<Movie> getTopKMoviesAverage(long topK) {
    	Movie sortedMovies[] = movies.values().toArray(new Movie[movies.size()]);
    	// Sorting the array, comparing the movies by their score
    	Arrays.sort(sortedMovies, new Comparator<Movie>() {
			@Override
			public int compare(Movie arg0, Movie arg1) {
				double score0 = arg0.getScore();
				double score1 = arg1.getScore();
				if (score0 == score1) {
					// according to the description, in such case we need to sort lexicographically by productId
					return arg0.getProductId().compareTo(arg1.getProductId());
				}				
				return ((score0 < score1) ? -1 : 1);
			}    		
    	});
    	List<Movie> topKMovies = new LinkedList<Movie>();
    	int totalNumMovies = sortedMovies.length;
    	for (int i = 0 ; i < topK ; i++) {
    		topKMovies.add(sortedMovies[totalNumMovies - (int)topK + i]);
    	}
    	
    	return topKMovies;    	        
    }

    @Override
    public Movie movieWithHighestAverage() {
    	// Reusing already implemented method for K top movies and returning first element in the returned list
        List<Movie> top1Movies = getTopKMoviesAverage(1);
        return top1Movies.get(0);
    }

    @Override
    public List<Movie> getMoviesPercentile(double percentile) {
        throw new UnsupportedOperationException("You have to implement this method on your own.");
    }

    @Override
    public String mostReviewedProduct() {
        throw new UnsupportedOperationException("You have to implement this method on your own.");
    }

    @Override
    public Map<String, Long> reviewCountPerMovieTopKMovies(int topK) {
        throw new UnsupportedOperationException("You have to implement this method on your own.");
    }

    @Override
    public String mostPopularMovieReviewedByKUsers(int numOfUsers) {
        throw new UnsupportedOperationException("You have to implement this method on your own.");
    }

    @Override
    public Map<String, Long> moviesReviewWordsCount(int topK) {
        throw new UnsupportedOperationException("You have to implement this method on your own.");
    }

    @Override
    public Map<String, Long> topYMoviewsReviewTopXWordsCount(int topMovies, int topWords) {
        throw new UnsupportedOperationException("You have to implement this method on your own.");
    }

    @Override
    public Map<String, Double> topKHelpfullUsers(int k) {
        throw new UnsupportedOperationException("You have to implement this method on your own.");
    }

    @Override
    public long moviesCount() {
        throw new UnsupportedOperationException("You have to implement this method on your own.");
    }
}
