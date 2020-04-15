package org.etools.testdoc;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
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
        return SourceVersion.RELEASE_11;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(TestDoc.class);
        if (!elements.isEmpty()) {
            try (Writer out = processingEnv.getFiler()
                    .createResource(StandardLocation.SOURCE_OUTPUT,
                            "org.etools.j1939_84",
                            "testdoc.html")
                    .openWriter()) {
                out.write("<html><table>");
                for (Element e : elements) {
                    TestDoc a = e.getAnnotation(TestDoc.class);

                    out.write("<tr><td>" + e.getSimpleName().toString() + "</td><td>"
                            + Arrays.asList(a.verifies()).stream().collect(Collectors.joining("<br/>")) + "</td><td>"
                            + a.description() + "</td></tr>");
                    String str = e.getSimpleName().toString() + Arrays.asList(a.verifies()) + ": " + a.description();
                    processingEnv.getMessager().printMessage(Kind.NOTE,
                            "TestDoc: " + str);
                }
                out.write("</table></html>");
                return true;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }
}