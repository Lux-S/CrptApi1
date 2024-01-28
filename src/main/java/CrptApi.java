import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private final Semaphore requestSemaphore;
    private final ObjectMapper objectMapper;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestSemaphore = new Semaphore(requestLimit);
        this.objectMapper = new ObjectMapper();
    }

    public void createDocument(String documentJson, String signature) {
        try {
            requestSemaphore.acquire();

            ObjectNode documentNode = objectMapper.readValue(documentJson, ObjectNode.class);

                        String apiUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(apiUrl);

                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Signature", signature);

                httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(documentNode)));

                HttpResponse response = httpClient.execute(httpPost);

                System.out.println("API Response: " + response.getStatusLine().getStatusCode());
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error during API call: " + e.getMessage());
        } finally {
            requestSemaphore.release();
        }
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5);

        for (int i = 0; i < 10; i++) {
            String documentJson = "{\"description\": {\"participantInn\": \"string\"}, " +
                    "\"doc_id\": \"string\", " +
                    "\"doc_status\": \"string\", " +
                    "\"doc_type\": \"LP_INTRODUCE_GOODS\", " +
                    "\"importRequest\": true, " +
                    "\"owner_inn\": \"string\", " +
                    "\"participant_inn\": \"string\", " +
                    "\"producer_inn\": \"string\", " +
                    "\"production_date\": \"2020-01-23\", " +
                    "\"production_type\": \"string\", " +
                    "\"products\": [{\"certificate_document\": \"string\", " +
                    "\"certificate_document_date\": \"2020-01-23\", " +
                    "\"certificate_document_number\": \"string\", " +
                    "\"owner_inn\": \"string\", " +
                    "\"producer_inn\": \"string\", " +
                    "\"production_date\": \"2020-01-23\", " +
                    "\"tnved_code\": \"string\", " +
                    "\"uit_code\": \"string\", " +
                    "\"uitu_code\": \"string\"}], " +
                    "\"reg_date\": \"2020-01-23\", " +
                    "\"reg_number\": \"string\"}";

            String signature = "sampleSignature";

            crptApi.createDocument(documentJson, signature);
        }
    }
}