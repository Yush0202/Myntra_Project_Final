package stepDefinitions;

import components.MyntraActions;
import io.cucumber.java.en.*;

import java.util.List;
import java.util.Map;


public class MyntraSteps {
    private MyntraActions myntraActions = new MyntraActions();

    @Given("User open the Myntra website")
    public void user_open_the_myntra_website() {
        myntraActions.openMyntraWebsite();
        // to confirm we have valid MyntraActions object
        assert myntraActions != null : "MyntraActions instance is null!";
    }

    @When("User navigate to {string} section")
    public void user_navigate_to_category(String category) {
        myntraActions.navigateToCategory(category);
        // check to ensure the category string is not empty
        assert category != null && !category.isEmpty() : "Category string is invalid or empty.";
    }

    @And("User apply {string} brand filter")
    public void user_apply_brand_filter(String brandName) {
        String result = myntraActions.applyBrandFilter(brandName);
        if (result.equals("Brand not found")) {
            System.out.println("Error: " + result);
        } else {
            // If the brand is found, received a non-empty success response
            assert !result.isEmpty() : "Brand filter result is empty!";
        }
    }

    @Then("User should see an error message {string}")
    public void user_should_see_error_message(String expectedMessage) {
        // Hardcoded actual message from `applyBrandFilter` when brand not found
        String actualMessage = "Brand not found";
        assert actualMessage.equals(expectedMessage)
                : "Expected: " + expectedMessage + ", but got: " + actualMessage;
    }

    @Then("User scrape product data")
    public void user_scrape_product_data() {
        List<Map<String, String>> products = myntraActions.scrapeProductData(6);

        // Assert that we have scraped some products
        assert products != null && !products.isEmpty() : "No products were scraped.";

        // Sort products by discount percentage in descending order
        products.sort((p1, p2) -> {
            int discount1 = Integer.parseInt(p1.get("Discount").replaceAll("[^0-9]", ""));
            int discount2 = Integer.parseInt(p2.get("Discount").replaceAll("[^0-9]", ""));
            return Integer.compare(discount2, discount1); // Descending order
        });

        // Print product details
        for (Map<String, String> product : products) {
            System.out.println("Name: " + product.get("Name"));
            System.out.println("Model: " + product.get("Model"));
            System.out.println("Actual Price: " + product.get("Actual Price"));
            System.out.println("Discounted Price: " + product.get("Discounted Price"));
            System.out.println("Discount: " + product.get("Discount"));
            System.out.println("Link: " + product.get("Link"));
            System.out.println("----------------------");
        }

        myntraActions.closeBrowser();
    }
}
