package com.ddkolesnik.radparser.service;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * @author Alexandr Stegnin
 */

@Slf4j
@Service
public class ParseService {

    private final WebClient webClient;

    public ParseService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Document getDocument(String url) {
        long timer = 6_000;
        try {
            Thread.sleep(timer);
        } catch (InterruptedException e) {
            log.error("Произошла ошибка: " + e.getLocalizedMessage());
        }
        HtmlPage page;
        try {
            webClient.setAjaxController(new AjaxController(){
                @Override
                public boolean processSynchron(HtmlPage page, WebRequest request, boolean async) {
                    return false;
                }
            });
            page = webClient.getPage(url);
            for (int i = 0; i < 20; i++) {
                if (page.asXml().contains("div[id=container-filter]")) {
                    break;
                }
                synchronized (page) {
                    page.wait(500);
                }
            }
            return Jsoup.parse(page.asXml());
        }  catch (HttpStatusException e) {
            waiting(e);
        } catch (Exception e) {
            log.error("Произошла ошибка: " + e.getLocalizedMessage());
        }
        return null;
    }

    private void waiting(HttpStatusException e) {
        if (e.getStatusCode() == 429) {
            log.error("Слишком много запросов {}", e.getLocalizedMessage());
            log.info("Засыпаем на 60 мин для обхода блокировки");
            try {
                Thread.sleep(60 * 1000 * 60);
            } catch (InterruptedException exception) {
                log.error(String.format("Произошла ошибка: [%s]", exception));
            }
        }
    }

}
