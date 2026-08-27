package rs.vs.export_service.service;

import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
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

    public byte[] toXslx(MeetingReportDto report) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()){
            Sheet sheet =workbook.createSheet("Izvestaj");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Redni broj");
            header.createCell(1).setCellValue("Tacka dnevnog reda");
            header.createCell(2).setCellValue("Zakljucak");

            int rowIndex = 1;
            for(AgendaItemReportDto item : report.getAgendaItems()){
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(item.getOrderNum());
                row.createCell(1).setCellValue(item.getTitle());
                row.createCell(2).setCellValue(item.getConclusion() != null ? item.getConclusion() : "");
            }

            for(int i = 0; i<3 ; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();
        }catch (Exception ex){
            throw new RuntimeException("Greska pri generisanju XSLX", ex);
        }
    }

    public byte[] toDocx(MeetingReportDto report) {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()){
            addParagraph(doc, "Izvestaj sa sastanka: " + report.getTitle(), true);
            addParagraph(doc, "Datum: " + report.getScheduledDate() + " " + report.getScheduledTime(), false);
            addParagraph(doc, "Rukovodilac: " + report.getOrganizerFullName(), false);
            addParagraph(doc, "Zapisnicar: " + report.getRecorderFullName(), false);

            for(AgendaItemReportDto item : report.getAgendaItems()){
                addParagraph(doc, item.getOrderNum()+ " "+ item.getTitle(),true);
                if(report.isFullReport() && item.getDescription() != null){
                    addParagraph(doc,"Opis: " + item.getDescription(),false);
                }
                if(item.getConclusion() != null){
                    addParagraph(doc,"Zakljucak: " + item.getConclusion(), false);
                }
            }
            doc.write(out);
            return out.toByteArray();
        }catch (Exception ex){
            throw new RuntimeException("Greska pri generisanju DOCX", ex);
        }
    }

    private void addParagraph(XWPFDocument document, String text, boolean bold){
        XWPFParagraph p = document.createParagraph();
        XWPFRun run = p.createRun();
        run.setBold(bold);
        run.setText(text);
    }
}
