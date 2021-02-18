package com.ddkolesnik.radparser;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.ddkolesnik.radparser.model.TradingEntity;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Alexandr Stegnin
 */

@SpringBootTest
public class RtsParserTest {

    private final static String RTS_URL = "https://it2.rts-tender.ru/?priceFrom=1000000" +
            "&procedureTypeNames=%D0%9F%D1%80%D0%B8%D0%B2%D0%B0%D1%82%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B3%D0%BE%D1%81%D1%83%D0%B4%D0%B0%D1%80%D1%81%D1%82%D0%B2%D0%B5%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B8%20%D0%BC%D1%83%D0%BD%D0%B8%D1%86%D0%B8%D0%BF%D0%B0%D0%BB%D1%8C%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B8%D0%BC%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B0" +
            "&procedureTypeNames=%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B0%D1%80%D0%B5%D1%81%D1%82%D0%BE%D0%B2%D0%B0%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B8%D0%BC%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B0" +
            "&procedureTypeNames=%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B8%D0%BC%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B0,%20%D0%BE%D0%B1%D1%80%D0%B0%D1%89%D0%B5%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B2%20%D1%81%D0%BE%D0%B1%D1%81%D1%82%D0%B2%D0%B5%D0%BD%D0%BD%D0%BE%D1%81%D1%82%D1%8C%20%D0%B3%D0%BE%D1%81%D1%83%D0%B4%D0%B0%D1%80%D1%81%D1%82%D0%B2%D0%B0" +
            "&procedureTypeNames=%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20(%D0%BF%D1%80%D0%BE%D0%B4%D0%B0%D0%B6%D0%B0)%20%D0%BD%D0%B5%D0%BF%D1%80%D0%BE%D1%84%D0%B8%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B8%20%D0%BF%D1%80%D0%BE%D1%84%D0%B8%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B0%D0%BA%D1%82%D0%B8%D0%B2%D0%BE%D0%B2" +
            "&propertyAddress=%D0%A2%D1%8E%D0%BC%D0%B5%D0%BD%D1%81%D0%BA%D0%B0%D1%8F" +
            "&auctionStartDateTimeFrom=2021-01-01" +
            "&tab=publicTrades";

    @Test
    public void getRtsLots() {
        waitFiltered(RTS_URL, "lots-table");
        SelenideElement lotsTable = getLotsTable();
        assertNotNull(lotsTable);
        List<SelenideElement> elements = lotsTable.$$(By.cssSelector("[data-type-id=proceduresListRow]"));
        assertNotNull(elements);
        assertFalse(elements.isEmpty());
        String lotNumber = "";
        String lotUrl;
        String acceptRequestsDate = "";
        String tradingTime = "";
        List<TradingEntity> tradingEntities = new ArrayList<>();
        for (SelenideElement el : elements) {
            List<String> fields = Arrays.asList(el.text().split("\n"));
            SelenideElement href = el.$(By.cssSelector("a[data-type-id=proceduresListRowIT1LotNumber]"));
            lotUrl = href.attr("href");
            if (lotUrl != null) {
                lotNumber = lotUrl.split("#")[1];
            }
            if (fields.size() > 10) {
                acceptRequestsDate = fields.get(8).concat(" ").concat(fields.get(9));
            }
            if (fields.size() > 11) {
                tradingTime = fields.get(10).concat(" ").concat(fields.get(11));
            }
            TradingEntity tradingEntity = new TradingEntity();
            tradingEntity.setLot(lotNumber);
            tradingEntity.setUrl(lotUrl);
            tradingEntity.setLotSource("РТС");
            tradingEntity.setAcceptRequestsDate(acceptRequestsDate);
            tradingEntity.setTradingTime(tradingTime);
            if (fields.size() > 4) {
                if (!fields.get(4).equalsIgnoreCase("Завершён")) {
                    tradingEntities.add(tradingEntity);
                }
            }
        }
        assertNotNull(tradingEntities);
    }

    @Test
    public void getRtsLotInfo() {
        waitFiltered("https://i.rts-tender.ru/main/auction/Trade/Privatization/View.aspx?Id=50362&Guid=088b8245-23c9-41aa-bbc6-f1ee005f2447#115194",
                "openPart");
        String description = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvName_lblValue")).text();
        assertNotNull(description);
        String price = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvLotPrice_lblValue")).text();
        assertNotNull(price);
        String address = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvDeliveryAddress_lblValue")).text();
        assertNotNull(address);
        String auctionStep = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvAuctionStep_lblValue")).text();
        assertNotNull(auctionStep);
        String depositAmount = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvEarnestSum_lblValue")).text();
        assertNotNull(depositAmount);
        String seller = $(By.id("BaseMainContent_MainContent_fvSeller_lblValue")).text();
        assertNotNull(seller);
    }

    private SelenideElement getLotsTable() {
        return $(By.cssSelector("div.lots-table"));
    }

    private void waitFiltered(String url, String processName) {
        open(url);
        WebDriverRunner.getWebDriver().manage().window().fullscreen();
        WebDriverRunner.getWebDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        SelenideElement process = $(By.className(processName));
        process.shouldBe(Condition.visible);
    }

}
