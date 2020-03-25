package com.nayutalienx.moodle.watcher;

import com.ibm.icu.text.Transliterator;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import java.util.regex.Pattern;


import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class Main {

    public static final String CYRILLIC_TO_LATIN = "Cyrillic-Latin";

    public static void main(String[] args) throws MalformedURLException, InterruptedException {

        Transliterator toLatinTrans = Transliterator.getInstance(CYRILLIC_TO_LATIN);
        Map users = new HashMap<String, String>();

        for (String arg : args) {
            String[] logPass = arg.split(Pattern.quote(":"));
            users.put(logPass[0], logPass[1]);
        }

//        FirefoxOptions firefoxOptions = new FirefoxOptions();
//        final WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), firefoxOptions);

        System.setProperty("webdriver.chrome.driver", "C:\\WebDriver\\bin\\chromedriver.exe");
        final WebDriver driver = new ChromeDriver();

        final Logger logger = Logger.getLogger("Logger");

        final String url = "http://russian_moodle.sevsu.ru/login/index.php";
        final WebDriverWait wait = new WebDriverWait(driver, 10);
        final SimpleDateFormat dt = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        RetryExecutor executor = new AsyncRetryExecutor(scheduler).
                retryOn(Exception.class).
                withFixedBackoff(30000)
                .retryInfinitely();

        for (final Object kv : users.entrySet()) {

            executor.getWithRetry(new Callable<Object>() {
                public Object call() throws Exception {

                    logger.info("Authorization.");
                    driver.get(url);

                    driver.findElement(By.id("username")).sendKeys(((Map.Entry<String, String>) kv).getKey());
                    driver.findElement(By.id("password")).sendKeys(((Map.Entry<String, String>) kv).getValue());
                    driver.findElement(By.id("loginbtn")).submit();
                    WebElement firstResult = wait.until(presenceOfElementLocated(By.id("page-my-index")));
                    logger.info(toLatinTrans.transliterate(firstResult.findElement(By.className("headermain")).getText()) + " - online!");
                    //driver.findElement(By.partialLinkText("http://russian_moodle.sevsu.ru/login/logout.php?sesskey=")).click();
                    //driver.manage().deleteAllCookies();
                    driver.findElement(By.xpath("/html/body/div[2]/header/div[1]/div[1]/div/div[2]/div/div[2]/div/ul[1]/li")).click();
                    driver.findElement(By.xpath("/html/body/div[2]/header/div[1]/div[1]/div/div[2]/div/div[2]/div/ul[2]/li[8]")).click();

                    logger.info("Log Out.");
                    throw new Exception();

                }
            });

        }

    }

}
