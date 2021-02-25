package com.ddkolesnik.radparser;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.ddkolesnik.radparser.model.TradingEntity;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Alexandr Stegnin
 */

@SpringBootTest
public class Fund72ParserTest {

    private static final String DEFAULT_URL = "http://www.fund72.ru/torgi/inye-ob-ekty-prodazh";

    @Test
    public void getFund72Lots() {
        Integer lastPage = null;
        int startPos = 0;
        int pageCount = 1;
        List<TradingEntity> tradingEntities = new ArrayList<>();
        while (true) {
            String lotsUrl = DEFAULT_URL;
            if (pageCount > 1) {
                startPos += 20;
                lotsUrl += "?start=" + startPos;
            }
            waitFiltered(lotsUrl, "pagination");
            SelenideElement lotsTable = getLotsTable();
            assertNotNull(lotsTable);
            List<SelenideElement> blogPosts = lotsTable.$$(By.cssSelector("[itemprop=blogPost]"));
            assertNotNull(blogPosts);
            assertFalse(blogPosts.isEmpty());
            for (SelenideElement blogPost : blogPosts) {
                SelenideElement fbPostDesc = blogPost.$(By.cssSelector("div.fbpostdesc"));
                if (!fbPostDesc.exists()) {
                    fbPostDesc = blogPost.$(By.cssSelector("div.fbpostdesc_full"));
                }
                assertTrue(fbPostDesc.exists());
                SelenideElement fieldsContainer = fbPostDesc.$(By.cssSelector("dl.fields-container"));
                assertNotNull(fieldsContainer);
                int counter = 0;
                String address = "";
                String acceptRequestsDate = "";
                List<SelenideElement> fieldsEntry = fieldsContainer.$$(By.cssSelector("dd.field-entry"));
                if (fieldsEntry.size() >= 5) {
                    for (SelenideElement el : fieldsEntry) {
                        if (counter == 2) {
                            SelenideElement addressField = el.$(By.cssSelector("span.field-value"));
                            assertNotNull(addressField);
                            address = addressField.text();
                            assertNotNull(address);
                        }
                        if (counter == 3) {
                            SelenideElement fromField = el.$(By.cssSelector("span.field-value"));
                            assertNotNull(fromField);
                            acceptRequestsDate = fromField.text().concat("-");
                            assertNotNull(acceptRequestsDate);
                        }
                        if (counter == 4) {
                            SelenideElement fromField = el.$(By.cssSelector("span.field-value"));
                            assertNotNull(fromField);
                            acceptRequestsDate += fromField.text();
                            assertNotNull(acceptRequestsDate);
                        }
                        counter++;
                    }
                }
                String tradingTime = "";
                String strPrice = "";
                SelenideElement fbItemPrice = blogPost.$(By.cssSelector("div.fbtimeprice.clearfix"));
                assertNotNull(fbItemPrice);
                List<SelenideElement> fieldValues = fbItemPrice.$$(By.cssSelector("span.field-value"));
                assertNotNull(fieldValues);
                assertFalse(fieldValues.isEmpty());
                if (fieldValues.size() == 2) {
                    tradingTime = fieldValues.get(0).text();
                    assertNotNull(tradingTime);
                    strPrice = fieldValues.get(1).text();
                    assertNotNull(strPrice);
                }
                SelenideElement aHref = blogPost.$(By.cssSelector("a[itemprop=url]"));
                assertNotNull(aHref);
                String url = aHref.attr("href");
                assertNotNull(url);
                String city = "Тюмень";
                String lotSource = "ФИТО";
                TradingEntity trading = new TradingEntity();
                trading.setCity(city);
                trading.setLotSource(lotSource);
                trading.setUrl(url);
                trading.setAddress(address);
                trading.setAcceptRequestsDate(acceptRequestsDate);
                trading.setTradingTime(tradingTime);
                BigDecimal price = BigDecimal.ZERO;
                try {
                    price = BigDecimal.valueOf(new Double(strPrice.replaceAll("\\D", "")));
                } catch (NumberFormatException ignored) {}
                trading.setPrice(price);
                tradingEntities.add(trading);
            }
            if (lastPage == null) {
                lastPage = getLastPageNumber();
            }
            if (lastPage == -1 || (pageCount == lastPage)) {
                break;
            } else {
                pageCount++;
            }
        }
        assertFalse(tradingEntities.isEmpty());
    }

    @Test
    public void getFund72LotInfo() {
        waitFiltered("http://www.fund72.ru/torgi/inye-ob-ekty-prodazh/1671-ob-ekt-nezavershennogo-stroitelstv",
                "one-blog");
        SelenideElement fbPostDescAr = $(By.cssSelector("div.fbpostdescar"));
        assertTrue(fbPostDescAr.exists());
        SelenideElement fieldsContainer = fbPostDescAr.$(By.cssSelector("dl.fields-container"));
        assertTrue(fieldsContainer.exists());
        List<SelenideElement> fieldsEntry = fieldsContainer.$$(By.cssSelector("dd.field-entry"));
        assertFalse(fieldsEntry.isEmpty());
        String lotNumber = "";
        if (fieldsEntry.size() >= 5) {
            lotNumber = fieldsEntry.get(fieldsEntry.size() - 1).text();
            assertNotNull(lotNumber);
        }
        SelenideElement fbArctMap = $(By.cssSelector("div.clearfix.fbarctmap"));
        String description = "";
        if (fbArctMap.exists()) {
            description = fbArctMap.$(By.cssSelector("p")).text();
            assertFalse(description.isEmpty());
        }
        SelenideElement fbPostSved = $(By.cssSelector("div.fbpostsved"));
        assertTrue(fbPostSved.exists());
        SelenideElement postSvedFieldContainer = fbPostSved.$(By.cssSelector("dl.fields-container"));
        assertTrue(postSvedFieldContainer.exists());
        List<SelenideElement> fieldEntries = postSvedFieldContainer.$$(By.cssSelector("dd.field-entry"));
        assertFalse(fieldEntries.isEmpty());
        String depositAmount = "";
        String auctionStep = "";
        int counter = 0;
        if (fieldEntries.size() >= 8) {
            for (SelenideElement element : fieldEntries) {
                if (counter == 6) {
                    depositAmount = element.text();
                    depositAmount = depositAmount.replaceAll(",", ".").replaceAll("\\D", "");
                    assertFalse(depositAmount.isEmpty());
                }
                if (counter == 5) {
                    auctionStep = element.text();
                    auctionStep = auctionStep.replaceAll(",", ".").replaceAll("\\D", "");
                    assertFalse(auctionStep.isEmpty());
                }
                counter++;
            }
        }
        assertFalse(description.isEmpty());
    }

    private void waitFiltered(String url, String processName) {
        open(url);
        WebDriverRunner.getWebDriver().manage().window().fullscreen();
        WebDriverRunner.getWebDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        SelenideElement process = $(By.className(processName));
        process.shouldBe(Condition.visible);
    }

    private SelenideElement getLotsTable() {
        return $(By.cssSelector("div.mainblog"));
    }

    private int getLastPageNumber() {
        int lastPage = -1;
        // Находим счётчик страниц, если есть
        SelenideElement paginator = $(By.cssSelector("div.pagination"));
        assertNotNull(paginator);
        List<SelenideElement> elements = paginator.findAll(By.cssSelector("li"));
        assertNotNull(elements);
        assertFalse(elements.isEmpty());
        String elText = elements.get(elements.size() - 3).text().split("\n")[0];
        try {
            lastPage = Integer.parseInt(elText);
        } catch (NumberFormatException ignored) {}
        return lastPage;
    }

}
