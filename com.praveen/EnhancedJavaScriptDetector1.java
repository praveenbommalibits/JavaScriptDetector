import com.itextpdf.text.pdf.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

public class EnhancedJavaScriptDetector1 {

    // Pattern to detect common JavaScript in PDF streams
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile(
            "(eval\\(|Function\\(|app\\.alert|document\\.write|window\\.open|XMLHttpRequest|base64decode|unescape|atob|setTimeout|setInterval|ActiveXObject)",
            Pattern.CASE_INSENSITIVE
    );

    public static void main(String[] args) {
        // Simulate receiving a PDF in bytecode format (replace with actual byte array)
        byte[] pdfBytecode = createMaliciousPDFWithJavaScript();

        if (pdfBytecode == null) {
            System.err.println("Failed to create or receive PDF bytecode.");
            return;
        }

        System.out.println("Scanning PDF bytecode for JavaScript...");
        boolean isJavaScriptDetected = scanPDFForJavaScript(pdfBytecode);

        if (isJavaScriptDetected) {
            System.out.println("JavaScript detected in the PDF bytecode. Stopping further processing.");
        } else {
            System.out.println("No JavaScript detected in the PDF bytecode.");
        }
    }

    private static boolean scanPDFForJavaScript(byte[] pdfBytecode) {
        try (InputStream inputStream = new ByteArrayInputStream(pdfBytecode)) {
            PdfReader reader = new PdfReader(inputStream);

            // Scan all objects in the PDF for JavaScript
            for (int i = 0; i < reader.getXrefSize(); i++) {
                PdfObject obj = reader.getPdfObject(i);
                if (obj != null) {
                    String jsContent = extractJavaScript(obj);
                    if (jsContent != null && JAVASCRIPT_PATTERN.matcher(jsContent).find()) {
                        System.out.println("JavaScript detected: " + jsContent);
                        reader.close();
                        return true;
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("Error scanning PDF bytecode: " + e.getMessage());
        }
        return false;
    }

    private static String extractJavaScript(PdfObject obj) {
        if (obj instanceof PdfString) {
            return ((PdfString) obj).toUnicodeString();
        } else if (obj instanceof PdfDictionary) {
            PdfDictionary dict = (PdfDictionary) obj;
            PdfString js = dict.getAsString(PdfName.JS);
            if (js != null) {
                return js.toUnicodeString();
            }
        }
        return null;
    }

    // Simulates the creation of a malicious PDF with embedded JavaScript
    private static byte[] createMaliciousPDFWithJavaScript() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Initialize iText Document and PdfWriter
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            // Open the document for writing
            document.open();

            // Add some content to the PDF
            document.add(new com.itextpdf.text.Paragraph("This is a sample PDF with malicious JavaScript."));

            // Adding Document-Level JavaScript
            writer.addJavaScript("app.alert('Document-level JavaScript executed!');");

            // Adding an annotation with JavaScript action
            PdfAction jsAction = PdfAction.javaScript("app.alert('Annotation-level JavaScript executed!');", writer);
            PdfAnnotation annotation = PdfAnnotation.createText(writer,
                    new com.itextpdf.text.Rectangle(100, 200, 300, 400),
                    "Annotation with JavaScript",
                    "This annotation triggers JavaScript",
                    true,
                    null);
            annotation.setAction(jsAction);
            writer.addAnnotation(annotation);

            // Close the document
            document.close();

            // Return the PDF as a byte array
            return outputStream.toByteArray();
        } catch (Exception e) {
            System.err.println("Error creating malicious PDF: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
