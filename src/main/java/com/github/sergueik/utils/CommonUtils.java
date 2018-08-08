package com.github.sergueik.utils;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * Common Utilities (unfinished refacoring)
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class CommonUtils {

	private static String osName;
	private static String sql;
	private static Connection conn;
	private static boolean isWindow10 = false;
	private static String currentBuildNumber = null;

	public static boolean isWindow10() {
		return isWindow10;
	}

	public static String getCurrentBuildNumber() {
		return currentBuildNumber;
	}

	public static String getOSName() {
		if (osName == null) {
			osName = System.getProperty("os.name").toLowerCase();
			if (osName.startsWith("windows")) {
				osName = "windows";
				isWindow10 = window10Check();
			}
		}
		return osName;
	}

	// http://www.sqlitetutorial.net/sqlite-java/create-table/
	public static void createNewTable() {
		sql = "DROP TABLE IF EXISTS performance";
		try (java.sql.Statement statement = conn.createStatement()) {
			statement.execute(sql);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		sql = "CREATE TABLE IF NOT EXISTS performance (\n"
				+ "	id integer PRIMARY KEY,\n" + "	name text NOT NULL,\n"
				+ "	duration real\n" + ");";
		try (java.sql.Statement statement = conn.createStatement()) {
			statement.execute(sql);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	// origin:
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

	// https://stackoverflow.com/questions/31072543/reliable-way-to-get-windows-version-from-registry
	// CurrentMajorVersionNumber present in registry starting with Windows 10
	public static boolean window10Check() {
		String regPath = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion";
		int majorVersionNumber = 0;
		int minorVersionNumber = 0;
		String currentVersion = null;
		try {
			majorVersionNumber = Advapi32Util.registryGetIntValue(
					WinReg.HKEY_LOCAL_MACHINE, regPath, "CurrentMajorVersionNumber");
			System.err.println("Windows version: " + majorVersionNumber); 
		} catch (Exception e) {
			System.err.println("Exception (ignored)" + e.getMessage());
		}
		try {
			minorVersionNumber = Advapi32Util.registryGetIntValue(
					WinReg.HKEY_LOCAL_MACHINE, regPath, "CurrentMinorVersionNumber");
			System.err.println("Windows minor version: " + minorVersionNumber); 
		} catch (Exception e) {
			System.err.println("Exception (ignored)" + e.getMessage());
		}
		try {
			currentVersion = Advapi32Util.registryGetStringValue(
					WinReg.HKEY_LOCAL_MACHINE, regPath, "CurrentVersion");
			System.err.println("Windows version (legacy): " + currentVersion ); 
		} catch (Exception e) {
			System.err.println("Exception (ignored)" + e.getMessage());
		}
		
		try {
			currentBuildNumber = Advapi32Util.registryGetStringValue(
					WinReg.HKEY_LOCAL_MACHINE, regPath, "CurrentBuildNumber");
			System.err.println("Windows build number: " + currentBuildNumber ); 
		} catch (Exception e) {
			System.err.println("Exception (ignored)" + e.getMessage());
		}
		
		return (majorVersionNumber >= 10);
	}

}
