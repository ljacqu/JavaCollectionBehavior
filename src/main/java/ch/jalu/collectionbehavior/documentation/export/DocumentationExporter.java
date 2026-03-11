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

public class DocumentationExporter {

    private static final String EXPORT_PATH = "./result";

    private final ListDocumentationExporter listDocumentationExporter =
        new ListDocumentationExporter();
    private final ListIteratorDocumentationExporter listIteratorDocumentationExporter =
        new ListIteratorDocumentationExporter();

    public void writeMarkdown(Collection<CollectionDocumentation> documentations, String filename) {
        StringBuilder sb = new StringBuilder();

        for (CollectionDocumentation documentation : documentations) {
            sb.append(generateMarkdown(documentation))
                .append("\n\n");
        }

        Path mdDocument = Paths.get(EXPORT_PATH, filename);
        try {
            Files.createDirectories(mdDocument.getParent());
            Files.writeString(mdDocument, sb);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write to " + filename, e);
        }
    }

    private String generateMarkdown(CollectionDocumentation documentation) {
        return switch (documentation) {
            case ListDocumentation ld -> listDocumentationExporter.toMarkdown(ld);
            case ListIteratorDocumentation lid -> ""; //listIteratorDocumentationExporter.toMarkdown(lid);
            default -> throw new UnsupportedOperationException("Unknown class: " + documentation.getClass());
        };
    }
}
