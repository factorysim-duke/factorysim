package edu.duke.ece651.factorysim.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServerMessageTest {
    
    @Test
    void testConstructor() {
        // Setup test data
        String testStatus = "success";
        String testMessage = "Operation completed";
        String testPayload = "{\"data\":\"result\"}";
        
        // Create the object
        ServerMessage message = new ServerMessage(testStatus, testMessage, testPayload);
        
        // Verify all fields are set correctly
        assertEquals(testStatus, message.status, "Status field should match the value passed to constructor");
        assertEquals(testMessage, message.message, "Message field should match the value passed to constructor");
        assertEquals(testPayload, message.payload, "Payload field should match the value passed to constructor");
    }
    
    @Test
    void testNullValues() {
        // Test with null values
        ServerMessage message = new ServerMessage(null, null, null);
        
        // Verify null values are set correctly
        assertNull(message.status, "Status field should be null");
        assertNull(message.message, "Message field should be null");
        assertNull(message.payload, "Payload field should be null");
    }
    
    @Test
    void testEmptyValues() {
        // Test with empty strings
        ServerMessage message = new ServerMessage("", "", "");
        
        // Verify empty strings are set correctly
        assertEquals("", message.status, "Status field should be an empty string");
        assertEquals("", message.message, "Message field should be an empty string");
        assertEquals("", message.payload, "Payload field should be an empty string");
    }
    
    @Test
    void testMixedValues() {
        // Test with a mix of values
        String testStatus = "error";
        String testMessage = "Operation failed";
        String testPayload = "";  // Empty string for payload
        
        ServerMessage message = new ServerMessage(testStatus, testMessage, testPayload);
        
        // Verify fields are set correctly
        assertEquals(testStatus, message.status, "Status field should match the value passed");
        assertEquals(testMessage, message.message, "Message field should match the value passed");
        assertEquals("", message.payload, "Payload field should be an empty string");
    }
} 