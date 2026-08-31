package auth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class MeetingsServiceClient {

    private final WebClient webClient;
    private final String internalApiKey;

    public MeetingsServiceClient(@Value("${app.meetings-service.base-url}") String baseUrl, @Value("${app.internal-api-key}") String internalApiKey){
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public void notify(Long recipientUserId, String message, String type) {
        webClient.post().uri("/api/notifications/internal")
                .header("X-Internal-Api-Key", internalApiKey)
                .bodyValue(Map.of("recipientUserId", recipientUserId, "message", message, "type", type))
                .retrieve().toBodilessEntity().block();
    }
}
