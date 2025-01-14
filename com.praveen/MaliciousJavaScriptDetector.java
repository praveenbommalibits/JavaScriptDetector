import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfString;

import java.io.File;
import java.util.regex.Pattern;

public class MaliciousJavaScriptDetector {

    // Define malicious JavaScript patterns
    private static final Pattern MALICIOUS_PATTERN = Pattern.compile(
            "(eval\\(|app\\.alert|document\\.write|window\\.open|XMLHttpRequest|base64decode)",
            Pattern.CASE_INSENSITIVE
    );

    public static void main(String[] args) {
        String filePath = "/Users/bommali/Downloads/MaliciousJavaScriptProject/JavaScriptDetector/JavaScriptSample.pdf"; // Replace with the path to your PDF file

        try {
            File pdfFile = new File(filePath);
            if (!pdfFile.exists()) {
                System.out.println("PDF file does not exist: " + filePath);
                return;
            }

            System.out.println("Scanning PDF for malicious JavaScript...");
            PdfReader reader = new PdfReader(filePath);

            boolean maliciousDetected = false;

            // Check for document-level JavaScript
            PdfDictionary catalog = reader.getCatalog();
            PdfDictionary names = catalog.getAsDict(PdfName.NAMES);
            if (names != null) {
                PdfDictionary javascript = names.getAsDict(PdfName.JAVASCRIPT);
                if (javascript != null) {
                    PdfArray jsArray = javascript.getAsArray(PdfName.KIDS);
                    if (jsArray != null) {
                        for (int i = 0; i < jsArray.size(); i++) {
                            PdfObject jsObj = jsArray.getDirectObject(i);
                            String jsContent = extractJavaScript(jsObj);
                            if (isMalicious(jsContent)) {
                                System.out.println("Malicious JavaScript detected in document-level actions:");
                                System.out.println(jsContent);
                                maliciousDetected = true;
                            }
                        }
                    }
                }
            }

            // Check for JavaScript in annotations
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                PdfDictionary pageDict = reader.getPageN(i);
                PdfArray annotations = pageDict.getAsArray(PdfName.ANNOTS);
                if (annotations != null) {
                    for (int j = 0; j < annotations.size(); j++) {
                        PdfDictionary annotation = annotations.getAsDict(j);
                        PdfDictionary action = annotation.getAsDict(PdfName.A);
                        if (action != null && PdfName.JAVASCRIPT.equals(action.getAsName(PdfName.S))) {
                            String jsContent = extractJavaScript(action.get(PdfName.JS));
                            if (isMalicious(jsContent)) {
                                System.out.println("Malicious JavaScript detected in annotations on page " + i + ":");
                                System.out.println(jsContent);
                                maliciousDetected = true;
                            }
                        }
                    }
                }
            }

            // Check for JavaScript in form fields
            PdfDictionary acroForm = catalog.getAsDict(PdfName.ACROFORM);
            if (acroForm != null) {
                PdfArray fields = acroForm.getAsArray(PdfName.FIELDS);
                if (fields != null) {
                    for (int i = 0; i < fields.size(); i++) {
                        PdfDictionary field = fields.getAsDict(i);
                        if (field != null) {
                            PdfObject jsObj = field.get(PdfName.AA);
                            String jsContent = extractJavaScript(jsObj);
                            if (isMalicious(jsContent)) {
                                System.out.println("Malicious JavaScript detected in form field:");
                                System.out.println(jsContent);
                                maliciousDetected = true;
                            }
                        }
                    }
                }
            }

            if (!maliciousDetected) {
                System.out.println("No malicious JavaScript detected in the PDF.");
            }

            reader.close();
        } catch (Exception e) {
            System.err.println("Error scanning PDF: " + e.getMessage());
        }
    }

    // Helper method to extract JavaScript content
    private static String extractJavaScript(PdfObject jsObj) {
        if (jsObj instanceof PdfString) {
            return ((PdfString) jsObj).toUnicodeString();
        } else if (jsObj instanceof PdfDictionary) {
            PdfDictionary dict = (PdfDictionary) jsObj;
            PdfString js = dict.getAsString(PdfName.JS);
            if (js != null) {
                return js.toUnicodeString();
            }
        }
        return "";
    }

    // Method to check if JavaScript contains malicious patterns
    private static boolean isMalicious(String jsContent) {
        return jsContent != null && MALICIOUS_PATTERN.matcher(jsContent).find();
    }
}
