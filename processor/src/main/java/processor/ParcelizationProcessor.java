package processor;

import lib.SafeParcelable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by tom on 24.08.16.
 */
@SupportedAnnotationTypes({"lib.SafeParcelable"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ParcelizationProcessor extends AbstractProcessor {


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getRootElements()) {

            switch (element.asType().getKind()) {
                case DECLARED:

                    processSafeParcelizableType(element, processingEnv);
//                    break;
                default:
                    ;
            }
        }

        return true;
    }

    private void processSafeParcelizableType(Element element, ProcessingEnvironment processingEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing " + element, element);

        List<? extends Element> allMembers = processingEnv.getElementUtils().getAllMembers((TypeElement) element);


        List<SafeParcelableField> safeParcelableFields = new ArrayList<>();

        for (Element member : allMembers) {
            if (member.getKind() != ElementKind.FIELD) {
                continue;
            }

            SafeParcelable safeParcelable = member.getAnnotation(SafeParcelable.class);
            if (safeParcelable == null) {
                continue;
            }

            safeParcelableFields.add(new SafeParcelableField(member.getSimpleName().toString(), safeParcelable));
        }

        if (safeParcelableFields.isEmpty()) {
            return;
        }

        System.out.println("Generating writeToParcelMethod for " + element);
        System.out.println(safeParcelableFields);

        try {
            String genSrcTarget = processingEnv.getOptions().get("genSrcTarget");

            FileObject in_file = processingEnv.getFiler().getResource(
                    StandardLocation.SOURCE_PATH
                    , "",
                    element.asType().toString().replace(".", "/") + ".java");

            CharSequence javaSource = in_file.getCharContent(false);

            String newJavaSource = generateWriteToParcelMethod(safeParcelableFields, javaSource);

//            JavaFileObject out_file = processingEnv.getFiler().createSourceFile(
//                    element.asType().toString(), element);
//            Writer w = out_file.openWriter();
            Writer w = new FileWriter(new File(genSrcTarget,element.asType().toString().replace(".", "/") + ".java"));
            w.append(newJavaSource);
            w.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateWriteToParcelMethod(List<SafeParcelableField> safeParcelableFields, CharSequence javaSource) {
        System.out.println("Java Source: " + javaSource);

        String writeToParcelMethodSourceCode = generateWriteToParcelMethod(safeParcelableFields);

        return javaSource.toString().replaceAll("//#writeToParcel", writeToParcelMethodSourceCode);
    }

    private String generateWriteToParcelMethod(List<SafeParcelableField> safeParcelableFields) {

        StringBuilder sb = new StringBuilder("public void writeToParcel(){\n");
        for (SafeParcelableField field : safeParcelableFields) {
            sb.append(String.format("System.out.println(\"field: %s idx: %s\");\n", field.fieldName, field.safeParcelable.value()));
        }
        sb.append("}\n");

        return sb.toString();
    }

    static class SafeParcelableField {

        final String fieldName;
        final SafeParcelable safeParcelable;

        public SafeParcelableField(String fieldName, SafeParcelable safeParcelable) {
            this.fieldName = fieldName;
            this.safeParcelable = safeParcelable;
        }

        @Override
        public String toString() {
            return "SafeParcelableField{" +
                    "fieldName='" + fieldName + '\'' +
                    ", safeParcelable=" + safeParcelable +
                    '}';
        }
    }
}
