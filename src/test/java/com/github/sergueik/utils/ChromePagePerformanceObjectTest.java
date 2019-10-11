package com.github.sergueik.utils;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.github.sergueik.utils.ChromePagePerformanceObject;
import com.github.sergueik.utils.ChromePagePerformanceUtil;

public class ChromePagePerformanceObjectTest {

	private static WebDriver driver;
	private static Connection conn;
	private static String browser = "chrome";
	private static final String osName = CommonUtils.getOSName();
	private static final boolean headless = Boolean.parseBoolean(CommonUtils.getPropertyEnv("HEADLESS", "false"));
	private static final boolean useLocalDb = Boolean.parseBoolean(CommonUtils.getPropertyEnv("LOCAL", "false"));

	// private static String baseURL = "https://www.royalcaribbean.com/";
	// private static By elementSelector = By.id("find-a-cruise");

	private static String baseURL = "https://www.expedia.com/";
	private static By elementSelector = By.cssSelector("button#tab-flight-tab-hp");
	/*
	 * private static String baseURL = "https://www.priceline.com/"; private
	 * static By elementSelector = By.cssSelector(
	 * "#global-header-nav-section > ul > li.global-header-nav-product-item.global-header-nav-product-item-hotels > a"
	 * );
	 */
	private static String sql;

	@SuppressWarnings("deprecation")

	@BeforeClass
	public static void beforeClass() throws IOException {

		System.setProperty("webdriver.chrome.driver",
				osName.contains("windows") ? new File("c:/java/selenium/chromedriver.exe").getAbsolutePath()
						: System.getenv("HOME") + "/Downloads/chromedriver");
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		ChromeOptions options = new ChromeOptions();

		Map<String, Object> chromePrefs = new HashMap<>();

		chromePrefs.put("profile.default_content_settings.popups", 0);
		String downloadFilepath = System.getProperty("user.dir") + System.getProperty("file.separator") + "target"
				+ System.getProperty("file.separator");
		chromePrefs.put("download.default_directory", downloadFilepath);
		chromePrefs.put("enableNetwork", "true");
		options.setExperimentalOption("prefs", chromePrefs);

		for (String option : (new String[] { "allow-running-insecure-content", "allow-insecure-localhost",
				"enable-local-file-accesses", "disable-notifications",
				/* "start-maximized" , */
				"browser.download.folderList=2",
				"--browser.helperApps.neverAsk.saveToDisk=image/jpg,text/csv,text/xml,application/xml,application/vnd.ms-excel,application/x-excel,application/x-msexcel,application/excel,application/pdf",
				String.format("browser.download.dir=%s", downloadFilepath)
				/* "user-data-dir=/path/to/your/custom/profile" , */
		})) {
			options.addArguments(option);
		}
		// options for headless
		if (headless) {
			// headless option arguments
			for (String option : (osName.toLowerCase().startsWith("windows"))
					? new String[] { "headless", "disable-gpu", "disable-plugins", "window-size=1200x600",
							"window-position=-9999,0" }
					: new String[] { "headless", "disable-gpu", "remote-debugging-port=9222",
							"window-size=1200x600" }) {
				options.addArguments(option);
			}
			// on Windows need ChromeDriver 2.31 / Chrome 60 to support headless
			// With earlier versions of chromedriver: chrome not reachable...
			// https://developers.google.com/web/updates/2017/04/headless-chrome
			// https://stackoverflow.com/questions/43880619/headless-chrome-and-selenium-on-windows
		}
		//
		capabilities.setBrowserName(DesiredCapabilities.chrome().getBrowserName());
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		driver = new ChromeDriver(capabilities);

		try {
			// origin:
			// https://www.tutorialspoint.com/sqlite/sqlite_java.htm
			Class.forName("org.sqlite.JDBC");

			// NOTE: SQLite driver on its own will not create folders to
			// construct
			// path to the file,
			// default is current project directory

			String dbURL = (useLocalDb) ? "jdbc:sqlite:performance.db"
					: CommonUtils.resolveEnvVars(String.format("jdbc:sqlite:${%s}/Desktop/sqlite_database_name.db",
							osName.equals("windows") ? "USERPROFILE" : "HOME"));

			conn = DriverManager.getConnection(dbURL);
			if (conn != null) {
				// System.out.println("Connected to the database");
				DatabaseMetaData databaseMetadata = conn.getMetaData();
				System.out.println("Database url: " + dbURL);
				System.out.println("Driver name: " + databaseMetadata.getDriverName());
				System.out.println("Driver version: " + databaseMetadata.getDriverVersion());
				System.out.println("Product name: " + databaseMetadata.getDatabaseProductName());
				System.out.println("Product version: " + databaseMetadata.getDatabaseProductVersion());

				createNewTable();
				createNewIndex();
				// insertData("name", 1.0);
				// conn.close();
			}
		} catch (ClassNotFoundException | SQLException ex) {
			ex.printStackTrace();
		} finally {
		}
		assertThat(driver, notNullValue());
	}

	@Before
	public void beforeMethod() throws IOException {

		driver.get(baseURL);
		// Wait for page url to update
		WebDriverWait wait = new WebDriverWait(driver, 20);
		wait.pollingEvery(500, TimeUnit.MILLISECONDS);
		ExpectedCondition<Boolean> urlChange = driver -> driver.getCurrentUrl()
				.matches(String.format("^%s.*", baseURL));
		wait.until(urlChange);
		System.err.println("Current  URL: " + driver.getCurrentUrl());
		/*
		 * // Take screenshot // under headless Chrome, some vendor pages behave
		 * differently e.g. // www.royalcaribbean.com redirects to the //
		 * "Oops... Looks like RoyalCaribbean.com is on vacation" page File
		 * screenShot = ((TakesScreenshot) driver)
		 * .getScreenshotAs(OutputType.FILE);
		 * 
		 * // To get the width of image. BufferedImage readImage =
		 * ImageIO.read(screenShot); int width = readImage.getWidth();
		 * FileUtils.copyFile(screenShot, new
		 * File(System.getProperty("user.dir") +
		 * System.getProperty("file.separator") + "test.png"));
		 */
	}

	@After
	public void afterMethod() {
	}

	@AfterClass
	public static void teardown() {
		driver.close();
		driver.quit();
		try {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			System.err.println("SQL exception (ignored): " + e.getMessage());
		}
	}

	// @Ignore
	// NOTE: only works with HEADLESS = false
	@Test
	public void testSetTimer() {
		double test = new ChromePagePerformanceObject(driver, baseURL, elementSelector).getLoadTime();
		System.err.println(test);
	}

	@Ignore
	@Test
	public void testRecordSplitter() {
		String payload = "{stuff} , {redirectCount=0, encodedBodySize=64518, unloadEventEnd=0, responseEnd=4247.699999992619, domainLookupEnd=2852.7999999932945, unloadEventStart=0, domContentLoadedEventStart=4630.699999994249, type=navigate, decodedBodySize=215670, duration=5709.000000002561, redirectStart=0, connectEnd=3203.5000000032596, toJSON={}, requestStart=3205.499999996391, initiatorType=beacon}, {some other stuff}";
		String splitter = "(?<=\\}) *, *(?=\\{)";
		Pattern pattern = Pattern.compile(splitter);
		Matcher matcher = pattern.matcher(payload);
		if (matcher.find()) {
			new ArrayList<String>(Arrays.asList(payload.split(splitter))).stream().forEach(System.err::println);
		}
	}

	// NOTE: this test is failing
	@SuppressWarnings("unchecked")
	@Ignore
	@Test
	public void testRecordNativeSplitter() {
		String payloadsArray = "[{stuff} , {redirectCount=0, encodedBodySize=64518, unloadEventEnd=0, responseEnd=4247.699999992619, domainLookupEnd=2852.7999999932945, unloadEventStart=0, domContentLoadedEventStart=4630.699999994249, type=navigate, decodedBodySize=215670, duration=5709.000000002561, redirectStart=0, connectEnd=3203.5000000032596, toJSON={}, requestStart=3205.499999996391, initiatorType=beacon}, {some other stuff}]";
		String splitter = "(?<=\\}) *, *(?=\\{)";

		List<Object> objectList = new ArrayList<Object>();
		try {
			objectList = (List<Object>) ((Object) payloadsArray);
			System.err.println("First object: " + objectList.get(0));
		} catch (ClassCastException e) {
			System.err.println("Exception (ignored) " + e.toString());
			// Exception (ignored) java.lang.ClassCastException:
			// java.lang.String
			// cannot be cast to java.util.List
		} catch (Exception e) {
			System.err.println("Exception (rethrown) " + e.toString());
			throw e;
		}
		if (objectList.size() > 0) {
			System.err.println("First object: " + objectList.get(0));
			String payload = objectList.get(0).toString();
			Pattern pattern = Pattern.compile(splitter);
			Matcher matcher = pattern.matcher(payload);
			if (matcher.find()) {
				new ArrayList<String>(Arrays.asList(payload.split(splitter))).stream().forEach(System.err::println);

			}
		}
	}

	@Ignore
	@Test
	public void testChromePagePerformanceObjectParse() {
		ChromePagePerformanceObject chromePagePerformanceObject = new ChromePagePerformanceObject(driver, null);
		String payload = "[{redirectCount=0, encodedBodySize=64518, unloadEventEnd=0, responseEnd=4247.699999992619, domainLookupEnd=2852.7999999932945, unloadEventStart=0, domContentLoadedEventStart=4630.699999994249, type=navigate, decodedBodySize=215670, duration=5709.000000002561, redirectStart=0, connectEnd=3203.5000000032596, toJSON={}, requestStart=3205.499999996391, initiatorType=beacon}]";
		// get rid of array.
		payload = payload.substring(1, payload.length() - 1);
		Map<String, Double> data = chromePagePerformanceObject.CreateDateMap(payload);
		data.entrySet().stream().forEach(System.err::println);
	}

	// @Ignore
	@Test
	public void testEdgePagePerformanceCollection() {
		if (CommonUtils.isWindow10()) {
			ChromePagePerformanceUtil chromePagePerformanceUtil = ChromePagePerformanceUtil.getInstance();
			ChromePagePerformanceUtil.setBrowser("edge");
			double loadTime = chromePagePerformanceUtil.getLoadTime(baseURL);
			System.out.println("Page Load Time: " + loadTime);
			Map<String, Double> pageElementTimers = chromePagePerformanceUtil.getPageElementTimers();
			if (pageElementTimers != null) {
				Set<String> names = pageElementTimers.keySet();
				for (String name : names) {
					System.out.println(name + " " + pageElementTimers.get(name));
				}
			}
		}

	}

	// @Ignore
	// NOTE: only works with HEADLESS = false
	@Test
	public void testUtil() {
		ChromePagePerformanceUtil chromePagePerformanceUtil = ChromePagePerformanceUtil.getInstance();
		ChromePagePerformanceUtil.setBrowser(browser);
		double loadTime = chromePagePerformanceUtil.getLoadTime(driver, baseURL, elementSelector);
		System.out.println("Page Load Time: " + loadTime);
		Map<String, Double> pageElementTimers = chromePagePerformanceUtil.getPageElementTimers();
		if (conn != null) {
			Set<String> names = pageElementTimers.keySet();
			for (String name : names) {
				insertData(name, pageElementTimers.get(name));
			}
		}
	}

	// https://www.sqlitetutorial.net/sqlite-index/
	public static void createNewIndex() {
		sql = "CREATE /* UNIQUE */ INDEX idx_name ON performance(name);";
		try (java.sql.Statement statement = conn.createStatement()) {
			statement.execute(sql);
			System.err.println("Executed: " + sql);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	// http://www.sqlitetutorial.net/sqlite-java/create-table/
	public static void createNewTable() {
		sql = "DROP TABLE IF EXISTS performance";
		try (java.sql.Statement statement = conn.createStatement()) {
			statement.execute(sql);
			System.err.println("Executed: " + sql);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		sql = "CREATE TABLE IF NOT EXISTS performance (\n" + "	id integer PRIMARY KEY,\n" + "	name text NOT NULL,\n"
				+ "	duration real\n" + ");";
		try (java.sql.Statement statement = conn.createStatement()) {
			statement.execute(sql);
			System.err.println("Executed: " + sql);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	// http://www.sqlitetutorial.net/sqlite-java/insert/
	public static void insertData(String name, double duration) {
		sql = "INSERT INTO performance(name,duration) VALUES(?,?)";
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, name);
			statement.setDouble(2, duration);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	// https://github.com/TsvetomirSlavov/wdci/blob/master/code/src/main/java/com/seleniumsimplified/webdriver/manager/EnvironmentPropertyReader.java
	public static String getPropertyEnv(String name, String defaultValue) {
		String value = System.getProperty(name);
		if (value == null) {
			value = System.getenv(name);
			if (value == null) {
				value = defaultValue;
			}
		}
		return value;
	}

}
