package com.ddkolesnik.radparser;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.ddkolesnik.radparser.model.TradingEntity;
import com.ddkolesnik.radparser.service.ParseService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RadParserApplicationTests {

    private static Map<String, String> links = new HashMap<>();

    @BeforeAll
    public static void setup() {
        links.put("РАД-247162", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000296818%3Bmode%3Djust");
        links.put("РАД-247274", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000297046%3Bmode%3Djust");
        links.put("РАД-247164", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000296817%3Bmode%3Djust");
        links.put("РАД-247273", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000297045%3Bmode%3Djust");
        links.put("РАД-247163", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000296813%3Bmode%3Djust");
        links.put("РАД-247166", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000296816%3Bmode%3Djust");
        links.put("РАД-247275", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000297047%3Bmode%3Djust");
        links.put("РАД-247165", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000296812%3Bmode%3Djust");
        links.put("РАД-247168", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000296815%3Bmode%3Djust");
        links.put("РАД-247167", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000296811%3Bmode%3Djust");
        links.put("РАД-245650", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000295054%3Bmode%3Djust");
        links.put("РАД-246246", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000295989%3Bmode%3Djust");
        links.put("РАД-245651", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000295055%3Bmode%3Djust");
        links.put("РАД-247664", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000297420%3Bmode%3Djust");
        links.put("РАД-246247", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000295990%3Bmode%3Djust");
        links.put("РАД-246537", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000296112%3Bmode%3Djust");
        links.put("РАД-247816", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000297659%3Bmode%3Djust");
        links.put("РАД-245649", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000295052%3Bmode%3Djust");
        links.put("РАД-247160", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000296814%3Bmode%3Djust");
        links.put("РАД-247170", "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000296810%3Bmode%3Djust");
    }

    @Autowired
    private ParseService parseService;

    @Test
    public void getAuctionLinksTest() throws Exception {
        Map<String, String> links = getLinks();
        assertFalse(links.isEmpty());
        links.forEach((k, v) -> System.out.println(k + ":" + v));
    }

    private Map<String, String> getLinks() throws InterruptedException {
        Map<String, String> links = new HashMap<>();
        filterPage();
        while (true) {
            links.putAll(prepareLinks());
            SelenideElement nextPage = getNextPage();
            if (nextPage.exists()) {
                nextPage.click();
                Thread.sleep(3_000);
            } else {
                break;
            }
        }
        return links;
    }

    private void filterPage() {
        open("https://sales.lot-online.ru/e-auction/lots.xhtml");
        WebDriverRunner.getWebDriver().manage().window().fullscreen();
        WebDriverRunner.getWebDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        // Раскрываем форму поиска
        $(By.id("formMain:switcher-filter")).click();
        // Берём элемент формы "Регион"
        SelenideElement region = $(By.id("formMain:scmSubjectRFId"));
        assertNotNull(region);

        // Нажимаем треугольник, чтоб раскрылся список
        SelenideElement btnRF = region.find(By.cssSelector("div.ui-selectcheckboxmenu-trigger.ui-state-default.ui-corner-right"));
        assertNotNull(btnRF);
        btnRF.click();

        // Берём элемент раскрывшегося списка (панель)
        SelenideElement rfPanel = $(By.id("formMain:scmSubjectRFId_panel"));
        assertNotNull(rfPanel);
        // Находим лэйбл, на котором содержится "Тюменская" и кликаем по нему
        SelenideElement rfLabel = rfPanel.find(withText("Тюменская"));
        assertNotNull(rfLabel);
        rfLabel.click();

        // Находим крестик и закрываем выпадающий список
        rfPanel.find(By.cssSelector("span.ui-icon.ui-icon-circle-close")).click();

        // Находим текстовое поле для ключевых слов вводим туда текст
        SelenideElement keyWordsInput = $(By.id("formMain:itKeyWords"));
        assertNotNull(keyWordsInput);
        keyWordsInput.setValue("Тюмень");

        // Находим поле время проведения "C" и вставляем текущую дату
        SelenideElement dateFrom = $(By.id("formMain:auctionDatePlanBID_input"));
        assertNotNull(dateFrom);
        LocalDate now = LocalDate.now();
        dateFrom.setValue(now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        // Находим поле время проведения "ПО" и вставляем текущую дату
        SelenideElement dateTo = $(By.id("formMain:auctionDatePlanEID_input"));
        assertNotNull(dateTo);
        LocalDate after = now.plusMonths(6);
        dateTo.setValue(after.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        // Находим переключатель "Активные торги" и кликаем
        SelenideElement radioActive = $(By.id("formMain:selectIndPublish:0"));
        assertNotNull(radioActive);
        radioActive.click();

        // Находим кнопку "Искать" и кликаем по ней
        SelenideElement submitFilter = $(By.id("formMain:cbFilter"));
        assertNotNull(submitFilter);
        submitFilter.click();

        // Находим окно процесса загрузки и пока оно активно - ждём
        SelenideElement process = $(By.id("procId"));
        assertNotNull(process);
        process.shouldNotBe(Condition.visible);
    }

    private SelenideElement getNextPage() {
        // Находим счётчик страниц, если есть
        SelenideElement paginator = $(By.id("formMain:LotListPaginatorID"));
        assertNotNull(paginator);
        return paginator.find(By.cssSelector("span.item.next"));
    }

    private Map<String, String> prepareLinks() {
        Map<String, String> linksMap = new HashMap<>();
        // Находим таблицу с объявлениями
        SelenideElement contTender = $(By.cssSelector("div.cont-tender"));
        assertNotNull(contTender);
        // Находим переключатель представлений (интересует табличное представление)
        SelenideElement spanViewTable = contTender.find(By.id("formMain:formSelectTableType"));
        assertNotNull(spanViewTable);

        // Находим переключатель представления в табличный вид и кликаем его
        SelenideElement tableView = spanViewTable.find(By.cssSelector("li.view-table"));
        assertNotNull(tableView);
        tableView.click();
        tableView.shouldHave(Condition.cssClass("active"));

        // Находим объект таблицу на странице
        SelenideElement table = $(By.id("formMain:auctionLotCategoryTable"));
        assertNotNull(table);

        // Находим все строки таблицы на странице
        List<SelenideElement> rows = table.$$("tbody tr");
        rows.forEach(row -> {
            SelenideElement lot = row.find(By.cssSelector("span.field.field-lot"));
            if (lot != null) {
                SelenideElement aHref = row.find(By.cssSelector("a.command-link-text"));
                if (aHref != null) {
                    linksMap.put(lot.text(), aHref.attr("href"));
                }
            }
        });

        return linksMap;
    }

    @Test
    public void getAuctionInfoTest() {
        String lot = "РАД-236305";
        String url = "https://sales.lot-online.ru/e-auction/auctionLotProperty.xhtml?parm=lotUnid%3D960000295054%3Bmode%3Djust";
        TradingEntity tradingEntity = getAuctionInfo(lot, url);
        assertNotNull(tradingEntity);
    }

    private TradingEntity getAuctionInfo(String lot, String url) {
        Document document = parseService.getDocument(url);
        TradingEntity tradingEntity = new TradingEntity();
        tradingEntity.setLot(lot);
        tradingEntity.setUrl(url);
        tradingEntity.setDescription(getDescription(document));
        tradingEntity.setAddress(getAddress(document));
        tradingEntity.setTradingNumber(getTradingNumber(document));
        tradingEntity.setAuctionStep(getAuctionStep(document));
        tradingEntity.setEfrsbId(getEfrsbId(document));
        tradingEntity.setDepositAmount(getDepositAmount(document));
        tradingEntity.setTradingTime(getTradingTime(document));
        tradingEntity.setAcceptRequestsDate(getAcceptRequestsDate(document));
        tradingEntity.setLotSource("Российский Аукционный Дом");
        tradingEntity.setCity("");
        tradingEntity.setPrice(getPrice(document));
        return tradingEntity;
    }

    private Element getElement(Document document, String cssSelector) {
        Element product = document.selectFirst(cssSelector);
        assertNotNull(product);
        return product;
    }

    private String getDescription(Document document) {
        Element product = getElement(document, "div.product");
        List<Element> paragraphs = product.select("p");
        StringBuilder description = new StringBuilder();
        int counter = 0;
        for (Element p : paragraphs) {
            counter ++;
            if (counter == 1 || counter == 3) {
                description.append(p.text()).append(" ");
            }
        }
        return description.toString().trim();
    }

    private String getAddress(Document document) {
        Element product = getElement(document, "div.product");
        List<Element> paragraphs = product.select("p");
        StringBuilder address = new StringBuilder();
        for (Element p : paragraphs) {
            Element aHref = p.selectFirst("a[id=formMain:openAddrCardPreviewId]");
            if (aHref != null) {
                address.append(aHref.text());
            }
        }
        return address.toString().trim();
    }

    private String getTradingNumber(Document document) {
        String tradingNumber = "";
        Element tender = getElement(document, "div.tender");
        List<Element> paragraphs = tender.select("p");
        int counter = 0;
        for (Element p : paragraphs) {
            counter++;
            if (counter == 7) {
                tradingNumber = p.text().replaceAll("\\D", "");
                break;
            }
        }
        return tradingNumber;
    }

    private String getEfrsbId(Document document) {
        String efrsbId = "";
        Element tender = getElement(document, "div.tender");
        List<Element> paragraphs = tender.select("p");
        int counter = 0;
        for (Element p : paragraphs) {
            counter++;
            if (counter == 8) {
                efrsbId = p.text().replaceAll("\\D", "");
                break;
            }
        }
        return efrsbId;
    }

    private String getAuctionStep(Document document) {
        String auctionStep = "";
        Element tender = getElement(document, "div.tender");
        Element step = tender.selectFirst("div[id=formMain:opStepValue]");
        if (step != null) {
            List<Element> paragraphs = step.select("span.gray1");
            if (paragraphs.size() > 0) {
                auctionStep = paragraphs.get(0).text().replaceAll("\\s", "");
            }
        }
        return auctionStep;
    }

    private String getDepositAmount(Document document) {
        String depositAmount = "";
        Element tender = getElement(document, "div.tender");
        List<Element> spans = tender.select("span.gray1");
        String tmp;
        if (spans.size() == 11) {
            tmp = spans.get(8).text().replaceAll("\\s", "");
        } else {
            tmp = spans.get(5).text().replaceAll("\\s", "");
        }
        try {
            new BigDecimal(tmp);
            depositAmount = tmp;
        } catch (NumberFormatException ignored) {}
        return depositAmount;
    }

    private String getTradingTime(Document document) {
        String tradingTime = "";
        Element tender = getElement(document, "div.tender");
        Element paragraph = tender.selectFirst("p");
        Element em = paragraph.selectFirst("em");
        String tmp = em.text().replaceAll("[А-Яа-я]", "").trim();
        if (tmp.length() >= 33) {
            tradingTime = tmp.substring(0, 33);
        } else if (tmp.length() >= 18) {
            tradingTime = tmp.substring(0, 18);
        }
        return tradingTime;
    }

    private String getAcceptRequestsDate(Document document) {
        StringBuilder acceptRequestDate = new StringBuilder();
        Element tender = getElement(document, "div.tender");
        List<Element> paragraphs = tender.select("p");
        int counter = 0;
        for (Element p : paragraphs) {
            counter++;
            if (counter == 2) {
                Element em = p.selectFirst("em");
                if (em != null) {
                    List<Element> spans = em.select("span.gray1");
                    for (Element span : spans) {
                        acceptRequestDate.append(span.text()).append("\n");
                    }
                }
            }
        }
        return acceptRequestDate.toString().trim();
    }

    private BigDecimal getPrice(Document document) {
        BigDecimal price = BigDecimal.ZERO;
        Element priceDiv = document.selectFirst("div[id=formMain:opCostBValue]");
        if (priceDiv != null) {
            Element priceSpan = priceDiv.selectFirst("span.price");
            if (priceSpan != null) {
                List<Node> child = priceSpan.childNodes();
                if (child.size() > 0) {
                    String priceStr = child.get(0).toString();
                    try {
                        price = new BigDecimal(priceStr
                                .replaceAll("&nbsp;", "")
                                .replace(",", ".")
                                .trim());
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return price;
    }

}
