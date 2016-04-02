/*
 * Name1; Daphna Kopel id;	209051036
 * Name2; Tal Hakim id; 	301013439
 * Name3; Omri Cahen id; 	200736064
 * Name4; Itay Segev id; 	209146067
 */
package univ.bigdata.course.movie;

public class Movie {

    private String productId;

    private double score;

    public Movie() {
    }

    public Movie(String productId, double score) {
        this.productId = productId;
        this.score = score;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "productId='" + productId + '\'' +
                ", score=" + score +
                '}';
    }

}
