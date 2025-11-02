package model;

import org.apache.pdfbox.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.List;

public class PdfExporter implements ReportExporter {
    @Override
    public void export(List<AnomalyReport> reports, String filePath) {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Define Fonts and Layout
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            float fontSize = 10;
            float leading = 15; // Line spacing (1.5x font size)

            PDRectangle mediaBox = page.getMediaBox();
            float margin = 50; // 50 points (72 points = 1 inch)
            float startX = mediaBox.getLowerLeftX() + margin;
            float startY = mediaBox.getUpperRightY() - margin;

            float currentY = startY;

            // Start Writing
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);

            // Write a Title
            contentStream.setFont(fontBold, 16);
            contentStream.showText("Anomaly Report Export");
            contentStream.newLineAtOffset(0, -leading * 1.5f); // Move down
            currentY -= (leading * 1.5f);

            contentStream.setFont(font, fontSize);

            // --- Loop Through Reports ---
            for (AnomalyReport report : reports) {
                // Check if there is enough space for this report (~6 lines)
                if (currentY < (margin + (leading * 7))) {
                    contentStream.endText();
                    contentStream.close();

                    // Add a new blank page
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);

                    // Start a new content stream for the new page
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.beginText();
                    contentStream.setFont(font, fontSize);

                    // Reset Y position to the top margin
                    currentY = startY;
                    contentStream.newLineAtOffset(startX, currentY);
                }

                // Write the report data
                contentStream.showText("Anomaly ID: " + report.id().toString());
                contentStream.newLineAtOffset(0, -leading);
                currentY -= leading;

                contentStream.showText("Timestamp: " + report.timestamp().toString());
                contentStream.newLineAtOffset(0, -leading);
                currentY -= leading;

                contentStream.showText("Type: " + report.anomalyType());
                contentStream.newLineAtOffset(0, -leading);
                currentY -= leading;

                contentStream.showText("Drone ID: " + (report.droneId()));
                contentStream.newLineAtOffset(0, -leading);
                currentY -= leading;

                contentStream.showText("Simple Report: " + report.simpleReport());
                contentStream.newLineAtOffset(0, -leading);
                currentY -= leading;

                contentStream.showText("Detailed Report: " + report.detailedReport());
                contentStream.newLineAtOffset(0, -leading);
                currentY -= leading;

                contentStream.showText("-------------------------------------------------------------------");
                contentStream.newLineAtOffset(0, -leading);
                currentY -= leading;
            }

            // Finish and Save
            contentStream.endText();
            contentStream.close();

            document.save(filePath);
            System.out.println("Successfully exported " + reports.size() + " reports to " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing to PDF file: " +e.getMessage());
        }
    }
}
