/*
 * Name1; Daphna Kopel id;	209051036
 * Name2; Tal Hakim id; 	301013439
 * Name3; Omri Cahen id; 	200736064
 * Name4; Itay Segev id; 	209146067
 */
package univ.bigdata.course.providers;

import univ.bigdata.course.movie.MovieReview;

public interface MoviesProvider {

    boolean hasMovie();

    MovieReview getMovie();
}
