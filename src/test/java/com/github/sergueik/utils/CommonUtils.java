package com.github.sergueik.utils;

import static org.junit.Assert.assertTrue;

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

/**
 * Common Utilities (unfinished refacoring)
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class CommonUtils {

	private static String osName;
	private static String sql;
	private static Connection conn;

	// Utilities
	public static String getOsName() {
		if (osName == null) {
			osName = System.getProperty("os.name");
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
}
