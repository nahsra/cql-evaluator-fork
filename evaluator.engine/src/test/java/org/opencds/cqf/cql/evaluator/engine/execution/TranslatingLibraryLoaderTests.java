
package org.opencds.cqf.cql.evaluator.engine.execution;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.mockito.Mockito.times;

import static org.opencds.cqf.cql.evaluator.converter.VersionedIdentifierConverter.toElmIdentifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

import org.apache.commons.lang3.NotImplementedException;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.Mockito;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.evaluator.cql2elm.content.fhir.BaseFhirLibraryContentProvider;
import org.opencds.cqf.cql.evaluator.cql2elm.content.LibraryContentType;
import org.opencds.cqf.cql.evaluator.fhir.adapter.r4.AdapterFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class TranslatingLibraryLoaderTests {

    private static FhirContext fhirContext;
    private static IParser parser;
    private static ModelManager modelManger;
    private BaseFhirLibraryContentProvider testFhirLibraryContentProvider;
    private LibraryLoader libraryLoader;

    @BeforeClass
    public static void setup() {
        fhirContext = FhirContext.forR4();
        modelManger = new ModelManager();
        parser = fhirContext.newJsonParser();
    }

    @BeforeMethod
    public void initialize() {
        this.testFhirLibraryContentProvider = Mockito.spy(new BaseFhirLibraryContentProvider(new AdapterFactory()) {
            @Override
            public IBaseResource getLibrary(org.hl7.elm.r1.VersionedIdentifier versionedIdentifier) {
                String name = versionedIdentifier.getId();

                InputStream libraryStream = TranslatingLibraryLoaderTests.class.getResourceAsStream(name + ".json");

                return parser.parseResource(new InputStreamReader(libraryStream));
            }
        });

        this.libraryLoader = new TranslatingLibraryLoader(modelManger,
                Collections.singletonList(testFhirLibraryContentProvider), CqlTranslatorOptions.defaultOptions());
    }

    // Disabled due to engine issue https://github.com/DBCG/cql_engine/issues/436
    // @Test
    public void canUseXml() {
        VersionedIdentifier libraryIdentifier = new VersionedIdentifier().withId("LibraryXml");
        Library library = this.libraryLoader.load(libraryIdentifier);
        assertNotNull(library);
    }

    // TODO: Add detection of mismatched content.
    // @Test
    public void returnsNullIfContentMismatch() {
        throw new NotImplementedException(
                "TranslatingLibraryLoaders needs implementation so that it can detect if the content (CQL or ELM) loaded does not match the library requested");
    }

    @Test
    public void canUseJson() {
        VersionedIdentifier libraryIdentifier = new VersionedIdentifier().withId("LibraryJson");
        Library library = this.libraryLoader.load(libraryIdentifier);
        assertNotNull(library);
    }

    @Test
    public void usesJsonIfBothPresent() {
        VersionedIdentifier libraryIdentifier = new VersionedIdentifier().withId("LibraryBoth");
        Library library = this.libraryLoader.load(libraryIdentifier);
        assertNotNull(library);

        Mockito.verify(this.testFhirLibraryContentProvider, times(1))
                .getLibraryContent(toElmIdentifier(libraryIdentifier), LibraryContentType.JXSON);

        Mockito.verify(this.testFhirLibraryContentProvider, times(0))
                .getLibraryContent(toElmIdentifier(libraryIdentifier), LibraryContentType.XML);
    }

    @Test
    public void doesNotTranslateIfOptionsMatch() {
        VersionedIdentifier libraryIdentifier = new VersionedIdentifier().withId("LibraryDefaultOptions");
        Library library = this.libraryLoader.load(libraryIdentifier);
        assertNotNull(library);

        Mockito.verify(this.testFhirLibraryContentProvider, times(1))
                .getLibraryContent(toElmIdentifier(libraryIdentifier), LibraryContentType.JXSON);

        Mockito.verify(this.testFhirLibraryContentProvider, times(0))
                .getLibraryContent(toElmIdentifier(libraryIdentifier), LibraryContentType.CQL);
    }

    @Test
    public void translateIfOptionsDontMatch() {
        VersionedIdentifier libraryIdentifier = new VersionedIdentifier().withId("LibraryNoOptions");
        Library library = this.libraryLoader.load(libraryIdentifier);
        assertNotNull(library);

        Mockito.verify(this.testFhirLibraryContentProvider, times(1))
                .getLibraryContent(toElmIdentifier(libraryIdentifier), LibraryContentType.JXSON);

        Mockito.verify(this.testFhirLibraryContentProvider, times(1))
                .getLibraryContent(toElmIdentifier(libraryIdentifier), LibraryContentType.CQL);
    }

    @Test()
    public void returnsNullIfNoContent() {
        VersionedIdentifier libraryIdentifier = new VersionedIdentifier().withId("LibraryNoContent");
        Library library = this.libraryLoader.load(libraryIdentifier);
        assertNull(library);
    }

    @Test()
    public void returnsNullIfBadElmAndNoCql() {
        VersionedIdentifier libraryIdentifier = new VersionedIdentifier().withId("LibraryNoContent");
        Library library = this.libraryLoader.load(libraryIdentifier);

        Mockito.verify(this.testFhirLibraryContentProvider, times(1))
                .getLibraryContent(toElmIdentifier(libraryIdentifier), LibraryContentType.JXSON);

        Mockito.verify(this.testFhirLibraryContentProvider, times(1))
                .getLibraryContent(toElmIdentifier(libraryIdentifier), LibraryContentType.CQL);

        assertNull(library);
    }
}