package edu.duke.ece651.factorysim;

import java.util.List;

/**
 * Implements Shortest Job First (SJF) request policy.
 */
public class SjfRequestPolicy implements RequestPolicy {
    @Override
    public Request popRequest(Building producer, List<Request> requests) {
        if (requests.isEmpty()) {
            return null;
        }
        Request shortestRequest = requests.get(0);
        for (Request request : requests) {
            if (request.getRecipe().getLatency() < shortestRequest.getRecipe().getLatency()) {
                shortestRequest = request;
            }
        }
        requests.remove(shortestRequest);
        return shortestRequest;
    }
}
