package org.etools.testdoc;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;

public class TestDocProcessor extends AbstractProcessor {
    private static <T> T[] concat(T[] a, T[] b) {
        T[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public TestDocProcessor() {
    }

    private boolean generateMapDoc(Set<? extends Element> elements) {
        class ItemDescriptor implements Comparable<ItemDescriptor> {
            String[] dependsOn;

            String description;
            String name;
            String verifies;

            public ItemDescriptor(String verifies, String name, String description, String[] dependsOn) {
                this.verifies = verifies;
                this.name = name;
                this.description = description;
                this.dependsOn = dependsOn;
            }

            @Override
            public int compareTo(ItemDescriptor that) {
                return Comparator.comparing((ItemDescriptor i) -> i.verifies)
                        .thenComparing(i -> i.name).compare(this, that);
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof ItemDescriptor) {
                    ItemDescriptor that = (ItemDescriptor) o;
                    return name.equals(that.name);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return name.hashCode();
            }
        }
        List<ItemDescriptor> list = elements.stream()
                .flatMap(e -> {
                    TestDoc testDoc = e.getAnnotation(TestDoc.class);
                    String method = e.getKind() == ElementKind.METHOD ? e.getEnclosingElement().getSimpleName() + "."
                            + e.getSimpleName().toString() : e.getSimpleName().toString();
                    return Stream.concat(
                            Stream.of(new ItemDescriptor(method, method, testDoc.description(), testDoc.dependsOn())),
                            Stream.concat(
                                    Stream.of(testDoc.verifies())
                                            .map(v -> new ItemDescriptor(v,
                                                    method,
                                                    testDoc.description(),
                                                    testDoc.dependsOn())),
                                    Stream.of(testDoc.items())
                                            .map(v -> new ItemDescriptor(v.value(),
                                                    method,
                                                    v.description(),
                                                    concat(testDoc.dependsOn(), v.dependsOn())))));
                })
                .sorted()
                .collect(Collectors.toList());

        try (

                Writer out = processingEnv.getFiler()
                        .createResource(StandardLocation.SOURCE_OUTPUT,
                                "",
                                "testMap.html")
                        .openWriter()) {
            // index by requirement
            out.write("<html>\n");
            out.write("<link rel=\"stylesheet\" href=\"testdoc.css\">\n");
            out.write("test 1\n");
            out.write("<table class=\"testdoctable\">\n");
            out.write(
                    "<tr><th>Requirement</th><th>Test Name</th><th>Description</th><th>Depends On</th></tr>");
            for (ItemDescriptor i : list) {
                out.write("<tr id=\"" + i.verifies + "\">"
                        + "<td>" + i.verifies + "</td>"
                        + "<td>" + i.name + "</td>"
                        + "<td>" + i.description + "</td>"
                        + "<td>" + Stream.of(i.dependsOn)
                                .map(d -> "<a href=\"#" + d + "\">" + d + "</a>")
                                .collect(Collectors.joining("<br/>"))
                        + "</td>"
                        + "</tr>\n");
            }
            out.write("</table>");

            // index by test

            out.write("</html>\n");
            return true;
        } catch (

        Throwable e1) {
            e1.printStackTrace();
        }
        return false;
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

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(TestDoc.class);
        if (!elements.isEmpty()) {
            // generateTestDoc(elements);
            generateMapDoc(elements);
            return true;
        }
        return false;
    }
}