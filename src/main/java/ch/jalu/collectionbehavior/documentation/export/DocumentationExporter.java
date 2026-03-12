package ch.jalu.collectionbehavior.documentation.export;

import ch.jalu.collectionbehavior.documentation.CollectionDocumentation;
import ch.jalu.collectionbehavior.documentation.ListDocumentation;
import ch.jalu.collectionbehavior.documentation.ListIteratorDocumentation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

public class DocumentationExporter {

    private static final String EXPORT_PATH = "./result";

    private final ListDocumentationExporter listDocumentationExporter =
        new ListDocumentationExporter();
    private final ListIteratorDocumentationExporter listIteratorDocumentationExporter =
        new ListIteratorDocumentationExporter();
    private final ListMethodsDocumentationExporter listMethodsDocumentationExporter =
        new ListMethodsDocumentationExporter();

    public void writeMarkdown(Collection<CollectionDocumentation> documentations, String filename) {
        StringBuilder sb = new StringBuilder();

        for (CollectionDocumentation documentation : documentations) {
            generateMarkdown(sb, documentation);
            sb.append("\n\n");
        }

        writeToFile(sb, filename);
    }

    private static void writeToFile(StringBuilder sb, String filename) {
        Path mdDocument = Paths.get(EXPORT_PATH, filename);
        try {
            Files.createDirectories(mdDocument.getParent());
            Files.writeString(mdDocument, sb);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write to " + filename, e);
        }
    }

    public void writeMethodCallsTable(Collection<CollectionDocumentation> documentations, String filename) {
        List<ListDocumentation> listDocumentations = documentations.stream()
            .filter(doc -> doc instanceof ListDocumentation)
            .map(doc -> (ListDocumentation) doc)
            .toList();
        StringBuilder methodsTable = listMethodsDocumentationExporter.exportMethodsTable(listDocumentations);

        writeToFile(methodsTable, filename);
    }

    private void generateMarkdown(StringBuilder sb, CollectionDocumentation documentation) {
        switch (documentation) {
            case ListDocumentation ld -> listDocumentationExporter.toMarkdown(sb, ld);
            case ListIteratorDocumentation lid -> listIteratorDocumentationExporter.toMarkdown(sb, lid);
            default -> throw new UnsupportedOperationException("Unknown class: " + documentation.getClass());
        }
    }
}
