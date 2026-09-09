/**
 * Represents one local BridgeTut user profile.
 */
public class User {

    private final String userId;
    private final String username;

    /**
     * Creates a user profile.
     *
     * @param userId   unique database ID
     * @param username visible profile name
     */
    public User(String userId, String username) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("The user ID cannot be null or blank.");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("The username cannot be null or blank.");
        }

        this.userId = userId.trim();
        this.username = username.trim();
    }

    /**
     * Returns the user’s unique database ID.
     *
     * @return user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the user’s visible profile name.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Controls how the user is displayed by Swing components.
     *
     * @return visible username
     */
    @Override
    public String toString() {
        return username;
    }
}