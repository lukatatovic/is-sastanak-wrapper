package rs.vs.export_service.service;

import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import rs.vs.export_service.dto.AgendaItemReportDto;
import rs.vs.export_service.dto.MeetingReportDto;

import java.io.ByteArrayOutputStream;
import com.lowagie.text.Document;

@Service
public class ExportService {
    public byte[] toPdf(MeetingReportDto report) {
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document,out);
            document.open();
            document.add(new Paragraph("Izvestaj sa sastanka: "+ report.getTitle()));
            document.add(new Paragraph("Datum: "+ report.getScheduledDate() + " "+ report.getScheduledTime()));
            document.add(new Paragraph("Rukovodilac: "+ report.getOrganizerFullName()));
            document.add(new Paragraph("Zapisnicar: "+ report.getRecorderFullName()));
            document.add(new Paragraph(" "));

            for(AgendaItemReportDto item : report.getAgendaItems()){
                document.add(new Paragraph(item.getOrderNum() + ". " + item.getTitle()));
                if(report.isFullReport() && item.getDescription() != null){
                    document.add(new Paragraph(" Opis: " + item.getDescription()));
                }
                if(item.getConclusion() != null){
                    document.add(new Paragraph(" Zakljucak: " + item.getConclusion()));
                }

            }

            if(report.getFinalConclusion() != null){
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Zakljucak sastanka: " + report.getFinalConclusion()));
            }

            document.close();
            return out.toByteArray();
        }catch (Exception ex){
            throw new RuntimeException("Greska pri generisanju PDF", ex);
        }
    }
}
