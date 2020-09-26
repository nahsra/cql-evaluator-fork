package org.opencds.cqf.cql.evaluator.library.common;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Parameters;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverter;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverterFactory;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.evaluator.builder.Constants;
import org.opencds.cqf.cql.evaluator.builder.CqlEvaluatorBuilder;
import org.opencds.cqf.cql.evaluator.builder.DataProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.EndpointConverter;
import org.opencds.cqf.cql.evaluator.builder.LibraryLoaderFactory;
import org.opencds.cqf.cql.evaluator.builder.ModelResolverFactory;
import org.opencds.cqf.cql.evaluator.builder.RetrieveProviderConfig;
import org.opencds.cqf.cql.evaluator.builder.RetrieveProviderConfigurer;
import org.opencds.cqf.cql.evaluator.builder.TerminologyProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.data.FhirModelResolverFactory;
import org.opencds.cqf.cql.evaluator.builder.data.TypedRetrieveProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.library.TypedLibrarySourceProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.terminology.TypedTerminologyProviderFactory;
import org.opencds.cqf.cql.evaluator.cql2elm.BundleLibrarySourceProvider;
import org.opencds.cqf.cql.evaluator.engine.retrieve.BundleRetrieveProvider;
import org.opencds.cqf.cql.evaluator.engine.terminology.BundleTerminologyProvider;
import org.opencds.cqf.cql.evaluator.fhir.adapter.AdapterFactory;
import org.opencds.cqf.cql.evaluator.library.CqlFhirParametersConverter;
import org.opencds.cqf.cql.evaluator.library.OperationParametersParser;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;

public class LibraryProcessorTests {

    LibraryProcessor libraryProcessor = null;
    FhirContext fhirContext = null;

    @BeforeClass
    @SuppressWarnings("serial")
    public void setup() {
        fhirContext = FhirContext.forR4();

        AdapterFactory adapterFactory = new org.opencds.cqf.cql.evaluator.fhir.adapter.r4.AdapterFactory();

        Set<TypedLibrarySourceProviderFactory> librarySourceProviderFactories = new HashSet<TypedLibrarySourceProviderFactory>() {
            {
                add(new TypedLibrarySourceProviderFactory() {
                    @Override
                    public String getType() {
                        return Constants.HL7_FHIR_FILES;
                    }

                    @Override
                    public LibrarySourceProvider create(String url, List<String> headers) {
                        return new BundleLibrarySourceProvider(fhirContext,
                                (IBaseBundle) fhirContext.newJsonParser()
                                        .parseResource(LibraryProcessorTests.class.getResourceAsStream(url)),
                                adapterFactory);
                    }
                });
            }
        };

        Set<ModelResolverFactory> modelResolverFactories = new HashSet<ModelResolverFactory>() {
            {
                add(new FhirModelResolverFactory());
            }
        };

        LibraryLoaderFactory libraryLoaderFactory = new org.opencds.cqf.cql.evaluator.builder.library.LibraryLoaderFactory(fhirContext, adapterFactory, librarySourceProviderFactories);
        Set<TypedRetrieveProviderFactory> retrieveProviderFactories = new HashSet<TypedRetrieveProviderFactory>() {
            {
                add(new TypedRetrieveProviderFactory() {
                    @Override
                    public String getType() {
                        return Constants.HL7_FHIR_FILES;
                    }

                    @Override
                    public RetrieveProvider create(String url, List<String> headers) {

                        return new BundleRetrieveProvider(fhirContext, (IBaseBundle) fhirContext.newJsonParser()
                                .parseResource(LibraryProcessorTests.class.getResourceAsStream(url)));
                    }
                });
            }
        };

        DataProviderFactory dataProviderFactory = new org.opencds.cqf.cql.evaluator.builder.data.DataProviderFactory(
                fhirContext, modelResolverFactories, retrieveProviderFactories);


        Set<TypedTerminologyProviderFactory> typedTerminologyProviderFactories = new HashSet<TypedTerminologyProviderFactory>() {
            {
                add(new TypedTerminologyProviderFactory() {
                    @Override
                    public String getType() {
                        return Constants.HL7_FHIR_FILES;
                    }

                    @Override
                    public TerminologyProvider create(String url, List<String> headers) {
                        return new BundleTerminologyProvider(fhirContext, (IBaseBundle) fhirContext.newJsonParser()
                                .parseResource(LibraryProcessorTests.class.getResourceAsStream(url)));
                    }
                });
            }
        };

        TerminologyProviderFactory terminologyProviderFactory = new org.opencds.cqf.cql.evaluator.builder.terminology.TerminologyProviderFactory(fhirContext, typedTerminologyProviderFactories);

        RetrieveProviderConfigurer retrieveProviderConfigurer = new org.opencds.cqf.cql.evaluator.builder.data.RetrieveProviderConfigurer(new RetrieveProviderConfig());


        EndpointConverter endpointConverter = new org.opencds.cqf.cql.evaluator.builder.common.EndpointConverter(
                adapterFactory);

        CqlEvaluatorBuilder cqlEvaluatorBuilder = new CqlEvaluatorBuilder(retrieveProviderConfigurer);

        FhirTypeConverter fhirTypeConverter = new FhirTypeConverterFactory().create(fhirContext.getVersion().getVersion());

        OperationParametersParser operationParametersParser = new OperationParametersParser(adapterFactory, fhirTypeConverter);

        CqlFhirParametersConverter cqlFhirParametersConverter = new CqlFhirParametersConverter(fhirContext, adapterFactory, fhirTypeConverter);

        libraryProcessor = new LibraryProcessor(fhirContext, cqlFhirParametersConverter, operationParametersParser, libraryLoaderFactory,
                dataProviderFactory, terminologyProviderFactory, endpointConverter, cqlEvaluatorBuilder);
    }


    @Test
    public void TestEXM125() {
        Parameters expected = new Parameters();
        expected.addParameter().setName("Numerator").setValue(new BooleanType(true));

        Endpoint endpoint = new Endpoint().setAddress("r4/EXM125-8.0.000-bundle.json")
                .setConnectionType(new Coding().setCode(Constants.HL7_FHIR_FILES));

        Set<String> expressions = new HashSet<String>();
        expressions.add("Numerator");

        Parameters actual = (Parameters)libraryProcessor.evaluate(
                new VersionedIdentifier().withId("EXM125").withVersion("8.0.000"), "Patient", "numer-EXM125", null,
                null, null, endpoint, endpoint, endpoint, null, null, expressions);
        
    
        assertTrue(expected.equalsDeep(actual));
    }

    @Test
    public void TestRuleFiltersReportable() {
        Parameters expected = new Parameters();
        expected.addParameter().setName("IsReportable").setValue(new BooleanType(true));

        Endpoint endpoint = new Endpoint().setAddress("r4/RuleFilters-1.0.0-bundle.json")
        .setConnectionType(new Coding().setCode(Constants.HL7_FHIR_FILES));

        Endpoint dataEndpoint = new Endpoint().setAddress("r4/tests-Reportable-bundle.json")
            .setConnectionType(new Coding().setCode(Constants.HL7_FHIR_FILES));

        Set<String> expressions = new HashSet<String>();
        expressions.add("IsReportable");

        Parameters actual = (Parameters)libraryProcessor.evaluate(
                new VersionedIdentifier().withId("RuleFilters").withVersion("1.0.0"), "Patient", "Reportable", null,
                null, null, endpoint, endpoint, dataEndpoint, null, null, expressions);
        
        assertTrue(expected.equalsDeep(actual));
    }

    @Test
    public void TestRuleFiltersNotReportable() {
        Parameters expected = new Parameters();
        expected.addParameter().setName("IsReportable").setValue(new BooleanType(false));

        Endpoint endpoint = new Endpoint().setAddress("r4/RuleFilters-1.0.0-bundle.json")
        .setConnectionType(new Coding().setCode(Constants.HL7_FHIR_FILES));

        Endpoint dataEndpoint = new Endpoint().setAddress("r4/tests-NotReportable-bundle.json")
            .setConnectionType(new Coding().setCode(Constants.HL7_FHIR_FILES));

        Set<String> expressions = new HashSet<String>();
        expressions.add("IsReportable");

        Parameters actual = (Parameters)libraryProcessor.evaluate(
                new VersionedIdentifier().withId("RuleFilters").withVersion("1.0.0"), "Patient", "NotReportable", null,
                null, null, endpoint, endpoint, dataEndpoint, null, null, expressions);
        
        assertTrue(expected.equalsDeep(actual));
    }
}