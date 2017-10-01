package demo;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class testbug {
	@Test
	public void run() {
		DesiredCapabilities capabilities = DesiredCapabilities.firefox();

		System.setProperty("webdriver.gecko.driver", "C:/Users/andre/.m2/repository/webdriver/geckodriver/win64/0.19.0/geckodriver.exe");
		capabilities.setCapability("marionette", true);
		
		FirefoxProfile profile = new FirefoxProfile();
		
		// Work around for FireFox not closing, fix comes from here: https://github.com/mozilla/geckodriver/issues/517
		profile.setPreference("browser.tabs.remote.autostart", false);
		profile.setPreference("browser.tabs.remote.autostart.1", false);
		profile.setPreference("browser.tabs.remote.autostart.2", false);
		profile.setPreference("browser.tabs.remote.force-enable", false);

		capabilities.setCapability(FirefoxDriver.PROFILE, profile);
		
		WebDriver driver = new FirefoxDriver(capabilities);
		
		for (int i = 0; i < 200; i++) {
			driver.navigate().to("http://google.co.nz");
			driver.findElement(By.cssSelector("input[name=q]")).sendKeys("concordion");
			driver.findElement(By.cssSelector("input[name=btnK]")).click();
			
			WebDriverWait wait = new WebDriverWait(driver, 3);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("foot")));
			
			boolean exception = false;
			
			do {
				try {
					List<WebElement> results = driver.findElements(By.cssSelector("h3[class=r] > a"));
								
					for (WebElement result : results) {
						if (result.getAttribute("href").equals("http://concordion.org/")) {
							result.click();
						}	
					}
					
					exception = false;
							
				} catch (StaleElementReferenceException e) {
					exception = true;
				}
			} while (exception);
		}
	}
}
