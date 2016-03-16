package univ.bigdata.course.providers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import univ.bigdata.course.movie.Movie;
import univ.bigdata.course.movie.MovieReview;

public class FileIOMoviesProvider implements MoviesProvider {

	BufferedReader br = null;
	String nextString = null;
	boolean endOfFile = false;
	HashMap<String, Movie> movies = new HashMap<String, Movie>();
	HashMap<String, Integer> movieReviewCounters = new HashMap<String, Integer>();
	
	private void lazyDatafileLoadaer() throws Exception {
		if (br != null) {
			return;
		}
		FileReader fr = null;
		URL fileUrl = this.getClass().getResource("/movies-sample.txt");
		try {
			fr = new FileReader(fileUrl.getFile());
		} catch (FileNotFoundException e) {
			throw new Exception("File not found: " + e.getMessage());
		}
		br = new BufferedReader(fr);
	}
	
    @Override
    public boolean hasMovie() {
    	try {
			lazyDatafileLoadaer();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	try {
			nextString = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return (nextString != null);
    }
    
    private String getFieldValueFromString(String fieldStr) {
    	return (fieldStr.split(": "))[1].trim();
    }
    @Override
    public MovieReview getMovie() {
        MovieReview mr = new MovieReview();
        String[] fields = nextString.split("\\t");
        String productId = getFieldValueFromString(fields[0]);
        String userId = getFieldValueFromString(fields[1]);
        String profileName = getFieldValueFromString(fields[2]);
        String helpfulness = getFieldValueFromString(fields[3]);
        double score = Double.parseDouble(getFieldValueFromString(fields[4]));
        long time = Long.parseLong(getFieldValueFromString(fields[5]));
        String summary = getFieldValueFromString(fields[6]);
        String reviewText = getFieldValueFromString(fields[7]);
        
        if (! movies.containsKey(productId)) {
        	movies.put(productId, new Movie(productId, 0));
        	movieReviewCounters.put(productId, 0);
        }
        Movie movieObj = movies.get(productId);
        int lastNumReviews = movieReviewCounters.get(productId);
        movieObj.setScore((movieObj.getScore() * lastNumReviews + score) / (lastNumReviews + 1));
        movieReviewCounters.put(productId, lastNumReviews + 1);
        mr.setHelpfulness(helpfulness);
        mr.setMovie(movieObj);
        mr.setProfileName(profileName);
        mr.setTimestamp(new Date(time * 1000));
        mr.setUserId(userId);
        mr.setReview(reviewText);
        mr.setSummary(summary);
        
        return mr;
    }
    
    public static void main(String[] args) {
    	FileIOMoviesProvider fimp = new FileIOMoviesProvider();
    	while(fimp.hasMovie()) {
    		MovieReview mr = fimp.getMovie();
    		System.out.println(mr.toString());
    	}
    }
}
