package rs.vs.meetings_service.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import rs.vs.meetings_service.dto.MeetingReportDto;

@Component
public class ExportServiceClient {

    private final WebClient webClient;
    private final String internalApiKey;

    public ExportServiceClient(@Value("${app.export-service.base-url}") String baseUrl,
                               @Value("${app.export-service.internal-api-key}") String internalApiKey){

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(5 * 1024 *1024))
                .build();
        this.internalApiKey = internalApiKey;
    }
    public byte[] exportPdf(MeetingReportDto report) {
        return call("/api/export/pdf", report);
    }

    public byte[] exportXslx(MeetingReportDto report) {
        return call("/api/export/xlsx",report);
    }

    private byte[] call(String path, MeetingReportDto report){
        return webClient.post()
                .uri(path)
                .header("X-Internal-Api-Key", internalApiKey)
                .bodyValue(report)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }


}
