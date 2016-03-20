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
	HashMap<String, String> fileKeysNextKey = null;
	
	private class Titles {
		static final String PRODUCT_ID = "product/productId: ";
		static final String USER_ID = "\treview/userId: ";
		static final String PROFILE_NAME = "\treview/profileName: ";
		static final String HELPFULNESS = "\treview/helpfulness: ";
		static final String SCORE = "\treview/score: ";
		static final String TIME = "\treview/time: ";
		static final String SUMMARY = "\treview/summary: ";
		static final String TEXT = "\treview/text: ";
	}
	
	
	private void lazyKeysMapSetter() {
		fileKeysNextKey = new HashMap<String, String>();
		fileKeysNextKey.put(Titles.PRODUCT_ID, Titles.USER_ID);
		fileKeysNextKey.put(Titles.USER_ID, Titles.PROFILE_NAME);
		fileKeysNextKey.put(Titles.PROFILE_NAME, Titles.HELPFULNESS);
		fileKeysNextKey.put(Titles.HELPFULNESS, Titles.SCORE);
		fileKeysNextKey.put(Titles.SCORE, Titles.TIME);
		fileKeysNextKey.put(Titles.TIME, Titles.SUMMARY);
		fileKeysNextKey.put(Titles.SUMMARY, Titles.TEXT);
		// Not defining review-text to point to null, as it behaves this way anyway when the key is not set
	}
	
	private String getFieldValueFromLine(String line, String fieldTitle) {
		String[] parts = line.split(fieldTitle);
		String value = parts[1];
		String nextKey = fileKeysNextKey.get(fieldTitle);
		if (nextKey != null) {
			value = (value.split(nextKey))[0];
		}
		return value.trim();
	}
	
	private void lazyDatafileLoadaer() throws Exception {
		if (br != null) {
			return;
		}
		lazyKeysMapSetter();
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
        //String[] fields = nextString.split("\\t");
        String productId = getFieldValueFromLine(nextString, Titles.PRODUCT_ID);
        String userId = getFieldValueFromLine(nextString, Titles.USER_ID);
        String profileName = getFieldValueFromLine(nextString, Titles.PROFILE_NAME);
        String helpfulness = getFieldValueFromLine(nextString, Titles.HELPFULNESS);
        double score = Double.parseDouble(getFieldValueFromLine(nextString, Titles.SCORE));
        long time = Long.parseLong(getFieldValueFromLine(nextString, Titles.TIME));
        String summary = getFieldValueFromLine(nextString, Titles.SUMMARY);
        String reviewText = getFieldValueFromLine(nextString, Titles.TEXT);
        
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
