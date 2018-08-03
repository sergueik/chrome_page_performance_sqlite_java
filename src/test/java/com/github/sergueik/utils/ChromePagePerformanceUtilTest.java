package com.github.sergueik.utils;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.json.JSONObject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChromePagePerformanceUtilTest {

	private static WebDriver driver;
	private static String osName;
	private static boolean headless = Boolean.parseBoolean(CommonUtils.getPropertyEnv("HEADLESS","true"));
	// private static boolean headless = true;

	private static String baseURL = "https://www.royalcaribbean.com/";
	private static boolean useChromeLogging = true;

	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void beforeClass() throws IOException {
		getOsName();

		System.setProperty("webdriver.chrome.driver",
				osName.toLowerCase().startsWith("windows")
						? new File("c:/java/selenium/chromedriver.exe").getAbsolutePath()
						:  System.getenv("HOME") + "/Downloads/chromedriver");
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		ChromeOptions options = new ChromeOptions();

		Map<String, Object> chromePrefs = new HashMap<>();

		chromePrefs.put("profile.default_content_settings.popups", 0);
		String downloadFilepath = System.getProperty("user.dir")
				+ System.getProperty("file.separator") + "target"
				+ System.getProperty("file.separator");
		chromePrefs.put("download.default_directory", downloadFilepath);
		chromePrefs.put("enableNetwork", "true");
		options.setExperimentalOption("prefs", chromePrefs);

		for (String option : (new String[] { "allow-running-insecure-content",
				"allow-insecure-localhost", "enable-local-file-accesses",
				"disable-notifications",
				/* "start-maximized" , */
				"browser.download.folderList=2",
				"--browser.helperApps.neverAsk.saveToDisk=image/jpg,text/csv,text/xml,application/xml,application/vnd.ms-excel,application/x-excel,application/x-msexcel,application/excel,application/pdf",
				String.format("browser.download.dir=%s", downloadFilepath)
				/* "user-data-dir=/path/to/your/custom/profile"  , */
		})) {
			options.addArguments(option);
		}
		// options for headless
		if (headless) {
			// headless option arguments
			for (String option : (osName.toLowerCase().startsWith("windows"))
					? new String[] { "headless", "disable-gpu", "disable-plugins",
							"window-size=1200x600", "window-position=-9999,0" }
					: new String[] { "headless", "disable-gpu",
							"remote-debugging-port=9222", "window-size=1200x600" }) {
				options.addArguments(option);
			}
			// on Windows need ChromeDriver 2.31 / Chrome 60 to support headless
			// With earlier versions of chromedriver: chrome not reachable...
			// https://developers.google.com/web/updates/2017/04/headless-chrome
			// https://stackoverflow.com/questions/43880619/headless-chrome-and-selenium-on-windows
		}
		//
		if (useChromeLogging) {
			LoggingPreferences loggingPreferences = new LoggingPreferences();
			loggingPreferences.enable(LogType.PERFORMANCE, Level.ALL);
			capabilities.setCapability(CapabilityType.LOGGING_PREFS,
					loggingPreferences);
		}
		capabilities.setBrowserName(DesiredCapabilities.chrome().getBrowserName());
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		driver = new ChromeDriver(capabilities);

		assertThat(driver, notNullValue());
	}

	@Before
	public void beforeMethod() throws IOException {

	}

	@After
	public void afterMethod() {

		System.err.println("After Method:");
		int cnt = 0;
		int maxCnt = 5;
		LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);
		for (LogEntry logEntry : logEntries) {
			if (cnt++ > maxCnt) {
				break;
			}
			// System.err.println("Log entry: " + logEntry.getMessage());
			System.err.println(String.format("Log entry %d: ", cnt));
			Map<String, Object> o = (Map<String, Object>) logEntry.toJson();
			JSONObject logEntryJSONObject = new JSONObject(o.get("message"));
			Iterator<String> logEntryKeys = logEntryJSONObject.keys();
			while (logEntryKeys.hasNext()) {
				String logEntryKey = logEntryKeys.next();
				System.err.println(logEntryKey);
			}
		}

	}

	@AfterClass
	public static void teardown() {
		driver.close();
		driver.quit();
	}

	// @Ignore
	@Test
	public void testnavigateBaseURL() {
		System.err.println("base URL loading test");
		try {
			driver.get(baseURL);
			// Wait for page url to update
			WebDriverWait wait = new WebDriverWait(driver, 20);
			wait.pollingEvery(500, TimeUnit.MILLISECONDS);
			ExpectedCondition<Boolean> urlChange = driver -> driver.getCurrentUrl()
					.matches(String.format("^%s.*", baseURL));
			wait.until(urlChange);
			System.err.println("Current  URL: " + driver.getCurrentUrl());
		} catch (TimeoutException e) {

		}
	}

	// Utilities
	public static String getOsName() {
		if (osName == null) {
			osName = System.getProperty("os.name");
		}
		return osName;
	}

}
