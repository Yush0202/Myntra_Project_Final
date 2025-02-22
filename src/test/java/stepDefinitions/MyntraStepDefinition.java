package stepDefinitions;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import components.Utilities;
import io.cucumber.java.en.*;
import org.junit.Assert;

import java.util.List;
import java.util.Map;


public class MyntraStepDefinition {
    private final Playwright playwright = Playwright.create();
    private final Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    private final Page page = browser.newPage();
    private final Utilities utilities = new Utilities(playwright, browser, page);

    @Given("User opens the Myntra website")
    public void user_open_the_myntra_website() {
        utilities.openWebsite("https://www.myntra.com/");

        // Asserting visibility of Myntra Logo
        Assert.assertTrue(page.locator("a.myntraweb-sprite.desktop-logo.sprites-headerLogo").isVisible());
    }

    @When("User navigates to {string} category and {string} section")
    public void user_navigate_to_category(String category, String section) {
        utilities.navigateToCategory(category, section);
    }

    @And("User apply {string} brand filter")
    public void user_apply_brand_filter(String brandName) {
        utilities.applyBrandFilter(brandName);
    }

    @Then("User scrapes product data and prints them")
    public void user_scrape_product_data() {
        List<Map<String, String>> products = utilities.scrapeProductData(6);
        List<Map<String, String>> sortedProducts = utilities.sortScrapedProductsByDiscountInDescendingOrder(products);

        // Print product details
        for (Map<String, String> product : sortedProducts) {
            System.out.println("Model: " + product.get("Model"));
            System.out.println("Actual Price: " + product.get("Actual Price"));
            System.out.println("Discounted Price: " + product.get("Discounted Price"));
            System.out.println("Discount: " + product.get("Discount"));
            System.out.println("Link: " + product.get("Link"));
            System.out.println("----------------------");
        }
    }

    @Then("close browser")
    public void closeBrowser() {
        utilities.closeBrowser();
    }

    @And("validate that user is on {string} page")
    public void validateThatUserIsOnPage(String category) {
        // validates that the user is on the correct category abd section
        Assert.assertEquals("Invalid Category", category, utilities.fetchCategoryFromProductListingPage());
    }

    @And("validate that {string} filter is applied")
    public void validateThatFilterIsApplied(String filter) {
        Assert.assertTrue(page.locator("div.filter-summary-filter").innerText().contains(filter));
    }
}
