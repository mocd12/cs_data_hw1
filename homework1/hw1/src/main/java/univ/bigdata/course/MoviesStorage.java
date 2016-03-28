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
    	// Floor is good. Example: 10 movies, 0.35 percentile - starting from index 3
    	// Starting from index 3 means we "leave behind" 0.3 of the movies and less than 0.4 of the movies
    	int startFrom = (int)(sortedMovies.length * percentile);
    	Movie[] outputMovies = Arrays.copyOfRange(sortedMovies, startFrom, sortedMovies.length);
    	
    	return Arrays.asList(outputMovies);    	
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
    	//put the K top reviewed movies in a map
    	Map<String,Long> TopKReview = new HashMap<String,Long>();
    	for(int i=0;i<topK; i++ )
    	{
    		//find the most reviewed movie in the map
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
    	//sorting the map
    	return sortByComparator(TopKReview);  
    }

    @Override
    public String mostPopularMovieReviewedByKUsers(int numOfUsers) {
String mostPM ="There is no such movie";
    	
    	Map<String,Integer> reviewsPerMovie = new HashMap<String,Integer>();
    	Map<String,Integer> reviewsPerMovieBnum = new HashMap<String,Integer>();
    	
    	for (MovieReview mr : movieReviews) {
    		String pid = mr.getMovie().getProductId();
        	if (reviewsPerMovie.containsKey(pid) == false) {
        		reviewsPerMovie.put(pid, 0);
        	}
        	reviewsPerMovie.put(pid,reviewsPerMovie.get(pid)+1);
        }
    	
    	for(String currentKey : reviewsPerMovie.keySet()){
    		if(reviewsPerMovie.get(currentKey)>=numOfUsers)
    		{reviewsPerMovieBnum.put(currentKey,reviewsPerMovie.get(currentKey));}
    	}
    	
    	double maxscore=0.0;
    	
    	for (MovieReview mr : movieReviews) {
    		if(reviewsPerMovieBnum.containsKey(mr.getMovie().getProductId())){
    		 if(totalMovieAverage(mr.getMovie().getProductId())>maxscore){
    			maxscore=mr.getMovie().getScore();
    			mostPM=mr.getMovie().getProductId();
    			}
    		}
    	}
    	return mostPM;
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
    	LinkedList<MovieReview> tempx =new LinkedList<MovieReview>(movieReviews);
    	Map<String, Double> re=new HashMap<String, Double>();
        Map<String, Double> topk=new HashMap<String, Double>();
    	
    while(!tempx.isEmpty()){
    	String name=tempx.getFirst().getUserId();
    	
    	if(!re.containsKey(name))
    	{
    		re.put(name, 0.0);
    	}
    	tempx.removeFirst();
    	}
    
    for(String Nuser:re.keySet()){
    	Double a=0.0;
    	Double b=0.0;
    	Double v=0.0;
    	String[] tm;
    	for(MovieReview mr : movieReviews){
    		if(mr.getUserId().equals(Nuser)){
    			tm=mr.getHelpfulness().split("/");
    			a+=Integer.parseInt(tm[0]);
    			b+=Integer.parseInt(tm[1]);		
    		}
    	}
    	if(a==0||b==0){
			v=0.0;
		}else{
		v=a/b;}
		
		re.put(Nuser, v);
    }
    if(k>=re.size()){
    	k=re.size();}
    
    re=sortD(re);
    
    	for(int i=0;i<k;i++){
    		Double max=-1.0;
    		String idmax="";
    		for(String s:re.keySet()){
    			if(re.get(s)>max){
    				max=re.get(s);
    				idmax=s;
    			}
    		}
    		topk.put(idmax, re.get(idmax));
    		
    		re.remove(idmax);
    	}
    	
    
    	return sortD(topk);
    }

    @Override
    public long moviesCount() {
    	LinkedList<String> MovieNum = new LinkedList<String>();
    	//create a list of the movies 
    	for (MovieReview mr : movieReviews) {
    		String pid = mr.getMovie().getProductId();
        	if (MovieNum.contains(pid) == false) {
        		MovieNum.add(pid);
        	}
        }
    	//return the size of the list
    	return MovieNum.size();
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




private static Map<String, Double> sortD(Map<String, Double> origMap) {


Map<Double, LinkedList<String>> reverseMap = new HashMap<Double, LinkedList<String>>() ;
Map<String, Double> retmap = new LinkedHashMap<String, Double>() ;

for (String key : origMap.keySet()) {
   double val = origMap.get(key);
   if (! reverseMap.containsKey(val)) {
        reverseMap.put(val, new LinkedList<String>()) ;

    } 
    reverseMap.get(val).add(key);
} 

Double[] vals = reverseMap.keySet().toArray(new Double[reverseMap.size()]);


Arrays.sort(vals, Collections.reverseOrder());

for (Double val : vals) {
	
	
    LinkedList<String> keys = reverseMap.get(val);
    
    
    Collections.sort(keys, new Comparator<String>(){
        @Override
            public int compare(String o1,String o2){
                return o1.compareTo(o2);
            }
        });
    
    for (String key : keys)  {
        retmap.put(key, val);
    } 
}
return retmap;
}
}
