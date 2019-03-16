package Model;

public class Users_model {

    /**
     *Model Class of the RecyclerView which holds the Getters and Setter if the Individual Widgets for Ex.
     *
     * Textviews, ImageViews etc.
     */


    private String name;
    private String status;
    private String user_image;
    private String thumb_image;

    public Users_model() {
    }

    public Users_model(String name, String status, String user_image, String thumb_image) {
        this.name = name;
        this.status = status;
        this.user_image = user_image;
        this.thumb_image = thumb_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

}
