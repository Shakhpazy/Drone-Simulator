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

/**
 * A class to compile AnomalyReports into a PDF file.
 * @author nlevin11
 * @version 11-6
 */
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

            PdfContext ctx = new PdfContext(document, page, contentStream, startX, startY, margin, leading, font,
                    fontSize);

            // Start Writing
            contentStream.beginText();
            contentStream.newLineAtOffset(ctx.startX, ctx.startY);

            // Write a Title
            contentStream.setFont(fontBold, 16);
            contentStream.showText("Anomaly Report Export");
            contentStream.newLineAtOffset(0, -leading * 1.5f); // Move down
            ctx.currentY -= (leading * 1.5f);
            contentStream.setFont(font, fontSize);

            // --- Loop Through Reports ---
            for (AnomalyReport report : reports) {

                checkPageBreak(ctx, 4);

                writeLine(ctx, "Anomaly ID: " + report.id().toString());
                writeLine(ctx, "Timestamp: " + report.timestamp().toString());
                writeLine(ctx, "Type: " + report.anomalyType());
                writeLine(ctx, "Drone ID: " + report.droneId());

                writeMultiLine(ctx, "Simple Report: ", report.simpleReport());
                writeMultiLine(ctx, "Detailed Report: ", report.detailedReport());

                writeLine(ctx, "--------------------------------------------------------------------------------");
            }

            ctx.contentStream.endText();
            ctx.contentStream.close();
            document.save(filePath);
            System.out.println("Successfully exported " + reports.size() + " reports to " + filePath);

        } catch (IOException e) {
            System.err.println("Error writing to PDF file: " +e.getMessage());
        }
    }

    private void checkPageBreak(PdfContext ctx, int linesNeeded) throws IOException {
        float spaceNeeded = linesNeeded * ctx.leading;

        if (ctx.currentY - spaceNeeded < ctx.margin) {
            ctx.contentStream.endText();
            ctx.contentStream.close();

            PDPage newPage = new PDPage(PDRectangle.A4);
            ctx.document.addPage(newPage);
            ctx.page = newPage;
            ctx.contentStream = new PDPageContentStream(ctx.document, newPage);
            ctx.contentStream.beginText();
            ctx.contentStream.setFont(ctx.font, ctx.fontSize);

            ctx.currentY = ctx.startY;
            ctx.contentStream.newLineAtOffset(ctx.startX, ctx.currentY);
        }
    }

    private void writeLine(PdfContext ctx, String text) throws IOException {
        checkPageBreak(ctx, 1);
        ctx.contentStream.showText(text);
        ctx.contentStream.newLineAtOffset(0, -ctx.leading);
        ctx.currentY -= ctx.leading;
    }

    private void writeMultiLine(PdfContext ctx, String label, String text) throws IOException{
        checkPageBreak(ctx, 1);
        ctx.contentStream.showText(label);
        ctx.contentStream.newLineAtOffset(0, -ctx.leading);
        ctx.currentY -= ctx.leading;

        String[] lines = text.replace("\r", "").split("\n");
        for (String line : lines) {
            checkPageBreak(ctx, 1);
            ctx.contentStream.showText(line);
            ctx.contentStream.newLineAtOffset(0, -ctx.leading);
            ctx.currentY -= ctx.leading;
        }
    }

    private static class PdfContext {
        PDDocument document;
        PDPage page;
        PDPageContentStream contentStream;
        float currentY;

        final float startX, startY, margin, leading, fontSize;

        final PDType1Font font;

        public PdfContext(PDDocument doc, PDPage page, PDPageContentStream stream, float startX, float startY, float margin, float leading, PDType1Font font, float fontSize) {
            this.document = doc;
            this.page = page;
            this.contentStream = stream;
            this.currentY = startY;
            this.startX = startX;
            this.startY = startY;
            this.margin = margin;
            this.leading = leading;
            this.font = font;
            this.fontSize = fontSize;
        }
    }
}
