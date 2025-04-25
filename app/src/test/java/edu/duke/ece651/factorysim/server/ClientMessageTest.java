package edu.duke.ece651.factorysim.server;

import edu.duke.ece651.factorysim.client.ClientMessage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientMessageTest {
    
    @Test
    void testConstructor() {
        // Setup test data
        String testType = "command";
        String testUser = "user123";
        String testPayload = "{\"action\":\"start\"}";
        
        // Create the object
        ClientMessage message = new ClientMessage(testType, testUser, testPayload);
        
        // Verify all fields are set correctly
        assertEquals(testType, message.type, "Type field should match the value passed to constructor");
        assertEquals(testUser, message.user, "User field should match the value passed to constructor");
        assertEquals(testPayload, message.payload, "Payload field should match the value passed to constructor");
    }
    
    @Test
    void testNullValues() {
        // Test with null values
        ClientMessage message = new ClientMessage(null, null, null);
        
        // Verify null values are set correctly
        assertNull(message.type, "Type field should be null");
        assertNull(message.user, "User field should be null");
        assertNull(message.payload, "Payload field should be null");
    }
    
    @Test
    void testEmptyValues() {
        // Test with empty strings
        ClientMessage message = new ClientMessage("", "", "");
        
        // Verify empty strings are set correctly
        assertEquals("", message.type, "Type field should be an empty string");
        assertEquals("", message.user, "User field should be an empty string");
        assertEquals("", message.payload, "Payload field should be an empty string");
    }
    
    @Test
    void testMixedWithEmptyString() {
        // Test with a mix of values where one is an empty string
        String testType = "update";
        String testUser = "";  // Empty string for user
        String testPayload = "{\"data\":\"value\"}";
        
        ClientMessage message = new ClientMessage(testType, testUser, testPayload);
        
        // Verify fields are set correctly
        assertEquals(testType, message.type, "Type field should match the value passed");
        assertEquals("", message.user, "User field should be an empty string");
        assertEquals(testPayload, message.payload, "Payload field should match the value passed");
    }
} 