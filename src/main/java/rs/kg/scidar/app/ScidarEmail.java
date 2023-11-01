package rs.kg.scidar.app;

/**
 *
 * @author milos
 */
class ScidarEmail {

    private final String name;
    private final String email;
    private final String note;
    private final String title;
    private final String uri;

    ScidarEmail(String name, String email, String note, String title, String uri) {
        this.name = name;
        this.email = email;
        this.note = note;
        this.title = title;
        this.uri = uri;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getNote() {
        return this.note;
    }

    public String getTitle() {
        return this.title;
    }

    public String getUri() {
        return this.uri;
    }

}
