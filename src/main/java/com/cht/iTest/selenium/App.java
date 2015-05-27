package com.cht.iTest.selenium;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cht.iTest.def.Action;
import com.cht.iTest.def.FindMethod;
import com.cht.iTest.def.Skip;
import com.cht.iTest.def.Snapshot;
import com.cht.iTest.def.Status;
import com.cht.iTest.def.Sync;
import com.cht.iTest.entity.TestStep;
import com.cht.iTest.util.Cache;
import com.cht.iTest.util.SpringUtils;

/**
 * 
 * Selenium代理
 * 
 * @author wen
 *
 */
public class App {

	public static final String WEBDRIVER_IE_DRIVER = "webdriver.ie.driver";
	public static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";
	public static final String INDEX = "index";
	public static final String IMPLICITLY_WAIT = "implicitlyWait";
	public static final String IE = "Internet Explorer";
	public static final String CHROME = "Chromer";
	public static final String FIREFOX = "Firefox";
	public static final String DEFAULT_WAIT_SEC = "default wait sec";
	public static final String SNANSHOT_PATH = "snapshot path";
	public static final String RETRY_TIMES = "retry times";

	private RemoteWebDriver driver = null;
	private List<TestStep> steps = new ArrayList<TestStep>();
	private Iterator<TestStep> iterator;
	private Map<String, String> executeContext = new HashMap<String, String>();
	private TestStep testStep;
	private Boolean isStop = Boolean.FALSE;
	private int process = 0;
	private String[] currentGetVar = null;

	public String[] getCurrentGetVar() {
		return currentGetVar;
	}

	public void setCurrentGetVar(String[] currentGetVar) {
		this.currentGetVar = currentGetVar;
	}

	public int getProcess() {
		return process;
	}

	public void setProcess(int process) {
		this.process = process;
	}

	public Boolean getIsStop() {
		return isStop;
	}

	private App() {

	}

	public void stop() {
		isStop = true;
	}

	public App createIterator() {
		isStop = false;
		iterator = steps.iterator();
		return this;
	}

	public boolean hasNext() {
		return iterator.hasNext() && !isStop;
	}

	public TestStep doIteratorNext() {
		return iterator.next();
	}

	public void doCurrentStep() throws Exception {
		if (Skip.Y == testStep.getSkip()) {
			return;
		}

		currentGetVar = null;
		Thread.sleep(500);
		Object webElement = findElement();
		triggerAction(webElement);
		snapshot();
		testStep.setExeStatus(Status.Done);
		BigDecimal total = new BigDecimal(this.getSteps().size());
		BigDecimal exec = new BigDecimal((steps.indexOf(testStep) + 1) * 100L);
		process = (exec.divide(total, 2, RoundingMode.HALF_UP)).intValue();
	}

	public App start() {
		setTestStep(null);

		for (TestStep current : steps) {
			current.setErrorMsg(null);
			current.setExeStatus(Status.Ready);
		}

		int times = Integer.parseInt(Cache.getSysCateVal(App.RETRY_TIMES, "2"));

		for (TestStep current : steps) {
			testStep = current;

			for (int i = 0; i <= times; i++) {
				try {
					doCurrentStep();
					break;
				} catch (Exception ex) {
					if (i == times) {
						ex.printStackTrace();
						testStep.setErrorMsg(ex.getMessage());
						testStep.setExeStatus(Status.Fail);
					}
				}
			}

			if (testStep.getErrorMsg() != null) {
				break;
			}

			setTestStep(null);
		}

		return this;
	}

	public App continueFromFail(int index) {
		int times = Cache.getSysCateIntVal(App.RETRY_TIMES, "2").intValue();

		for (int i = index; i < steps.size(); i++) {
			testStep = steps.get(i);

			for (int j = 0; j <= times; j++) {
				try {
					testStep.setErrorMsg(null);
					testStep.setExeStatus(Status.Ready);
					doCurrentStep();
					break;
				} catch (Exception ex) {
					if (j == times) {
						ex.printStackTrace();
						testStep.setErrorMsg(ex.getMessage());
						testStep.setExeStatus(Status.Fail);
					}
				}
			}

			if (testStep.getErrorMsg() != null) {
				break;
			}

			setTestStep(null);
		}

		return this;
	}

	private void triggerAction(Object element) throws InterruptedException {
		Action action = testStep.getAction();

		if (action == Action.wait) {
			String val = testStep.getInputValue();
			Long waitSec = StringUtils.isNumeric(val) ? Long.parseLong(val) : Cache.getSysCateLongVal(App.DEFAULT_WAIT_SEC, "10");
			Thread.sleep(1000L * waitSec);
		} else if (action == Action.replace) {
			String key = getVarsFromContent(testStep.getElement()).iterator().next();
			currentGetVar = new String[] { key, testStep.getInputValue() };
			executeContext.put(key, testStep.getInputValue());
		} else if (element == null && action == Action.switchpop) {
			handleMultipleWindows(testStep.getInputValue());
		} else if (action == Action.to_page) {
			driver.get(testStep.getInputValue());
		} else if (action == Action.get_title) {
			String data = testStep.getInputValue().replaceAll("\\s+", "");
			String tempVal = driver.getTitle().replaceAll("\\s+", "");
			String mykey = getVarsFromContent(data).iterator().next();
			StringBuilder sb = new StringBuilder(mykey);
			sb.insert(sb.indexOf("$"), "\\").insert(sb.indexOf("{"), "\\").insert(sb.indexOf("}"), "\\").toString();
			String replace = data.replaceAll(sb.toString(), "---");
			String[] replaces = replace.split("---");

			for (String re : replaces) {
				tempVal = tempVal.replace(re, "");
			}

			currentGetVar = new String[] { mykey, tempVal };
			executeContext.put(mykey, tempVal);
		} else if (element instanceof WebElement) {
			WebElement webElement = (WebElement) element;
			String data = testStep.getInputValue();

			if (StringUtils.isNotBlank(data) && action != Action.get && action != Action.get_title && action != Action.replace) {
				Set<String> vars = getVarsFromContent(testStep.getInputValue());

				for (String var : vars) {
					data = data.replace(var, executeContext.get(var));
				}
			}

			switch (action) {
			case input:
				webElement.clear();
				webElement.sendKeys(data);
				break;
			case click:
				webElement.click();
				break;
			case select:
				new Select(webElement).selectByVisibleText(data);
				break;
			case iframe:
				driver = (RemoteWebDriver) driver.switchTo().frame(webElement);
				break;
			case get:
				String key = getVarsFromContent(data).iterator().next();
				String val = "";

				if (webElement.getTagName().equals("input")) {
					val = webElement.getAttribute("value");
				} else {
					val = webElement.getText();
				}

				String other = StringUtils.replace(data, key, "");
				val = StringUtils.replace(val, other, "");
				currentGetVar = new String[] { key, val };
				executeContext.put(key, val);
				break;
			case focus:
				if ("input".equals(webElement.getTagName())) {
					webElement.sendKeys("");
				} else {
					new Actions(driver).moveToElement(webElement).build().perform();
				}

				break;
			case switchpop:
				webElement.click();
				handleMultipleWindows(data);
				break;
			case dialog:
				webElement.click();
				driver.switchTo().activeElement();
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

	private void handleMultipleWindows(String windowTitle) {
		Set<String> windows = driver.getWindowHandles();

		for (String window : windows) {
			try {
				driver.switchTo().window(window);
			} catch (Exception ex) {

			}

			if (driver.getTitle().trim().startsWith(windowTitle.trim())) {
				return;
			}
		}
	}

	public static Set<String> getVarsFromContent(String content) {
		Set<String> vars = new LinkedHashSet<String>();

		if (StringUtils.isBlank(content)) {
			return vars;
		}

		Matcher matcher = Pattern.compile("\\$\\{\\w+\\}").matcher(content);

		while (matcher.find()) {
			vars.add(matcher.group());
		}

		return vars;
	}

	private Object findElement() {
		FindMethod findMethod = testStep.getFindMethod();
		String element = testStep.getElement();
		Sync sync = testStep.getSync();
		By by = null;
		Object webElement = null;

		if (findMethod == null || findMethod == FindMethod.none) {
			return null;
		}

		if (StringUtils.isNotBlank(element)) {
			Set<String> vars = getVarsFromContent(element);

			for (String var : vars) {
				element = element.replace(var, executeContext.get(var));
			}
		}

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
		case partialLinkText:
			by = By.partialLinkText(element);
			break;
		case alert:
			webElement = driver.switchTo().alert();
			break;
		default:
			break;
		}

		if (by != null && findMethod != FindMethod.alert) {
			webElement = Sync.Y == sync ? driver.findElement(by) : new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(by));
		}

		return webElement;
	}

	private void snapshot() {
		if (Snapshot.Y == testStep.getSnapshot()) {
			byte[] byteAry = driver.getScreenshotAs(OutputType.BYTES);
			String dir = Cache.getSysCateVal(App.SNANSHOT_PATH);

			try {
				Set<String> vars = getVarsFromContent(testStep.getInputValue());
				String name = testStep.getInputValue();

				for (String var : vars) {
					name = name.replace(var, executeContext.get(var));
				}

				if (StringUtils.isBlank(name)) {
					name = testStep.getTestCase().getName() + "-" + testStep.getName();
				}

				FileUtils.writeByteArrayToFile(new File(dir, name + ".png"), byteAry);
			} catch (Exception e) {
				e.printStackTrace();
			}

			testStep.setSnapshotImg(byteAry);
		}
	}

	public void exit() {
		try {
			Set<String> windows = driver.getWindowHandles();

			for (String window : windows) {
				try {
					driver.switchTo().window(window).close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				driver.quit();

			} catch (Exception ee) {
				try {
					Runtime.getRuntime().exec("TaskKill /F /IM IEDriverServer.exe");
				} catch (IOException e) {
					e.printStackTrace();
				}
				ee.printStackTrace();
			}
		}
	}

	public static App getInstance() {
		return new App().init(IE, null);
	}

	public static App getInstance(String driverType, String size) {
		return new App().init(driverType, size);
	}

	public App add(TestStep step) {
		steps.add(step);
		return this;
	}

	public App addAll(List<TestStep> steps) {
		this.steps.addAll(steps);
		return this;
	}

	private static void sysProertyCheck() {
		if (System.getProperty(WEBDRIVER_IE_DRIVER) == null) {
			try {
				String path = Cache.getSysCateVal(WEBDRIVER_IE_DRIVER);
				System.setProperty(WEBDRIVER_IE_DRIVER, SpringUtils.getResource(path).getURL().getPath());
				path = Cache.getSysCateVal(WEBDRIVER_CHROME_DRIVER);
				System.setProperty(WEBDRIVER_CHROME_DRIVER, SpringUtils.getResource(path).getURL().getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private App init(String driverType, String size) {
		sysProertyCheck();

		if (CHROME.equals(driverType)) {
			driver = new ChromeDriver();
		} else if (FIREFOX.equals(driverType)) {
			driver = new FirefoxDriver();
		} else {
			driver = new InternetExplorerDriver();
		}

		if ("FullScreen".equals(size) || size == null) {
			driver.manage().window().maximize();
		} else {
			String[] sizes = size.split("X");
			driver.manage().window().setSize(new Dimension(Integer.parseInt(sizes[0]), Integer.parseInt(sizes[1])));
		}

		driver.manage().timeouts().implicitlyWait(Cache.getSysCateLongVal(IMPLICITLY_WAIT, "30"), TimeUnit.SECONDS);
		return this;
	}

	public App toIndex() {
		String index = (String) Cache.getSysCateVal(INDEX);
		driver.navigate().to(index);
		return this;
	}

	public App setSteps(List<TestStep> steps) {
		this.steps = steps;
		return this;
	}

	public Map<String, String> getExecuteContext() {
		return executeContext;
	}

	public App setExecuteContext(Map<String, String> executeContext) {
		this.executeContext = executeContext;
		return this;
	}

	public List<TestStep> getSteps() {
		return steps;
	}

	public TestStep getTestStep() {
		return testStep;
	}

	public void setTestStep(TestStep testStep) {
		this.testStep = testStep;
	}

}