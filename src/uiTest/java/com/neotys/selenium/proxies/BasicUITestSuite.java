package com.neotys.selenium.proxies;

/**
 * Created by paul on 11/6/17.
 */

import com.google.common.io.CharStreams;
import com.neotys.rest.design.client.DesignAPIClient;
import com.neotys.selenium.proxies.helpers.ModeHelper;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openqa.selenium.By;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(Categories.class)
@Suite.SuiteClasses({
        GoogleTest.class,
        BasicUshahidiDesignTest.class,
        TransactionUshahidiDesignTest.class
})
public class BasicUITestSuite {

    @BeforeClass
    public static void verifyTargetAppIsUp() throws Exception {

        String baseUrl = ModeHelper.getSetting("baseUrl", "http://ushahidi");

        URL url = new URL(baseUrl);
        String body = CharStreams.toString(new InputStreamReader(url.openStream()));

        assertTrue( // is our target app
                String.format("The URL '%s' is unavailable or contains unexpected content.", baseUrl),
                body.contains("ng-controller")
        );
    }

    @BeforeClass
    public static void verifyThatNeoLoadIsRunning() throws Exception {

        try {
            if (ModeHelper.getMode() == ModeHelper.Mode.DESIGN ||
                    ModeHelper.getMode() == ModeHelper.Mode.END_USER_EXPERIENCE) {
                final DesignAPIClient designAPIClient = DesignManager.getDesignApiClient();
                switch (designAPIClient.getStatus()) {
                    case READY:
                        return;
                    case NO_PROJECT:
                        throw new Exception("No NeoLoad project is loaded. Please load an existing project.");
                    case BUSY:
                    case NEOLOAD_INITIALIZING:
                    case TEST_LOADING:
                    case TEST_RUNNING:
                    case TEST_STOPPING:
                        throw new Exception("NeoLoad is busy. Please retry later or halt the current operation.");
                    default:
                        throw new Exception("NeoLoad is in a yet unknown 'not ready' state.");
                }
            }
        } catch(Exception e) {
            System.err.println("Could not verify that NeoLoad is running, imperative when in Design mode.");
            throw e;
        }
    }

    public static boolean elementContains(NLWebDriver driver, By by, String s) {
        return driver.fluent()
                .element(by)
                .getText().toString()
                .contains(s);
    }

    public static boolean includesContainers(NLWebDriver driver, String[] requiredContainerNames) throws IllegalArgumentException {
        if(requiredContainerNames.length < 1)
            throw new IllegalArgumentException("Parameter 'requiredContainerNames' must be a non-zero length array of expected container names.");

        List<String> containerNames = Arrays.asList(driver.getTransactionNames());
        for(String containerName : requiredContainerNames) {
            if(!containerNames.contains(containerName)) {
                System.err.println(String.format("Recording does not contain an expected container '%s'", containerName));
                return false;
            }
        }
        return true;
    }
}

