package components;

import com.microsoft.playwright.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.microsoft.playwright.options.LoadState;


public class MyntraActions {

    private final Page page;

    private final Playwright playwright;

    private final Browser browser;

    public MyntraActions(Page page, Playwright playwright, Browser browser) {
        this.page=page;
        this.playwright=playwright;
        this.browser=browser;
    }


    public void openMyntraWebsite() {
        page.navigate("https://www.myntra.com/");

        page.waitForLoadState(LoadState.LOAD);
    }



    public void navigateToCategory(String category) {
        page.hover("a[href='/shop/men']");

        //switch case for tshirt and casual shirt. for van heusen click tshirt
        //and for roadster click casual shirt

        switch (category.toLowerCase()) {
            case "men t-shirts":
                page.click("a[href='/men-tshirts']");
                break;
            case "men casual shirts":
                page.click("a[href='/men-casual-shirts']");
                break;
            default:
                System.out.println("Invalid category: " + category);
                return;
        }
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }




    public String applyBrandFilter(String brandName) {

        //find the searchicon and click on the first appearing as on the side panel we have multiple
        //first appearing is brand so we click on it
        //search for the brand name and click on the first checkbox

        page.locator(".filter-search-iconSearch").first().click();
        page.waitForSelector("input[placeholder='Search for Brand']");
        page.locator("input[placeholder='Search for Brand']").fill(brandName);
        page.locator("input[placeholder='Search for Brand']").press("Enter");
        page.waitForTimeout(1500);

        Locator brandCheckbox = page.locator("input[type='checkbox'][value='" + brandName + "']");

        if (brandCheckbox.count() > 0) {
            brandCheckbox.first().evaluate("(checkbox) => checkbox.click()");
            return "Success";
        } else {
            System.out.println("Brand not found: " + brandName);
            return "Brand not found";
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

            Locator nextButton = page.locator(".pagination-next");
            if (nextButton.count() > 0 && !nextButton.getAttribute("class").contains("pagination-disabled")) {
                nextButton.click();
                page.waitForLoadState(LoadState.LOAD);
                //page.waitForTimeout(3000);
                pageCount++;
            } else {
                break;
            }
        }
        return products; // we return the list of scraped products
    }


    //now we store product brand,model,link and extract the price and discount
    //we are formatting the product details
    private Map<String, String> parseProductData(String model, String priceText, String link) {
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
    private List<Integer> extractPrices(String priceText) {
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
    private int extractDiscount(String priceText) {
        Pattern discountPattern = Pattern.compile("\\((\\d+)% OFF\\)");
        Matcher matcher = discountPattern.matcher(priceText);

        //if discount found, we return percentage else we return 0
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    public void closeBrowser() {
        browser.close();
        playwright.close();
    }
}
