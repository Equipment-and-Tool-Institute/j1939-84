package org.etools.testdoc;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;

public class TestDocProcessor extends AbstractProcessor {
    public TestDocProcessor() {
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(TestDoc.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        // Sort by class, then by method name
        Comparator<Element> comparator = Comparator.comparing(
                (Element e) -> (e.getKind() == ElementKind.CLASS)
                        ? e.getSimpleName().toString()
                        // otherwise must be a method, so get the enclosing class name
                        : e.getEnclosingElement().getSimpleName().toString())
                // if the class names are equal, then sort by method name
                .thenComparing(e -> e.getSimpleName().toString());

        List<? extends Element> elements = roundEnv.getElementsAnnotatedWith(TestDoc.class).stream()
                .sorted(comparator).collect(Collectors.toList());
        if (!elements.isEmpty()) {
            try (Writer out = processingEnv.getFiler()
                    .createResource(StandardLocation.SOURCE_OUTPUT,
                            "",
                            "testdoc.html")
                    .openWriter()) {
                out.write("<html><table>");
                for (Element e : elements) {
                    TestDoc a = e.getAnnotation(TestDoc.class);
                    switch (e.getKind()) {
                        case CLASS:
                            out.write("<tr><th>" + e.getSimpleName().toString() + "</th><th>"
                                    + Arrays.asList(a.verifies()).stream().collect(Collectors.joining("<br/>"))
                                    + "</th><th>"
                                    + a.description() + "</th></tr>");
                            break;
                        case METHOD:
                            out.write(
                                    "<tr><td>" + e.getSimpleName().toString() + "</td><td>"
                                            + Arrays.asList(a.verifies()).stream().collect(Collectors.joining("<br/>"))
                                            + "</td><td>"
                                            + a.description() + "</td></tr>");
                            break;
                        default:
                            out.write(
                                    "<tr><td>" + e.getKind() + " " + e.getSimpleName().toString() + "</td><td>"
                                            + Arrays.asList(a.verifies()).stream().collect(Collectors.joining("<br/>"))
                                            + "</td><td>"
                                            + a.description() + "</td></tr>");
                            break;

                    }

                    // This outputs notes from a compile.
                    // String str = e.getSimpleName().toString() + Arrays.asList(a.verifies()) + ":
                    // " + a.description();
                    // processingEnv.getMessager().printMessage(Kind.NOTE, "TestDoc: " + str);
                }
                out.write("</table></html>");
                return true;
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }
}