package com.cht.iTest.selenium;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cht.iTest.def.Action;
import com.cht.iTest.def.FindMethod;
import com.cht.iTest.def.Snapshot;
import com.cht.iTest.def.Sync;
import com.cht.iTest.entity.TestStep;
import com.cht.iTest.util.SpringUtils;

public class App {

	private RemoteWebDriver driver = null;
	private List<TestStep> steps = new ArrayList<TestStep>();
	private static String INDEX;

	private App() {

	}

	public App start() {
		try {
			for (TestStep testStep : steps) {
				Object webElement = findElement(driver, testStep);
				triggerAction(webElement, driver, testStep);
				snapshot(driver, testStep);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return this;
	}

	private static void triggerAction(Object element, RemoteWebDriver driver, TestStep testStep) {
		Action action = testStep.getAction();

		if (element instanceof WebElement) {
			WebElement webElement = (WebElement) element;

			switch (action) {
			case input:
				webElement.sendKeys(testStep.getInputValue());
				break;
			case click:
				webElement.click();
				break;
			default:
				break;
			}
		} else if (element instanceof Alert) {
			Alert alert = (Alert) element;

			switch (action) {
			case accept:
				alert.accept();
				break;
			case dismiss:
				alert.dismiss();
				break;
			default:
				break;
			}
		}

	}

	private static Object findElement(RemoteWebDriver driver, TestStep testStep) {
		FindMethod findMethod = testStep.getFindMethod();
		String element = testStep.getElement();
		Sync sync = testStep.getSync();
		By by = null;
		Object webElement = null;

		switch (findMethod) {
		case name:
			by = (By.name(element));
			break;
		case id:
			by = (By.id(element));
			break;
		case xpath:
			by = (By.xpath(element));
			break;
		case cssSelector:
			by = (By.cssSelector(element));
			break;
		case alert:
			webElement = driver.switchTo().alert();
			break;
		default:
			break;
		}

		if (by != null) {
			webElement = Sync.Y == sync ? driver.findElement(by) : new WebDriverWait(driver, 30).until(ExpectedConditions.elementToBeClickable(by));
		}

		return webElement;
	}

	private static void snapshot(RemoteWebDriver driver, TestStep testStep) {
		if (Snapshot.Y == testStep.getSnapshot()) {
			byte[] byteAry = driver.getScreenshotAs(OutputType.BYTES);
			testStep.setSnapshotImg(byteAry);
		}
	}

	public void exit() {
		driver.quit();
	}

	public static App getInstance() {
		return new App().init();
	}

	public App add(TestStep step) {
		steps.add(step);
		return this;
	}

	public App addAll(List<TestStep> steps) {
		this.steps.addAll(steps);
		return this;
	}

	private App init() {
		if (INDEX == null) {
			try {
				ResourceBundle resourceBundle = ResourceBundle.getBundle("init");
				INDEX = String.format("http://%s/insurance/gs/sp/spLogin", resourceBundle.getString("host"));
				System.setProperty("webdriver.ie.driver", SpringUtils.getResource("classpath:IEDriverServer.exe").getURL().getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		driver = new InternetExplorerDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		driver.get(INDEX);
		return this;
	}

}