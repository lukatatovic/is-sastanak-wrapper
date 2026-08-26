package rs.vs.meetings_service.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import rs.vs.meetings_service.exception.ResourceNotFoundReception;

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
}
