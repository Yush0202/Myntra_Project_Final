package components;

import com.microsoft.playwright.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.microsoft.playwright.options.LoadState;
import org.junit.Assert;


public class Utilities {
    private final Page page;
    private final Playwright playwright;
    private final Browser browser;

    public Utilities(Playwright playwright, Browser browser, Page page) {
        this.page=page;
        this.playwright=playwright;
        this.browser=browser;
    }

    public void openWebsite(String url) {
        page.navigate(url);
        page.waitForLoadState(LoadState.LOAD);
    }

    public void navigateToCategory(String category, String section) {
        page.hover(String.format("a[href='/shop/%s']", category.toLowerCase()));
        page.click(String.format("a[href='/%s-%s']", category.toLowerCase(), section.toLowerCase()));
        // Waiting for the first product's image to load
        page.waitForSelector("//li[@class = 'product-base'][1]//img");
        page.waitForLoadState(LoadState.LOAD);
    }

    public String fetchCategoryFromProductListingPage(){
        return page.locator("h1.title-title").innerText();
    }

    public void applyBrandFilter(String brandName) {
        page.locator(".filter-search-iconSearch").first().click();
        page.locator("input[placeholder='Search for Brand']").fill(brandName);
        page.locator("input[placeholder='Search for Brand']").press("Enter");
        page.waitForTimeout(1500);

        try{
            page.locator(String.format("//input[@type='checkbox' and @value='%s']/..", brandName)).click();
            page.waitForTimeout(2000);
        } catch (TimeoutError e){
            Assert.fail("Brand not found: " + brandName);
        }
    }

    public List<Map<String, String>> scrapeProductData(int pageCountLimit) {
        List<Map<String, String>> products = new ArrayList<>();
        int pageCount = 0;

        while (pageCount < pageCountLimit) {
            page.waitForSelector(".product-base"); // Wait until product listings are available

            // we iterate through all product elements on the page
            for (Locator product : page.locator(".product-base").all()) {
                String model = product.locator(".product-product").textContent();
                String priceText = product.locator(".product-price").textContent();
                String link = "https://www.myntra.com/" + product.locator("a").getAttribute("href");

                products.add(parseProductData(model, priceText, link));
            }

            // Navigating to the next page
            Locator nextButton = page.locator(".pagination-next");
            if (nextButton.count() > 0 && !nextButton.getAttribute("class").contains("pagination-disabled")) {
                nextButton.click();
                page.waitForLoadState(LoadState.LOAD);
                pageCount++;
            } else {
                break;
            }
        }
        return products; // we return the list of scraped products
    }

    //now we store product brand,model,link and extract the price and discount
    //we are formatting the product details
    public Map<String, String> parseProductData(String model, String priceText, String link) {
        Map<String, String> productData = new HashMap<>();
        productData.put("Model", model);
        productData.put("Link", link);

        // Extract prices and discount
        List<Integer> prices = extractPrices(priceText);
        productData.put("Actual Price", "Rs. " + (prices.size() > 1 ? prices.get(1) : prices.get(0)));
        productData.put("Discounted Price", "Rs. " + prices.get(0));
        productData.put("Discount", extractDiscount(priceText) + "% OFF");

        return productData;
    }

    //List to store the extracted price
    public List<Integer> extractPrices(String priceText) {
        List<Integer> prices = new ArrayList<>();

        //here we define regex pattern to match format Rs. amount
        //and iterate through all matches and extract price values
        Pattern pricePattern = Pattern.compile("Rs\\.\\s*(\\d+)");
        Matcher matcher = pricePattern.matcher(priceText);
        while (matcher.find()) {
            prices.add(Integer.parseInt(matcher.group(1)));
        }
        return prices;
    }

    //here we are defining a regex pattern to match discount in a particular format
    public int extractDiscount(String priceText) {
        Pattern discountPattern = Pattern.compile("\\((\\d+)% OFF\\)");
        Matcher matcher = discountPattern.matcher(priceText);

        //if discount found, we return percentage else we return 0
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    public List<Map<String, String>> sortScrapedProductsByDiscountInDescendingOrder(List<Map<String, String>> scrapedProducts){
        // Sort products by discount percentage in descending order
        scrapedProducts.sort((p1, p2) -> {
            int discount1 = Integer.parseInt(p1.get("Discount").replaceAll("[^0-9]", ""));
            int discount2 = Integer.parseInt(p2.get("Discount").replaceAll("[^0-9]", ""));
            return Integer.compare(discount2, discount1); // Descending order
        });
        return scrapedProducts;
    }

    public void closeBrowser() {
        browser.close();
        playwright.close();
    }
}
