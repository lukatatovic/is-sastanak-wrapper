package rs.vs.meetings_service.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import rs.vs.meetings_service.exception.ResourceNotFoundReception;

import java.util.Map;

@Component
public class AuthServiceClient {

    private final WebClient webClient;
    private final String internalApiKey;

    public AuthServiceClient(@Value("${app.auth-service.base-url}") String baseUrl,
                             @Value("${app.auth-service.internal-api-key}") String internalApiKey){
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public UserInfoDto getUser(Long userId) { return getUser(userId,null,null); }

    public UserInfoDto getUser(Long userId, Long meetingId, Long organizationalUnitId){
        try {
            return webClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/internal/users/{id}");
                        if(meetingId != null) builder.queryParam("meetingId", meetingId);
                        if(organizationalUnitId != null) builder.queryParam("organizationalUnitId", organizationalUnitId);
                        return builder.build(userId);
                    })
                    .header("X-Internal-Api-Key", internalApiKey)
                    .retrieve()
                    .bodyToMono(UserInfoDto.class)
                    .block();
        }catch (Exception e){
            throw new ResourceNotFoundReception("Korisnik ne postoji u auth-service");
        }
    }

    public void assignTemporaryRole(Long userId, String role, Long meetingId, String note) {
        try {
            var requestBody = Map.of(
                    "userId", userId,
                    "role", role,
                    "contextType", "meeting",
                    "meetingId", meetingId,
                    "organizationalUnitId", null,
                    "note", note
            );

            webClient.post()
                    .uri("/api/temporary-roles")
                    .header("X-Internal-Api-Key", internalApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Nije moguće automatski dodeliti privremenu ulogu: " + e.getMessage());
        }
    }
}
