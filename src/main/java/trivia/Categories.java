package trivia;

public enum Categories {

    POP ("Pop"),
    SCIENCE ("Science"),
    SPORTS ("Sports"),
    ROCK ("Rock"),
    GEOGRAPHY ("Geography");

    private final String name;

    Categories (String name) {
        this.name = name;
    }

    public String value () {
        return this.name;
    }
}
