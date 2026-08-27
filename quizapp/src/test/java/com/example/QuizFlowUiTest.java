package com.example;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;

/**
 * End-to-end UI test for the Quiz App front end.
 *
 * <p>Walks the whole student journey the way a person would: open the app,
 * choose User, pick the first subject, answer every question, submit, and
 * check the result screen. Originally captured with
 * {@code npx playwright codegen} and then tidied up.
 *
 * <p>Needs both servers running:
 * <ul>
 *   <li>the front end on Tomcat  - http://localhost:8595/Quiz%20App/</li>
 *   <li>the Spring Boot back end - http://localhost:8093 (the front end calls it)</li>
 * </ul>
 *
 * <p>Run it with:  {@code mvn test -Dtest=QuizFlowUiTest}
 * <br>Add {@code -Dheaded=true} to watch it happen in a real browser window.
 *
 * <p>Note: submitting stores a real row in the quiz_result table, so each run
 * adds one attempt to /results.
 */
public class QuizFlowUiTest {

    /**
     * Where the front end is deployed. Jenkins deploys the WAR renamed to
     * QuizApp.war, so there the context path is /QuizApp/; locally it is still
     * the exported name with a space, which has to stay URL-encoded as %20.
     *
     * <p>Override with {@code -Dapp.url=...} or the PLAYWRIGHT_BASE_URL
     * environment variable, which is what the Jenkinsfile sets.
     */
    private static final String APP_URL = resolveAppUrl();

    private static final String DEFAULT_APP_URL = "http://localhost:8595/Quiz%20App/";

    private static String resolveAppUrl() {
        String fromProperty = System.getProperty("app.url");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        String fromEnv = System.getenv("PLAYWRIGHT_BASE_URL");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return DEFAULT_APP_URL;
    }

    /**
     * The first subject button on the User screen. This is an Appzillon-generated
     * id - if the screen is rebuilt it may change, and this is the first thing
     * to re-record.
     */
    private static final String FIRST_SUBJECT_BUTTON = "#QuizAp__UserQuizHome__el_btn_2_0";

    /**
     * One answer per question, in the order they appear. Against the seeded
     * General Knowledge subject this scores 4 out of 5 - the fourth answer is
     * deliberately wrong, which proves the grading actually discriminates
     * instead of just marking everything correct.
     */
    private static final List<String> ANSWERS = List.of("C", "A", "C", "B", "C");

    /** The subject the first button selects; asserted on the result screen. */
    private static final String EXPECTED_SUBJECT = "General Knowledge";

    /**
     * Whether to show a real browser window. Off by default; a Jenkins running
     * as a Windows service has no desktop to draw on and would fail. This
     * controller runs as a logged-in user, so the Jenkinsfile turns it on.
     */
    private static boolean headed() {
        if (Boolean.getBoolean("headed")) {
            return true;
        }
        return "true".equalsIgnoreCase(System.getenv("PLAYWRIGHT_HEADED"));
    }

    /**
     * Milliseconds to pause between actions. Headless runs go full speed;
     * when a window is on screen the steps otherwise flash past too quickly to
     * follow, so slow them down enough to watch.
     */
    private static double slowMo() {
        String configured = System.getProperty("slowmo");
        if (configured != null) {
            return Double.parseDouble(configured);
        }
        return headed() ? 700 : 0;
    }

    @Test
    void userTakesQuizAndSeesResult() {
        // try-with-resources closes Playwright even if an assertion fails.
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(!headed())
                    .setSlowMo(slowMo()));
            try {
                // Records the run to target/videos. Works headless too, so a
                // Jenkins build always leaves something you can watch back.
                BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions()
                        .setRecordVideoDir(Paths.get("target/videos"))
                        .setViewportSize(1280, 720));

                Page page = context.newPage();

                openQuiz(page);
                answerAllQuestions(page);
                String result = submitAndReadResult(page);

                assertResultScreen(result);

                // The video is only written out when the context closes.
                context.close();
                System.out.println("Video saved under quizapp/target/videos");
            } finally {
                browser.close();
            }
        }
    }

    /** Opens the app and starts the quiz for the first subject. */
    private void openQuiz(Page page) {
        page.navigate(APP_URL);

        // The landing screen offers two roles; the student side is "User".
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("User")).click();

        page.locator(FIRST_SUBJECT_BUTTON).click();
    }

    /**
     * Types each answer and advances. "Next" appears between questions but not
     * after the last one, where "Submit Quiz" takes its place - hence the
     * bound check rather than a plain click every time.
     */
    private void answerAllQuestions(Page page) {
        for (int i = 0; i < ANSWERS.size(); i++) {
            answerBox(page).fill(ANSWERS.get(i));

            boolean lastQuestion = (i == ANSWERS.size() - 1);
            if (!lastQuestion) {
                page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Next")).click();
            }
        }
    }

    /** Submits, waits for the results table to render, returns its visible text. */
    private String submitAndReadResult(Page page) {
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Submit Quiz")).click();

        // The results table is drawn by JavaScript after the backend responds,
        // so wait for the heading rather than guessing with a fixed sleep.
        page.getByText("Results").first().waitFor();

        // Evidence of the run, handy when this fails on a machine you can't see.
        page.screenshot(new Page.ScreenshotOptions()
            .setPath(Paths.get("target/quiz-result.png"))
            .setFullPage(true));

        String text = page.locator("body").innerText();
        System.out.println("=== Result screen ===\n" + text);
        return text;
    }

    private void assertResultScreen(String resultText) {
        assertTrue(resultText.contains("Results"),
            "Expected the results table after submitting, but got:\n" + resultText);

        assertTrue(resultText.contains(EXPECTED_SUBJECT),
            "Expected the result row to name '" + EXPECTED_SUBJECT + "', but got:\n" + resultText);
    }

    /** The single answer field on the current question. */
    private com.microsoft.playwright.Locator answerBox(Page page) {
        return page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Answer"));
    }
}
