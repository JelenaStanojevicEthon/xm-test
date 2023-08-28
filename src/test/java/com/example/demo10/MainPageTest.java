package com.example.demo10;

import com.codeborne.selenide.*;
import com.sun.jna.WString;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Locale;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static org.junit.jupiter.api.Assertions.*;
import static com.codeborne.selenide.Selenide.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainPageTest {
    public static String browserSize="";

    @BeforeAll
    public static void setUpAll() {
        Configuration.browser = "Firefox";
        //Configuration.browserSize = "1024x768";
        //Configuration.browserSize = "800x600";
        browserSize = "max";
    }
    @AfterAll
    public static void driverClose() {

    }


    @BeforeEach
    public void setUp() {

    }

    @Test
    public void calendarAndVideo() throws InterruptedException {
        open("https://www.xm.com/");
        if (browserSize.equals("max"))
        {
            getWebDriver().manage().window().maximize();
        }
        $(By.className("js-acceptDefaultCookie")).click();
        $(By.className("main_nav_research")).click();
        $(By.partialLinkText("Economic Calendar")).click();
        Thread.sleep(1000);

        switchTo().frame("iFrameResizer0");

        //slider move to right to today
        sliderMove("mat-slider-thumb", false);
        Thread.sleep(2000);
        ImmutablePair<String, String> immutablePair =  getFormatedDate(0);
        assertEquals(immutablePair.left, immutablePair.right);

        //slider move to right to Tomorrow
        sliderMove("mat-slider-thumb", true);
        Thread.sleep(2000);
        immutablePair =  getFormatedDate(1);
        assertEquals(immutablePair.left, immutablePair.right);

        //slider move to next week
        sliderMove("mat-slider", true);
        Thread.sleep(2000);
        immutablePair =  getFormatedDate(2);
        assertEquals(immutablePair.left, immutablePair.right);

        switchTo().defaultContent();
        //educational videos
        $(By.className("main_nav_research")).click();
        $(By.partialLinkText("Educational Videos")).click();
        $(By.xpath("/html/body/div[3]/div/div[6]/div[1]/ul/li[2]/button")).click();
        $(By.partialLinkText("Introduction to the Financial Markets")).click();
        //video
        // Find the video element in frame
        switchTo().frame($(By.xpath("/html/body/div[3]/div/div[6]/div[1]/div/div[1]/iframe")));
        WebElement videoElement = $(By.className("video"));
        // Play the video
        Thread.sleep(1000);
        WebElement playButton = $(By.className("player-big-play-button"));
        playButton.click();
        Thread.sleep(6000);
        // Execute JavaScript to get the video duration
        WebDriver driver= getWebDriver();
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        String durationScript = "return arguments[0].duration;";
        Double duration = (Double) jsExecutor.executeScript(durationScript, driver.findElement(By.tagName("video")));
        // Assert that the video duration is more than 5 seconds
        assertTrue(duration > 5.0);

    }

    private void sliderMove(String buttonClass, boolean toClick){
        Actions actions = new Actions(getWebDriver());
        WebElement slider = $(By.className(buttonClass));
        actions.clickAndHold(slider)
                .moveByOffset(50, 0) // Move to the right by 100 pixels
                .perform();
        if(toClick) {
            slider.click();
        }
    }

    private ImmutablePair<String, String> getFormatedDate(int dateOffset){
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today;
        if(dateOffset == 1) {
            targetDate = today.plusDays(1);
        }
        else if(dateOffset == 2) {
            targetDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        String date =  $(By.className("tc-economic-calendar-item-header-left-title")).getText();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM dd", Locale.ENGLISH);
        // Format the date
        String formattedDate = targetDate.format(formatter);
        return new ImmutablePair<>(date, formattedDate);
    }


    @Test
    public void getAllProjects() throws ParseException {
        RequestSpecification httpRequest = RestAssured.given();
        String url = "https://swapi.dev/api/films";
        Response response = httpRequest.get(url);
        assertEquals(200, response.getStatusCode());
        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        JSONArray moviesArray = jsonObject.getJSONArray("results");
        SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
        Date d0 = sdformat.parse("0000-00-00");
        Date d1 = null;
        String title = "";
        String curentTitle = "";
        int episode_id = 0;
        Object characters0=null;
        Object characters=null;
        for (Object o : moviesArray)
        {
            JSONObject movie = (JSONObject) o;
            characters = movie.get("characters");
            Object created = movie.get("release_date");
            d1 = sdformat.parse(created.toString());
            curentTitle = movie.get("title").toString();
            episode_id = (int) movie.get("episode_id");
            if(d1.compareTo(d0) > 0) {
               d0=d1;
               characters0=characters;
               title = curentTitle;
            }
        }
        System.out.println("release_date: " +d0);
        System.out.println("title: " +title);

        //people
        JSONArray charactersArray = (JSONArray) characters0;
        JSONObject responseBody;
        int heightInt0 = 0;
        int heightInt=0;
        String name = "";
        String name0 ="";
        for (Object o : charactersArray) {
            url = o.toString();
            response = httpRequest.get(url);
            responseBody = new JSONObject(response.getBody().asString());
            String height = responseBody.get("height").toString();
            name = responseBody.get("name").toString();
            heightInt = Integer.parseInt(height);
            if(heightInt>heightInt0)
            {
                heightInt0 = heightInt;
                name0=name;
            }
        }
        System.out.println("max height is: " +heightInt0);
        System.out.println("name is: " +name0);

    }


}
