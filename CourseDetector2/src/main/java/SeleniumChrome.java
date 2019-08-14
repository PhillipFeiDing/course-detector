import com.sun.tools.hat.internal.model.Root;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class SeleniumChrome {

    public static final String MY_USERNAME = "Sorry I won't tell you.";
    public static final String MY_PASSWORD = "Of course I remember to delete it.";
    public static final int DELAY_MIN = 2000;
    public static final int DELAY_MAX = 2500;
    public static final String ROOT_URL = "https://oscar.gatech.edu/pls/bprod/bwckgens.p_proc_term_date";

    private static WebDriver webDriver;

    /**
     * start chrome driver and go to ROOT_URL
     * @throws InterruptedException when sleep() is interrupted
     */
    public static void start() throws InterruptedException {
        System.setProperty("webdriver.chrome.driver",
                "src/main/resources/driver/chromedriver");

        webDriver = new ChromeDriver();
        webDriver.manage().window().maximize();
        webDriver.get("https://buzzport.gatech.edu/cp/home/displaylogin");
        WebElement loginBtn = webDriver.findElement(By.id("login_btn"));
        delay();
        loginBtn.click();

        WebElement gtAccount = webDriver.findElement(By.id("username"));
        gtAccount.sendKeys(MY_USERNAME);
        WebElement password = webDriver.findElement(By.id("password"));
        password.sendKeys(MY_PASSWORD);
        WebElement submit = webDriver.findElement(By.className("btn-submit"));
        delay();
        submit.click();

        String targetURL = "https://buzzport.gatech.edu/render.userLayoutRootNode.uP?uP_root=root";
        while (!webDriver.getCurrentUrl().equals(targetURL)) {
            Thread.sleep(1000);
        }

        delay();
        webDriver.get("https://buzzport.gatech.edu/render.UserLayoutRootNode.uP?uP_tparam=utf&utf=%2Fcp%2Fip%2Flogin%3Fsys%3Dsct%26url%3Dhttps%3A%2F%2Foscar.gatech.edu/pls/bprod%2Fztgkauth.zp_authorize_from_login");
        delay();
        webDriver.get("https://oscar.gatech.edu/pls/bprod/twbkwbis.P_GenMenu?name=bmenu.P_StuMainMnu");
        delay();
        webDriver.get("https://oscar.gatech.edu/pls/bprod/twbkwbis.P_GenMenu?name=bmenu.P_RegMnu");
        delay();
        webDriver.get("https://oscar.gatech.edu/pls/bprod/bwskfcls.p_sel_crse_search");

        Select dropDown = new Select(webDriver.findElement(By.tagName("select")));
        delay();
        dropDown.selectByValue("201908");

        List<WebElement> elements = webDriver.findElements(By.tagName("input"));
        for (WebElement element : elements) {
            if (element.getAttribute("value").equals("Submit")) {
                delay();
                element.click();
                break;
            }
        }
    }

    /**
     *
     * @param path the directory under which files are saved
     * @param courseTitles the title of all courses looking for
     * @throws IOException when exceptions are thrown operating files
     * @throws InterruptedException when sleep() is interrupted
     * @throws IllegalStateException when methods are called on wrong order
     */
    public static void pullAllPages(String path, String... courseTitles)
            throws IOException, InterruptedException, IllegalStateException {

        if (webDriver == null
        || !webDriver.getCurrentUrl().equals(ROOT_URL)) {
            throw new IllegalStateException("Web driver does not exist or"
            + " currently is on an unrecognizable page.");
        }

        CourseTitle converter = new CourseTitle();

        courseTitles = shuffle(courseTitles);

        for (String title : courseTitles) {
            int idx = 0;
            while (idx < title.length() && title.charAt(idx) >= 'A'
                    && title.charAt(idx) <= 'Z') {
                idx++;
            }
            String subj = title.substring(0, idx);
            String num = title.substring(idx);

            Select windowSelect = new Select(webDriver.findElement(By.tagName("select")));
            delay();
            try {
                windowSelect.selectByVisibleText(converter.get(subj));
            } catch (NullPointerException e) {
                throw new NullPointerException("Course + \"" + title
                        + "\" does not exist or is not offered at this time. "
                        + e.getMessage());
            }

            List<WebElement> elements = webDriver.findElements(By.tagName("input"));
            for (WebElement element : elements) {
                if (element.getAttribute("value").equals("Course Search")) {
                    delay();
                    element.click();
                    break;
                }
            }

            boolean clicked = false;
            elements = webDriver.findElements(By.tagName("td"));
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i).getAttribute(
                        "innerHTML").trim().equals(num)) {
                    delay();
                    elements.get(i + 2).findElement(By.name("SUB_BTN")).click();
                    clicked = true;
                    break;
                }
            }
            if (!clicked) {
                throw new NullPointerException("Course + \"" + title
                        + "\" does not exist or is not offered at this time. ");
            }

            String pageSource = webDriver.getPageSource();

            List<String> lines = Arrays.asList(pageSource.split("\\n"));
            Path file = Paths.get(path + "/" + title + ".html");
            Files.write(file, lines, Charset.forName("UTF-8"));

            while (!webDriver.getCurrentUrl().equals(ROOT_URL)) {
                delay();
                webDriver.navigate().back();
            }
        }
    }

    /**
     * close the web driver gracefully
     * @throws IllegalStateException when methods are called ing wrong order
     */
    public static void close() throws IllegalStateException {
        if (webDriver == null) {
            throw new IllegalStateException("Web driver does not exist.");
        }
        webDriver.close();
    }

    /**
     * create delay between operations
     * @throws InterruptedException when sleep is interrupted
     */
    private static void delay() throws InterruptedException {
        Thread.sleep((int) (Math.random()
                * (DELAY_MAX - DELAY_MIN) + DELAY_MIN));
    }

    /**
     * create randomness in visiting pages about courses
     * @param courseTitles Course Titles
     * @return the randomly shuffled course titles
     */
    private static String[] shuffle(String[] courseTitles) {
        String[] ret = Arrays.copyOf(courseTitles, courseTitles.length);
        for (int i = 0; i < ret.length - 1; i++) {
            int swapWith = (int) (i + Math.random() * (ret.length - i));
            String temp = ret[i];
            ret[i] = ret[swapWith];
            ret[swapWith] = temp;
        }
        return ret;
    }

    public static void main(String[] args) {
        String[] courseTitles = {"MATH2551", "CS1332", "CS2050"};
        try {
            SeleniumChrome.pullAllPages("data", courseTitles);
        } catch (IOException | InterruptedException e) {
            System.out.println("blows!!!!");
        }
    }
}
