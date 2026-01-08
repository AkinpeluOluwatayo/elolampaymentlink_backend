package enterprise.elroi.services.pdfGeneratorService;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import enterprise.elroi.model.Payment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    public byte[] generatePaymentReceipt(Payment payment) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            try {
                InputStream imgStream = new ClassPathResource("static/images/logo.png").getInputStream();
                ImageData imageData = ImageDataFactory.create(imgStream.readAllBytes());
                Image logo = new Image(imageData);
                logo.setWidth(80);
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(logo);
            } catch (Exception e) {
                System.err.println("Note: Logo not found");
            }

            document.add(new Paragraph("EL-OLAM SPECIAL HOME & REHABILITATION CENTER")
                    .setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Ability In Disability")
                    .setItalic().setFontSize(11).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("OFFICIAL PAYMENT RECEIPT")
                    .setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            float[] columnWidths = {1, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.useAllAvailableWidth();

            addTableRow(table, "Transaction Ref:", safeString(payment.getReference()));
            addTableRow(table, "Student Name:", safeString(payment.getStudentName()));
            addTableRow(table, "Amount Paid:", "NGN " + safeAmount(payment.getAmount()));
            addTableRow(table, "Payment Channel:", safeString(payment.getChannel()));

            String dateStr = payment.getPaidAt() != null ?
                    payment.getPaidAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")) :
                    "Not Available";
            addTableRow(table, "Date:", dateStr);

            document.add(table);

            document.add(new Paragraph("\nSTATUS: SUCCESSFUL")
                    .setBold().setFontSize(14).setFontColor(ColorConstants.GREEN));

            addWatermark(pdf);

            document.add(new Paragraph("\n\n"));

            Table containerTable = new Table(1)
                    .setWidth(UnitValue.createPercentValue(35))
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setMarginRight(0);

            Cell sigCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);

            try {
                InputStream sigStream = new ClassPathResource("static/images/signature.jpeg").getInputStream();
                ImageData sigData = ImageDataFactory.create(sigStream.readAllBytes());
                Image signature = new Image(sigData);
                signature.setWidth(100);
                signature.setMarginBottom(-12f);
                signature.setHorizontalAlignment(HorizontalAlignment.CENTER);
                sigCell.add(signature);
            } catch (Exception e) {
                System.err.println("Signature image not found");
            }

            sigCell.add(new Paragraph("__________________________").setMarginTop(0).setBold());
            containerTable.addCell(sigCell);

            containerTable.addCell(new Cell().add(new Paragraph("Amb. Dr. Mrs. Edward Grace")
                            .setBold().setFontSize(11).setMarginTop(2f))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER));

            containerTable.addCell(new Cell().add(new Paragraph("CEO, El-Olam Special Home")
                            .setFontSize(10).setMarginTop(-2f))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(containerTable);

            document.add(new Paragraph("\n\n\n\n"));
            document.add(new Paragraph("This is a computer-generated receipt and requires no physical stamp.")
                    .setFontSize(8).setItalic().setTextAlignment(TextAlignment.CENTER).setFontColor(ColorConstants.GRAY));

            document.close();
        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
        }

        return out.toByteArray();
    }

    private void addWatermark(PdfDocument pdf) {
        try {
            PdfPage page = pdf.getPage(1);
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdf);
            PdfExtGState gs1 = new PdfExtGState().setFillOpacity(0.12f);

            canvas.saveState()
                    .setExtGState(gs1)
                    .beginText()
                    .setFontAndSize(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD), 100)
                    .setFillColor(new DeviceRgb(0, 150, 0))
                    .setTextMatrix(1, 1, -1, 1, 150, 320)
                    .showText("EL-OLAM")
                    .endText()
                    .restoreState();
        } catch (Exception e) {
            System.err.println("Watermark error: " + e.getMessage());
        }
    }

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold().setFontSize(10)));
        table.addCell(new Cell().add(new Paragraph(value).setFontSize(10)));
    }

    private String safeString(String value) {
        return (value == null || value.trim().isEmpty()) ? "Not Available" : value;
    }

    private String safeAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(amount);
    }
}