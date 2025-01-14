import com.itextpdf.text.pdf.*;

import java.io.File;
import java.util.regex.Pattern;

public class EnhancedJavaScriptDetector {

    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile(
            "(eval\\(|Function\\(|app\\.alert|document\\.write|window\\.open|XMLHttpRequest|base64decode|unescape|atob|setTimeout|setInterval|ActiveXObject)",
            Pattern.CASE_INSENSITIVE
    );

    public static void main(String[] args) {
        String filePath = "/Users/bommali/Downloads/MaliciousJavaScriptProject/JavaScriptDetector/JavaScriptSample.pdf"; // Replace with your PDF path

        try {
            File pdfFile = new File(filePath);
            if (!pdfFile.exists()) {
                System.out.println("PDF file does not exist: " + filePath);
                return;
            }

            System.out.println("Scanning PDF for JavaScript...");
            PdfReader reader = new PdfReader(filePath);

            boolean isJavaScriptDetected = false;

            // Scan all objects in the PDF
            for (int i = 0; i < reader.getXrefSize(); i++) {
                PdfObject obj = reader.getPdfObject(i);
                if (obj != null) {
                    String jsContent = extractJavaScript(obj);
                    if (jsContent != null && JAVASCRIPT_PATTERN.matcher(jsContent).find()) {
                        System.out.println("JavaScript detected: " + jsContent);
                        isJavaScriptDetected = true;
                    }
                }
            }

            if (isJavaScriptDetected) {
                System.out.println("JavaScript detected in the PDF. Stopping further processing.");
            } else {
                System.out.println("No JavaScript detected in the PDF.");
            }

            reader.close();
        } catch (Exception e) {
            System.err.println("Error scanning PDF: " + e.getMessage());
        }
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
}
