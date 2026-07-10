package com.school.sis.report.service;

import com.school.sis.report.config.SchoolProperties;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class PdfReportBuilder implements AutoCloseable {
    private static final float MARGIN = 48f;
    private static final float BOTTOM_MARGIN = 48f;
    private static final float LEADING = 14f;
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private final PDDocument document = new PDDocument();
    private final SchoolProperties schoolProperties;
    private final String generatedBy;
    private final PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private PDPage page;
    private PDPageContentStream content;
    private float y;
    private int pageNumber = 0;

    PdfReportBuilder(SchoolProperties schoolProperties, String generatedBy) {
        this.schoolProperties = schoolProperties;
        this.generatedBy = blank(generatedBy) ? "System" : generatedBy;
    }

    void start(String title) {
        addPage();
        text(title, 16, bold);
        gap(8);
    }

    void section(String title) {
        gap(6);
        text(title, 11, bold);
        line();
    }

    void field(String label, Object value) {
        text(label + ": " + value(value), 9, regular);
    }

    void paragraph(String value) {
        for (String line : wrap(value(value), regular, 9, contentWidth())) {
            text(line, 9, regular);
        }
    }

    void table(String[] headers, List<String[]> rows) {
        ensureSpace(LEADING * 3);
        text(String.join(" | ", headers), 8, bold);
        line();
        if (rows.isEmpty()) {
            text("No records found.", 8, regular);
            return;
        }
        for (String[] row : rows) {
            String joined = String.join(" | ", Arrays.stream(row).map(this::value).toList());
            for (String wrapped : wrap(joined, regular, 8, contentWidth())) {
                text(wrapped, 8, regular);
            }
        }
    }

    byte[] finish() {
        try {
            closeContent();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create PDF report", exception);
        }
    }

    private void addPage() {
        try {
            closeContent();
            page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            pageNumber++;
            content = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - MARGIN;
            header();
            footer();
            y -= 20;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to add PDF page", exception);
        }
    }

    private void header() throws IOException {
        if (!blank(schoolProperties.getLogoPath())) {
            Path logo = Path.of(schoolProperties.getLogoPath());
            if (Files.isRegularFile(logo)) {
                PDImageXObject image = PDImageXObject.createFromFile(logo.toString(), document);
                content.drawImage(image, MARGIN, y - 28, 32, 32);
            }
        }
        writeAt(value(schoolProperties.getName()), MARGIN + 42, y, 12, bold);
        if (!blank(schoolProperties.getAddress())) {
            writeAt(schoolProperties.getAddress(), MARGIN + 42, y - 13, 8, regular);
        }
        if (!blank(schoolProperties.getContact())) {
            writeAt(schoolProperties.getContact(), MARGIN + 42, y - 24, 8, regular);
        }
        y -= 42;
        line();
    }

    private void footer() throws IOException {
        float footerY = 28;
        writeAt("Generated " + DATE_TIME.format(Instant.now()) + " by " + generatedBy, MARGIN, footerY, 7, regular);
        writeAt("Page " + pageNumber, page.getMediaBox().getWidth() - MARGIN - 40, footerY, 7, regular);
    }

    private void line() {
        try {
            ensureSpace(8);
            content.moveTo(MARGIN, y);
            content.lineTo(page.getMediaBox().getWidth() - MARGIN, y);
            content.stroke();
            y -= 8;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to draw PDF line", exception);
        }
    }

    private void text(String value, float size, PDFont font) {
        try {
            ensureSpace(LEADING);
            writeAt(value(value), MARGIN, y, size, font);
            y -= LEADING;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write PDF text", exception);
        }
    }

    private void writeAt(String value, float x, float y, float size, PDFont font) throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(sanitize(value));
        content.endText();
    }

    private void gap(float amount) {
        ensureSpace(amount);
        y -= amount;
    }

    private void ensureSpace(float needed) {
        if (page == null) {
            addPage();
            return;
        }
        if (y - needed < BOTTOM_MARGIN) {
            addPage();
        }
    }

    private float contentWidth() {
        return page.getMediaBox().getWidth() - (MARGIN * 2);
    }

    private List<String> wrap(String text, PDFont font, float size, float width) {
        List<String> lines = new ArrayList<>();
        String[] words = sanitize(text).split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (textWidth(candidate, font, size) <= width) {
                line = new StringBuilder(candidate);
            } else {
                if (!line.isEmpty()) lines.add(line.toString());
                line = new StringBuilder(word);
            }
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines.isEmpty() ? List.of("") : lines;
    }

    private float textWidth(String value, PDFont font, float size) {
        try {
            return font.getStringWidth(value) / 1000f * size;
        } catch (IOException exception) {
            return value.length() * size * 0.5f;
        }
    }

    private String sanitize(String value) {
        return value(value).replaceAll("[^\\x09\\x0A\\x0D\\x20-\\x7E]", "?");
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private void closeContent() throws IOException {
        if (content != null) {
            content.close();
            content = null;
        }
    }

    @Override
    public void close() {
        try {
            closeContent();
            document.close();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to close PDF report", exception);
        }
    }
}
