package org.opencds.cqf.cql.evaluator.spring;

import static org.testng.Assert.assertNotNull;


import org.opencds.cqf.cql.evaluator.library.LibraryProcessor;
import org.opencds.cqf.cql.evaluator.spring.configuration.TestConfigurationR4;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes = TestConfigurationR4.class)
public class CqlEvaluatorR4Test extends AbstractTestNGSpringContextTests {

    @Test
    public void canInstantiateR4() {
        LibraryProcessor libraryProcessor = this.applicationContext.getBean(LibraryProcessor.class);
        assertNotNull(libraryProcessor);
    }
}