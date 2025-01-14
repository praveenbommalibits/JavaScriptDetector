import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class GenericJavaScriptDetector {

    // Define a generic pattern to identify JavaScript-like contentø
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile(
            "(eval\\(|Function\\(|app\\.alert|document\\.write|window\\.open|XMLHttpRequest|base64decode|unescape|atob|setTimeout|setInterval|ActiveXObject)",
            Pattern.CASE_INSENSITIVE
    );

    public static void main(String[] args) {
        String filePath = "/Users/bommali/Downloads/MaliciousJavaScriptProject/JavaScriptDetector/JavaScriptSample.pdf"; // Replace with the path to your PDF

        try {
            File pdfFile = new File(filePath);
            if (!pdfFile.exists()) {
                System.out.println("PDF file does not exist: " + filePath);
                return;
            }

            System.out.println("Scanning PDF for JavaScript...");
            PdfReader reader = new PdfReader(filePath);

            boolean isJavaScriptDetected = false;

            // Scan document-level JavaScript
            if (scanDocumentLevelJavaScript(reader)) {
                System.out.println("JavaScript detected at the document level.");
                isJavaScriptDetected = true;
            }

            // Scan annotations
            if (scanAnnotations(reader)) {
                System.out.println("JavaScript detected in annotations.");
                isJavaScriptDetected = true;
            }

            // Scan form fields
            if (scanFormFields(reader)) {
                System.out.println("JavaScript detected in form fields.");
                isJavaScriptDetected = true;
            }

            // Scan embedded files
            if (scanEmbeddedFiles(reader)) {
                System.out.println("JavaScript detected in embedded files.");
                isJavaScriptDetected = true;
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

    private static boolean scanDocumentLevelJavaScript(PdfReader reader) {
        try {
            PdfDictionary catalog = reader.getCatalog();
            PdfDictionary names = catalog.getAsDict(PdfName.NAMES);
            if (names != null) {
                PdfDictionary javascript = names.getAsDict(PdfName.JAVASCRIPT);
                if (javascript != null) {
                    return processJavaScriptObject(javascript);
                }
            }
        } catch (Exception e) {
            System.err.println("Error scanning document-level JavaScript: " + e.getMessage());
        }
        return false;
    }

    private static boolean scanAnnotations(PdfReader reader) {
        try {
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                PdfDictionary pageDict = reader.getPageN(i);
                PdfArray annotations = pageDict.getAsArray(PdfName.ANNOTS);
                if (annotations != null) {
                    for (PdfObject annotationObj : annotations) {
                        PdfDictionary annotation = (PdfDictionary) PdfReader.getPdfObject(annotationObj);
                        PdfDictionary action = annotation.getAsDict(PdfName.A);
                        if (action != null && PdfName.JAVASCRIPT.equals(action.getAsName(PdfName.S))) {
                            if (processJavaScriptObject(action)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error scanning annotations: " + e.getMessage());
        }
        return false;
    }

    private static boolean scanFormFields(PdfReader reader) {
        try {
            PdfDictionary catalog = reader.getCatalog();
            PdfDictionary acroForm = catalog.getAsDict(PdfName.ACROFORM);
            if (acroForm != null) {
                PdfArray fields = acroForm.getAsArray(PdfName.FIELDS);
                if (fields != null) {
                    for (PdfObject fieldObj : fields) {
                        PdfDictionary field = (PdfDictionary) PdfReader.getPdfObject(fieldObj);
                        if (field != null) {
                            PdfObject additionalActions = field.get(PdfName.AA);
                            if (processJavaScriptObject(additionalActions)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error scanning form fields: " + e.getMessage());
        }
        return false;
    }

    private static boolean scanEmbeddedFiles(PdfReader reader) {
        try {
            PdfDictionary catalog = reader.getCatalog();
            PdfDictionary names = catalog.getAsDict(PdfName.NAMES);
            if (names != null) {
                PdfDictionary embeddedFiles = names.getAsDict(PdfName.EMBEDDEDFILES);
                if (embeddedFiles != null) {
                    PdfArray kids = embeddedFiles.getAsArray(PdfName.KIDS);
                    if (kids != null) {
                        for (PdfObject kid : kids) {
                            PdfDictionary embeddedFile = (PdfDictionary) PdfReader.getPdfObject(kid);
                            if (processJavaScriptObject(embeddedFile)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error scanning embedded files: " + e.getMessage());
        }
        return false;
    }

    private static boolean processJavaScriptObject(PdfObject jsObject) {
        if (jsObject == null) {
            return false;
        }

        String jsContent = extractJavaScript(jsObject);
        if (jsContent != null && JAVASCRIPT_PATTERN.matcher(jsContent).find()) {
            System.out.println("JavaScript content detected: " + jsContent);
            return true;
        }
        return false;
    }

    private static String extractJavaScript(PdfObject jsObject) {
        if (jsObject instanceof PdfString) {
            return ((PdfString) jsObject).toUnicodeString();
        } else if (jsObject instanceof PdfDictionary) {
            PdfString js = ((PdfDictionary) jsObject).getAsString(PdfName.JS);
            if (js != null) {
                return js.toUnicodeString();
            }
        }
        return null;
    }
}
