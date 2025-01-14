import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;

public class PDFWithJavaScript {

    public static void main(String[] args) {
        String outputFilePath = "JavaScriptSample.pdf";

        try {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFilePath));
            document.open();

            // Add a title to the PDF
            document.add(new Paragraph("PDF with Embedded JavaScript"));

            // Add Document-Level JavaScript
            addDocumentLevelJavaScript(writer);

            // Add an Annotation with JavaScript
            addAnnotationWithJavaScript(writer, document);

            // Add a Form Field with JavaScript
            addFormFieldWithJavaScript(writer, document);

            document.close();
            System.out.println("PDF created with embedded JavaScript: " + outputFilePath);

        } catch (Exception e) {
            System.err.println("Error creating PDF: " + e.getMessage());
        }
    }

    // Method to add document-level JavaScript
    private static void addDocumentLevelJavaScript(PdfWriter writer) {
        String documentJS = "app.alert('This is document-level JavaScript executed when the PDF opens!');";
        PdfAction jsAction = PdfAction.javaScript(documentJS, writer);
        writer.addJavaScript(jsAction);
    }

    // Method to add an annotation with JavaScript
    private static void addAnnotationWithJavaScript(PdfWriter writer, Document document) throws DocumentException {
        PdfAnnotation annotation = PdfAnnotation.createText(
                writer,
                new Rectangle(100, 700, 300, 750),
                "Annotation with JavaScript",
                "Click this annotation to execute JavaScript",
                true,
                null
        );

        // Add JavaScript action to the annotation
        String annotationJS = "app.alert('This is JavaScript executed from an annotation!');";
        PdfAction jsAction = PdfAction.javaScript(annotationJS, writer);
        annotation.setAction(jsAction);

        writer.addAnnotation(annotation);
        document.add(new Paragraph("Click the annotation above to execute JavaScript."));
    }

    // Method to add a form field with JavaScript
    private static void addFormFieldWithJavaScript(PdfWriter writer, Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20);

        PdfFormField form = PdfFormField.createEmpty(writer);

        // Create a text field
        PdfFormField textField = PdfFormField.createTextField(writer, false, false, 50);
        textField.setWidget(new Rectangle(100, 650, 300, 680), PdfAnnotation.HIGHLIGHT_INVERT);
        textField.setFieldName("TextField");
        textField.setFieldFlags(PdfFormField.FF_REQUIRED);

        // Add JavaScript to the text field
        String textFieldJS = "if (event.value.length > 5) app.alert('Text length exceeds 5 characters!');";
        textField.setAdditionalActions(PdfAnnotation.APPEARANCE_DOWN, PdfAction.javaScript(textFieldJS, writer));

        form.addKid(textField);
        writer.addAnnotation(textField);

        // Add a submit button
        PdfFormField submitButton = PdfFormField.createPushButton(writer);
        submitButton.setWidget(new Rectangle(350, 650, 450, 680), PdfAnnotation.HIGHLIGHT_PUSH);
        submitButton.setFieldName("SubmitButton");
        submitButton.setValueAsString("Submit");

        // Add JavaScript action to the button
        String buttonJS = "app.alert('Button clicked!');";
        submitButton.setAction(PdfAction.javaScript(buttonJS, writer));

        form.addKid(submitButton);
        writer.addAnnotation(submitButton);

        document.add(new Paragraph("Interactive form field added below. Enter text or click the button:"));
    }
}