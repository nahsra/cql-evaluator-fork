package org.opencds.cqf.cql.evaluator.measure.r4;

import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.MeasureReport.MeasureReportType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.IParser;

import static org.testng.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;

public class R4MeasureReportAggregatorTest {

    protected R4MeasureReportAggregator aggregator;
    protected FhirContext fhirContext;

    @BeforeClass
    public void setup()
    {
        this.aggregator = new R4MeasureReportAggregator();
        this.fhirContext = FhirContext.forCached(FhirVersionEnum.R4);
    }

    @Test
    public void aggregate_null_returns_null() {
        MeasureReport actual = this.aggregator.aggregate(null);

        assertNull(actual);
    }

    @Test
    public void aggregate_empty_list_returns_null() {
        MeasureReport actual = this.aggregator.aggregate(Collections.emptyList());

        assertNull(actual);
    }

    @Test
    public void aggregate_singleton_list_returns_singleton() {
        MeasureReport expected = new MeasureReport();
        MeasureReport actual = aggregator.aggregate(Collections.singletonList(expected));

        assertEquals(expected, actual);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void aggregate_individual_report_throws_exception() {
        MeasureReport one = new MeasureReport();
        one.setType(MeasureReportType.INDIVIDUAL);
        aggregator.aggregate(Collections.singletonList(one));
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void aggregate_mismatched_measure_throws_exception() {
        MeasureReport one = new MeasureReport();
        one.setMeasure("1");

        MeasureReport two = new MeasureReport();
        one.setMeasure("2");

        aggregator.aggregate(Lists.newArrayList(one, two));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void aggregate_mismatched_period_throws_exception() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("YYYY");
        Period periodOne = new Period();
        periodOne.setStart(format.parse("1999"));
        periodOne.setEnd(format.parse("2000"));

        MeasureReport one = new MeasureReport();
        one.setPeriod(periodOne);

        Period periodTwo = new Period();
        periodTwo.setStart(format.parse("2000"));
        periodTwo.setEnd(format.parse("2001"));

        MeasureReport two = new MeasureReport();
        two.setPeriod(periodTwo);

        aggregator.aggregate(Lists.newArrayList(one, two));
    }
    
    
    @Test 
    public void aggregateReports_combines_reports() {
        IParser parser = fhirContext.newJsonParser();
        MeasureReport left = (MeasureReport)parser.parseResource(R4MeasureReportAggregatorTest.class.getResourceAsStream("AggregateReport1.json"));
        assertNotNull(left);

        MeasureReport right = (MeasureReport)parser.parseResource(R4MeasureReportAggregatorTest.class.getResourceAsStream("AggregateReport2.json"));
        assertNotNull(right);

        MeasureReport expected = (MeasureReport)parser.parseResource(R4MeasureReportAggregatorTest.class.getResourceAsStream("AggregatedReport.json"));
        assertNotNull(expected);

        MeasureReport actual = this.aggregator.aggregate(Arrays.asList(left, right));
        /*
        FhirContext fhirContext = FhirContext.forR4();
        System.out.println("Resource:"+fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(actual));
        */
        assertTrue(actual.equalsDeep(expected));
    }
}
