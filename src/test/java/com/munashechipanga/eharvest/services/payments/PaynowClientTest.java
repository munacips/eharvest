package com.munashechipanga.eharvest.services.payments;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaynowClientTest {

    @Test
    void parseResponseFieldsParsesFormEncodedResponse() {
        PaynowClient client = new PaynowClient();

        Map<String, String> parsed = client.parseResponseFields(
                "status=Ok&browserurl=https%3A%2F%2Fexample.com%2Fpay&pollurl=https%3A%2F%2Fexample.com%2Fpoll&hash=ABC123"
        );

        assertEquals("Ok", parsed.get("status"));
        assertEquals("https://example.com/pay", parsed.get("browserurl"));
        assertEquals("https://example.com/poll", parsed.get("pollurl"));
        assertEquals("ABC123", parsed.get("hash"));
    }

    @Test
    void parseResponseFieldsParsesHtmlHiddenInputs() {
        PaynowClient client = new PaynowClient();

        Map<String, String> parsed = client.parseResponseFields("""
                <html>
                  <body>
                    <form>
                      <input type="hidden" name="status" value="Ok">
                      <input type="hidden" name="browserurl" value="https://example.com/pay?x=1&amp;y=2">
                      <input type="hidden" name="pollurl" value='https://example.com/poll'>
                      <input type="hidden" name="hash" value="ABC123">
                    </form>
                  </body>
                </html>
                """);

        assertEquals("Ok", parsed.get("status"));
        assertEquals("https://example.com/pay?x=1&y=2", parsed.get("browserurl"));
        assertEquals("https://example.com/poll", parsed.get("pollurl"));
        assertEquals("ABC123", parsed.get("hash"));
    }

    @Test
    void generateHashIsDeterministicForDifferentMapOrders() throws Exception {
        PaynowClient client = new PaynowClient();
        setIntegrationKey(client, "secret-key");

        Map<String, String> first = new LinkedHashMap<>();
        first.put("reference", "TXN-1");
        first.put("amount", "20.00");
        first.put("status", "Ok");

        Map<String, String> second = new LinkedHashMap<>();
        second.put("status", "Ok");
        second.put("amount", "20.00");
        second.put("reference", "TXN-1");

        String firstHash = invokeGenerateHash(client, first);
        String secondHash = invokeGenerateHash(client, second);

        assertTrue(client.isHashValid(addHash(first, firstHash)));
        assertFalse(firstHash.equals(secondHash));
        assertFalse(client.isHashValid(addHash(first, "WRONG")));
    }

    private Map<String, String> addHash(Map<String, String> values, String hash) {
        Map<String, String> copy = new LinkedHashMap<>(values);
        copy.put("hash", hash);
        return copy;
    }

    private void setIntegrationKey(PaynowClient client, String value) throws Exception {
        Field field = PaynowClient.class.getDeclaredField("integrationKey");
        field.setAccessible(true);
        field.set(client, value);
    }

    private String invokeGenerateHash(PaynowClient client, Map<String, String> values) throws Exception {
        Method method = PaynowClient.class.getDeclaredMethod("generateHash", Map.class);
        method.setAccessible(true);
        return (String) method.invoke(client, values);
    }
}



