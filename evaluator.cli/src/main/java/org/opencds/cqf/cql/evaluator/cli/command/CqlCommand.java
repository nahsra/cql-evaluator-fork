package org.opencds.cqf.cql.evaluator.cli.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.evaluator.cli.CqlRunner;
import org.opencds.cqf.cql.evaluator.cql2elm.content.LibraryContentProvider;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "cql", mixinStandardHelpOptions = true)
public class CqlCommand implements Callable<Integer> {
    @Option(names = {"-fv", "--fhir-version"}, required = true)
    public String fhirVersion;

    @ArgGroup(multiplicity = "1..*", exclusive = false)
    List<LibraryParameter> libraries;

    static class LibraryParameter {
        @Option(names = {"-lu", "--library-url"}, required = true)
        public String libraryUrl;

        @Option(names = {"-ln", "--library-name"}, required = true)
        public String libraryName;

        @Option(names = {"-lv", "--library-version"})
        public String libraryVersion;

        @Option(names = {"-t", "--terminology-url"})
        public String terminologyUrl;

        @ArgGroup(multiplicity = "0..1", exclusive = false)
        public ModelParameter model;

        @ArgGroup(multiplicity = "0..*", exclusive = false)
        public List<ParameterParameter> parameters;

        @Option(names = {"-e", "--expression"})
        public String[] expression;

        @ArgGroup(multiplicity = "0..1", exclusive = false)
        public ContextParameter context;

        static class ContextParameter {
            @Option(names = {"-c", "--context"})
            public String contextName;

            @Option(names = {"-cv", "--context-value"})
            public String contextValue;
        }

        static class ModelParameter {
            @Option(names = {"-m", "--model"})
            public String modelName;

            @Option(names = {"-mu", "--model-url"})
            public String modelUrl;
        }

        static class ParameterParameter {
            @Option(names = {"-p", "--parameter"})
            public String parameterName;

            @Option(names = {"-pv", "--parameter-value"})
            public String parameterValue;
        }
    }

    private Map<String, LibraryContentProvider> libraryContentProviderIndex = new HashMap<>();
    private Map<String, TerminologyProvider> terminologyProviderIndex = new HashMap<>();

    @Override
    public Integer call() throws Exception {

        List<CqlRunner.LibraryParameter> librariesIn = new ArrayList<>();
        for (LibraryParameter libraryParameter : libraries) {
            CqlRunner.LibraryParameter libraryPropertyIn = new CqlRunner.LibraryParameter();
            libraryPropertyIn.libraryUrl = libraryParameter.libraryUrl;
            libraryPropertyIn.libraryName = libraryParameter.libraryName;
            libraryPropertyIn.libraryVersion = libraryParameter.libraryVersion;
            libraryPropertyIn.terminologyUrl = libraryParameter.terminologyUrl;
            libraryPropertyIn.model = new CqlRunner.LibraryParameter.ModelParameter();
            libraryPropertyIn.model.modelName = libraryParameter.model.modelName;
            libraryPropertyIn.model.modelUrl = libraryParameter.model.modelUrl;
            libraryPropertyIn.context = new CqlRunner.LibraryParameter.ContextParameter();
            libraryPropertyIn.context.contextName = libraryParameter.context.contextName;
            libraryPropertyIn.context.contextValue = libraryParameter.context.contextValue;
            librariesIn.add(libraryPropertyIn);
        }
        EvaluationResult result = new CqlRunner().runCql(fhirVersion, librariesIn);
        for (Map.Entry<String, Object> libraryEntry : result.expressionResults.entrySet()) {
            System.out.println(libraryEntry.getKey() + "=" + tempConvert(libraryEntry.getValue()));
        }

        System.out.println();

        return 0;
    }

    private String tempConvert(Object value) {
        if (value == null) {
            return "null";
        }

        String result = "";
        if (value instanceof Iterable) {
            result += "[";
            Iterable<?> values = (Iterable<?>) value;
            for (Object o : values) {

                result += (tempConvert(o) + ", ");
            }

            if (result.length() > 1) {
                result = result.substring(0, result.length() - 2);
            }

            result += "]";
        } else if (value instanceof IBaseResource) {
            IBaseResource resource = (IBaseResource) value;
            result = resource.fhirType() + (resource.getIdElement() != null && resource.getIdElement().hasIdPart()
                    ? "(id=" + resource.getIdElement().getIdPart() + ")"
                    : "");
        } else if (value instanceof IBase) {
            result = ((IBase) value).fhirType();
        } else if (value instanceof IBaseDatatype) {
            result = ((IBaseDatatype) value).fhirType();
        } else {
            result = value.toString();
        }

        return result;
    }
}
