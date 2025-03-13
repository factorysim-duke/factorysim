package edu.duke.ece651.factorysim;

/**
 * Represents a request for an item from a building.
 */
public class Request {
    private String item;
    private Building requester;
    private boolean isUserRequest;

    /**
     * Creates a new request.
     *
     * @param item the requested item.
     * @param requester the building making the request.
     * @param isUserRequest true if the request was made by a user from system.in.
     */
    public Request(String item, Building requester,boolean isUserRequest) {
        this.item = item;
        this.requester = requester;
        this.isUserRequest = isUserRequest;
    }

    /**
     * Gets the requested item name.
     *
     * @return the item name.
     */
    public String getItem() {
        return item;
    }

    /**
     * Gets the building making the request.
     *
     * @return the building.
     */
    public Building getRequester() {
        return requester;
    }

    /**
     * Checks if the request was made by a user.
     *
     * @return true if the request was made by a user from system.in
     */
    public boolean isUserRequest() {
        return isUserRequest;
    }
}
