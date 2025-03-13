package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    private Building b;
    private Request userRequest=new Request("door",b,true);
    private Request buildingRequest=new Request("bolt",b,false);

    @Test
    void getItem() {
        assertEquals("door",userRequest.getItem());
        assertEquals("bolt",buildingRequest.getItem());
    }

    @Test
    void getRequester() {
        assertEquals(b,userRequest.getRequester());
        assertEquals(b,buildingRequest.getRequester());
    }

    @Test
    void isUserRequest() {
        assertTrue(userRequest.isUserRequest());
        assertFalse(buildingRequest.isUserRequest());
    }
}