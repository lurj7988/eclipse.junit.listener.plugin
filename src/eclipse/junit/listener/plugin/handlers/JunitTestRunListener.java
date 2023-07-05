package eclipse.junit.listener.plugin.handlers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.eclipse.jdt.internal.junit.model.TestCaseElement;
import org.eclipse.jdt.internal.junit.model.TestSuiteElement;
import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestCaseElement;
import org.eclipse.jdt.junit.model.ITestElement;
import org.eclipse.jdt.junit.model.ITestRunSession;

import com.alibaba.fastjson.JSON;

public class JunitTestRunListener extends TestRunListener
{

    /**
     * A test run session has finished. The test tree can be accessed through
     * the session element.
     *
     * <p>
     * Important: The implementor of this method must not keep the session
     * element when the method is finished.
     * </p>
     *
     * @param session
     *            the test
     */
    @Override
    public void sessionFinished(ITestRunSession session) {
        SurefireUploadData surefireUploadData = new SurefireUploadData();
        surefireUploadData.setBuildTime(new Date());
        try {
            InetAddress addr = InetAddress.getLocalHost();
            surefireUploadData.setHostName(addr.getHostName());
            surefireUploadData.setHostAddress(addr.getHostAddress());
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        surefireUploadData.setBuildBy(System.getProperty("user.name"));
        surefireUploadData.setBuildOs(String.format("%s (%s; %s)", System.getProperty("os.name"),
                System.getProperty("os.version"), System.getProperty("os.arch")));
        surefireUploadData.setBuiltJdk(
                String.format("%s (%s)", System.getProperty("java.version"), System.getProperty("java.vendor")));
        List<ReportTestSuite> reportTestSuites = new ArrayList();
        for (ITestElement iTestSuiteElement : session.getChildren()) {
            TestSuiteElement testSuiteElement = (TestSuiteElement) iTestSuiteElement;
            ReportTestSuite reportTestSuite = new ReportTestSuite();
            reportTestSuite.setFullClassName(testSuiteElement.getClassName());
            reportTestSuite.setPackageName(
                    testSuiteElement.getClassName().substring(0, testSuiteElement.getClassName().lastIndexOf('.')));
            reportTestSuite.setName(
                    testSuiteElement.getClassName().substring(testSuiteElement.getClassName().lastIndexOf('.') + 1));
            reportTestSuite.setTimeElapsed(Double.valueOf(testSuiteElement.getElapsedTimeInSeconds()).floatValue());
            int numberOfTests = 0;
            int numberOfErrors = 0;
            int numberOfFailures = 0;
            int numberOfSkipped = 0;
            int numberOfFlakes = 0;
            List<ReportTestCase> reportTestCases = new ArrayList();
            for (ITestElement iTestElement : testSuiteElement.getChildren()) {
                if (iTestElement instanceof TestCaseElement) {
                    numberOfTests++;
                    TestCaseElement testCaseElement = (TestCaseElement) iTestElement;
                    ReportTestCase reportTestCase = new ReportTestCase();
                    reportTestCase.setClassName(testCaseElement.getClassName()
                            .substring(testCaseElement.getClassName().lastIndexOf('.') + 1));
                    reportTestCase.setFullClassName(testCaseElement.getClassName());
                    reportTestCase.setName(testCaseElement.getDisplayName());
                    reportTestCase
                            .setFullName(testCaseElement.getClassName() + "." + testCaseElement.getTestMethodName());
                    reportTestCase.setTime(Double.valueOf(testCaseElement.getElapsedTimeInSeconds()).floatValue());
                    if (ITestElement.Result.OK.equals(testCaseElement.getTestResult(false))) {
                        reportTestCase.setSuccessful(true);
                    }
                    if (ITestElement.Result.ERROR.equals(testCaseElement.getTestResult(false))) {
                        numberOfErrors++;
                        reportTestCase.setSuccessful(false);
                        reportTestCase.setFailureDetail(testCaseElement.getFailureTrace().getTrace());
                    }
                    if (ITestElement.Result.FAILURE.equals(testCaseElement.getTestResult(false))) {
                        numberOfFailures++;
                        reportTestCase.setSuccessful(false);
                        String trace = testCaseElement.getFailureTrace().getTrace();
                        if (trace.indexOf("\r\n\t") > 0) {
                            trace = trace.substring(0, trace.indexOf("\r\n\t"));
                        }
                        reportTestCase.setFailureDetail(trace);
                    }
                    if (ITestElement.Result.IGNORED.equals(testCaseElement.getTestResult(false))) {
                        numberOfSkipped++;
                        reportTestCase.setSuccessful(false);
                    }
                    reportTestCases.add(reportTestCase);
                }
                if (iTestElement instanceof TestSuiteElement) {
                    TestSuiteElement testSuiteElement2 = (TestSuiteElement) iTestElement;
                    for (ITestElement iTestElement2 : testSuiteElement2.getChildren()) {
                        if (iTestElement2 instanceof TestCaseElement) {
                            numberOfTests++;
                            TestCaseElement testCaseElement2 = (TestCaseElement) iTestElement2;
                            ReportTestCase reportTestCase = new ReportTestCase();
                            reportTestCase.setClassName(testCaseElement2.getClassName()
                                    .substring(testCaseElement2.getClassName().lastIndexOf('.') + 1));
                            reportTestCase.setFullClassName(testCaseElement2.getClassName());
                            reportTestCase.setName(testCaseElement2.getDisplayName());
                            reportTestCase.setFullName(
                                    testCaseElement2.getClassName() + "." + testCaseElement2.getTestMethodName());
                            reportTestCase
                                    .setTime(Double.valueOf(testCaseElement2.getElapsedTimeInSeconds()).floatValue());
                            if (ITestElement.Result.OK.equals(testCaseElement2.getTestResult(false))) {
                                reportTestCase.setSuccessful(true);
                            }
                            if (ITestElement.Result.ERROR.equals(testCaseElement2.getTestResult(false))) {
                                numberOfErrors++;
                                reportTestCase.setSuccessful(false);
                                reportTestCase.setFailureDetail(testCaseElement2.getFailureTrace().getTrace());
                            }
                            if (ITestElement.Result.FAILURE.equals(testCaseElement2.getTestResult(false))) {
                                numberOfFailures++;
                                reportTestCase.setSuccessful(false);
                                String trace = testCaseElement2.getFailureTrace().getTrace();
                                if (trace.indexOf("\r\n\t") > 0) {
                                    trace = trace.substring(0, trace.indexOf("\r\n\t"));
                                }
                                reportTestCase.setFailureDetail(trace);
                            }
                            if (ITestElement.Result.IGNORED.equals(testCaseElement2.getTestResult(false))) {
                                numberOfSkipped++;
                                reportTestCase.setSuccessful(false);
                            }
                            reportTestCases.add(reportTestCase);
                        }
                    }
                }
            }
            reportTestSuite.setTestCases(reportTestCases);
            reportTestSuite.setNumberOfTests(numberOfTests);
            reportTestSuite.setNumberOfErrors(numberOfErrors);
            reportTestSuite.setNumberOfFailures(numberOfFailures);
            reportTestSuite.setNumberOfSkipped(numberOfSkipped);
            reportTestSuite.setNumberOfFlakes(numberOfFlakes);
            reportTestSuites.add(reportTestSuite);
        }
        surefireUploadData.setReportTestSuites(reportTestSuites);
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost("https://fdoc.epoint.com.cn/SystemInfoDetectTool/rest/junit/report");
            String json = JSON.toJSONString(surefireUploadData);
            System.out.println(json);
            httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
            System.out.println(
                    "surefire reports upload " + httpClient.execute(httpPost, new BasicHttpClientResponseHandler()));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A test case has ended. The result can be accessed from the element.
     * <p>
     * Important: The implementor of this method must not keep a reference to
     * the test case element
     * after {@link #sessionFinished(ITestRunSession)} has finished.
     * </p>
     *
     * @param testCaseElement
     *            the test that has finished running
     */
    @Override
    public void testCaseFinished(ITestCaseElement testCaseElement) {
        // System.out.println("TestClassName:" +
        // testCaseElement.getTestClassName());
        // System.out.println("TestMethodName:" +
        // testCaseElement.getTestMethodName());
        // System.out.println("ElapsedTimeInSeconds:" +
        // testCaseElement.getElapsedTimeInSeconds());
        // System.out.println(
        // "FailureTrace:" + (testCaseElement.getFailureTrace() == null ? "" :
        // testCaseElement.getFailureTrace()));
        // System.out.println("ParentContainer:"
        // + (testCaseElement.getParentContainer() == null ? "" :
        // testCaseElement.getParentContainer()));
        // System.out.println("ProgressState:"
        // + (testCaseElement.getProgressState() == null ? "" :
        // testCaseElement.getProgressState()));
        // System.out.println("TestResult(true):"
        // + (testCaseElement.getTestResult(true) == null ? "" :
        // testCaseElement.getTestResult(true)));
        // System.out.println("TestResult(false):"
        // + (testCaseElement.getTestResult(false) == null ? "" :
        // testCaseElement.getTestResult(false)));
        // System.out.println("TestRunSession:"
        // + (testCaseElement.getTestRunSession() == null ? "" :
        // testCaseElement.getTestRunSession()));
    }

}
