package univ.bigdata.course;

import univ.bigdata.course.movie.Movie;
import univ.bigdata.course.movie.MovieReview;
import univ.bigdata.course.providers.MoviesProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
	
	// Old implementation - start
	//private HashMap<String, Movie> movies = new HashMap<String, Movie>();
	// Old imlementation - end
	
    public MoviesStorage(final MoviesProvider provider) {
        while (provider.hasMovie()) {
        	MovieReview mr = provider.getMovie();
        	movieReviews.add(mr);
        	
        	// old implementation - start
        	// Also creating a list of Movies
        	//Movie m = mr.getMovie();
        	// Creating a map: productId --> Movie
        	//movies.put(m.getProductId(), m);
        	// old implementation = end
        }  
    }

    @Override
    public double totalMoviesAverageScore() {
    	// Note: I guess it should be the average of all the reviews - as implemented here
    	// and not the average of all the averages of all the movies
        double sum = 0.0;
        for (MovieReview mr : movieReviews) {
        	sum += mr.getMovie().getScore();
        }
        return sum / movieReviews.size();
    }

    @Override
    public double totalMovieAverage(String productId) {
    	double sum = 0.0;
    	int numRelevant = 0;
        for (MovieReview mr : movieReviews) {
        	if (mr.getMovie().getProductId().equals(productId)) {
        		sum += mr.getMovie().getScore();
        		numRelevant++;
        	}
        }
        return sum / numRelevant;
    }
    
    // Utility function
    private String[] getAllMovieProductIds() {
    	HashSet<String> uniqueProductIds = new HashSet<String>();
    	for (MovieReview mr : movieReviews) {
    		uniqueProductIds.add(mr.getMovie().getProductId());
    	}
    	
    	return uniqueProductIds.toArray(new String[uniqueProductIds.size()]);
    }

    @Override
    public List<Movie> getTopKMoviesAverage(long topK) {
    	String[] allMovieProductIds = getAllMovieProductIds();    	
    	Movie sortedMovies[] = new Movie[allMovieProductIds.length];
    	for (int i = 0 ; i < allMovieProductIds.length ; i++) {
    		String movieProductId = allMovieProductIds[i];
    		sortedMovies[i] = new Movie(movieProductId, totalMovieAverage(movieProductId));
    	}
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
    	HashMap<String,Integer> reviewsPerMovie = new HashMap<String,Integer>();
    	for (MovieReview mr : movieReviews) {
    		String pid = mr.getMovie().getProductId();
        	if (reviewsPerMovie.containsKey(pid) == false) {
        		reviewsPerMovie.put(pid, 0);
        	}
        	reviewsPerMovie.put(pid,reviewsPerMovie.get(pid)+1);
        }
    	
    	String mostReviewed = null;
    	int max = 0;
    	for (String id : reviewsPerMovie.keySet())
    	{
    		int currentValue=reviewsPerMovie.get(id);
    		if (currentValue>max) {
    			max=currentValue;
    			mostReviewed=id;
    		}
    	}
    	return mostReviewed;
    }

    @Override
    public Map<String, Long> reviewCountPerMovieTopKMovies(int topK) {
    	//create a map of id and number of reviews per movie
    	Map<String,Long> ReviewCount = new HashMap<String,Long>();
    	for (MovieReview mr : movieReviews) {
    		String pid = mr.getMovie().getProductId();
        	if (ReviewCount.containsKey(pid) == false) {
        		ReviewCount.put(pid, (long) 0);
        	}
        	ReviewCount.put(pid,ReviewCount.get(pid)+1);
        }
    	//sorting the map
    	Map<String,Long> TopKReview = new HashMap<String,Long>();
    	for(int i=0;i<topK; i++ )
    	{
	    	String reviewed=null;
	    	long max=0;
	    	for (String id : ReviewCount.keySet()){
	    		long count=ReviewCount.get(id);
	    		if (count>max) {
	    			max=count;
	    			reviewed=id;
	    		} 
	
	    	}
	    	ReviewCount.remove(reviewed, max);
	    	TopKReview.put(reviewed, max);
	    	}
    	return sortByComparator(TopKReview);  
    	}

    @Override
    public String mostPopularMovieReviewedByKUsers(int numOfUsers) {
        throw new UnsupportedOperationException("You have to implement this method on your own.");
    }

    @Override
    public Map<String, Long> moviesReviewWordsCount(int topK) {
    	Map<String, Long> wordsCountMap = new TreeMap<String, Long> ();
    	for (MovieReview mr : movieReviews) {
    		String review = mr.getReview();
    		String[] splitted = review.split("\\s+");
    		for (String word : splitted) {
    			if (wordsCountMap.containsKey(word) == false) {
    				wordsCountMap.put(word, (long)0);
            	}
    			wordsCountMap.put(word,wordsCountMap.get(word)+1);
    		}
    	}
    	
    	wordsCountMap = sortByComparator(wordsCountMap);
    	Iterator<Map.Entry<String, Long>> it = wordsCountMap.entrySet().iterator();
    	int i=0;
    	while(it.hasNext()) {
    	    i++;
    		it.next();
    	    if (i>topK) {
    	    	it.remove();
    	    }
    	}
    	return wordsCountMap;
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
    
    
    private static Map<String, Long> sortByComparator(Map<String, Long> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Long>> list = 
			new LinkedList<Map.Entry<String, Long>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
			public int compare(Map.Entry<String, Long> o1,
                                           Map.Entry<String, Long> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Long> sortedMap = new LinkedHashMap<String, Long>();
		for (Iterator<Map.Entry<String, Long>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Long> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}
