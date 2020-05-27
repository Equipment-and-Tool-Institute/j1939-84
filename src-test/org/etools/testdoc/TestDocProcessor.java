package org.etools.testdoc;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import org.junit.Test;

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
            String clas;

            String[] dependsOn;
            String description;
            String id;
            String name;
            String verifies;

            public ItemDescriptor(String verifies, String name, String description, String[] dependsOn) {
                id = verifies.isEmpty() ? name : verifies;
                this.verifies = verifies;
                this.name = name;
                this.description = description;
                this.dependsOn = dependsOn;
                clas = name.contains(".") ? "" : "clas";
            }

            @Override
            public int compareTo(ItemDescriptor that) {
                return Comparator.comparing((ItemDescriptor i) -> i.verifies.isEmpty())
                        .thenComparing(i -> i.verifies)
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
        Map<String, String[]> classDeps = new HashMap<>();
        List<ItemDescriptor> list = elements.stream()
                .flatMap(e -> {
                    TestDoc testDoc = e.getAnnotation(TestDoc.class);
                    // TestDoc may not be on every org.junit.Test
                    String tdDescription = testDoc == null ? "" : testDoc.description();
                    TestItem[] tdItems = testDoc == null ? new TestItem[0] : testDoc.items();
                    String[] tdDependOn = testDoc == null ? new String[0] : testDoc.dependsOn();

                    String name;
                    String[] dependsOn;
                    Stream<ItemDescriptor> thisRecord;
                    if (e.getKind() == ElementKind.METHOD) {
                        Element cls = e.getEnclosingElement();
                        String className = cls.getSimpleName().toString();
                        name = className + "." + e.getSimpleName().toString();
                        dependsOn = Stream.concat(Stream.of(tdDependOn),
                                Stream.of(classDeps.getOrDefault(className, new String[0])))
                                .toArray(x -> new String[x]);

                        // add record for class (will be redundant, but removed
                        // in a later distinct
                        TestDoc clsDoc = cls.getAnnotation(TestDoc.class);
                        ItemDescriptor clsDescriptor;
                        if (clsDoc == null) {
                            clsDescriptor = new ItemDescriptor("", className, "", new String[0]);
                        } else {
                            clsDescriptor = new ItemDescriptor("", className, clsDoc.description(), clsDoc.dependsOn());
                        }
                        thisRecord = Stream.of(clsDescriptor,
                                new ItemDescriptor("", name, tdDescription, tdDependOn));
                    } else {
                        // must be a class
                        dependsOn = tdDependOn;
                        name = e.getSimpleName().toString();
                        classDeps.put(name, dependsOn);
                        thisRecord = Stream.of(new ItemDescriptor("", name, tdDescription, dependsOn));
                    }
                    return Stream.concat(
                            thisRecord,
                            Stream.of(tdItems)
                                    .map(v -> new ItemDescriptor(v.value(),
                                            name,
                                            v.description().isEmpty() ? tdDescription : v.description(),
                                            concat(dependsOn, v.dependsOn()))));
                })
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        try (

                Writer out = processingEnv.getFiler()
                        .createResource(StandardLocation.SOURCE_OUTPUT,
                                "",
                                "testdoc.html")
                        .openWriter()) {
            // index by requirement
            out.write("<html>\n");
            out.write("<link rel=\"stylesheet\" href=\"testdoc.css\">\n");
            out.write("<h1>Test Plan</h1>\n");
            out.write("Generated: " + new Date() + "\n");
            out.write("<table class=\"testdoctable\">\n");
            out.write(
                    "<tr><th>Requirement</th><th>Test Name</th><th>Description</th><th>Depends On</th></tr>");
            for (ItemDescriptor i : list) {
                out.write("<tr id=\"" + i.id + "\" class=\"" + i.clas + "\">"
                        + "<td>" + i.verifies + "</td>"
                        + "<td><a href=\"#" + i.name + "\">" + i.name + "</a></td>"
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
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Test.class);
        if (!elements.isEmpty()) {
            generateMapDoc(elements);
            return true;
        }
        return false;
    }
}