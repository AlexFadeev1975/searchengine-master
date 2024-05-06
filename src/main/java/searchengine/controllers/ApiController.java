package searchengine.controllers;

import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.ResultPage;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class ApiController extends Thread {

    private final StatisticsService statisticsService;
    private static boolean isRun = true;
    private final SearchService searchService;
    private final IndexingService indexingService;
    Site site;
    SitesList sitesList;
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, SitesList sitesList, SearchService searchService, Site site) {
        this.statisticsService = statisticsService;
        this.sitesList = sitesList;
        this.indexingService = indexingService;
        this.site = site;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")

    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {

        ApiController api = new ApiController(statisticsService, indexingService, sitesList, searchService, site);

        IndexingResponse indexingResponse = null;

        if (isRun) {
            api.start();
            isRun = false;
            indexingResponse = new IndexingResponse(true);
            return new ResponseEntity<>(indexingResponse, HttpStatus.OK);
        } else {
            indexingResponse = new IndexingResponse(false, "Индексация уже запущена");
            return new ResponseEntity<>(indexingResponse, HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> startOneSiteIndexing(@RequestBody String url) throws InterruptedException, IOException, ExecutionException {
        IndexingResponse indexingResponse = null;
        if (!url.isEmpty()) {
            String result = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8);
            String realUrl = result.substring(4);
            List<Site> sites = sitesList.getSites();
            for (Site value : sites) {
                if (value.getUrl().equals(realUrl)) {
                    site = value;
                    break;
                } else site = null;
            }

            if (site == null) {
                indexingResponse = new IndexingResponse(false, "Данная страница находится за пределами сайтов, " +
                        "указанных в конфигурационном файле");
            } else {
                isRun = false;
                if (indexingService.oneIndexingSite(site)) {
                    indexingResponse = new IndexingResponse(true);
                    isRun = true;
                    return ResponseEntity.ok(indexingResponse);

                } else {
                    indexingResponse = new IndexingResponse(false, "Индексация уже запущена");

                }

            }
        }
        if (indexingResponse.isResult()) {
            return ResponseEntity.ok(indexingResponse);
        } else return new ResponseEntity<>(indexingResponse, HttpStatus.FORBIDDEN);

    }


    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() throws InterruptedException {
        IndexingResponse indexingResponse = null;
        if (!isRun) {

            logger.info("прерывание включено");
            ApiController.this.interrupt();

            indexingService.stopFullIndexing();

            isRun = true;

            indexingResponse = new IndexingResponse(true);

        } else {
            indexingResponse = new IndexingResponse(false, "Индексация не запущена");
        }
        if (indexingResponse.isResult() && ApiController.this.isInterrupted()) {
            return ResponseEntity.ok(indexingResponse);
        } else return new ResponseEntity<>(indexingResponse, HttpStatus.FORBIDDEN);
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(String query,
                                                 String site,
                                                 int offset,
                                                 int limit) throws IOException {

        SearchResponse searchResponse;

        if (!query.isEmpty()) {
            List<ResultPage> resultPageList = searchService.searchEngine(query, site, offset, limit);
            if (!(resultPageList == null)) {
                int countPages = resultPageList.size();
                if (offset < resultPageList.size()) {
                    List<ResultPage> totalPageList = resultPageList.subList(offset, resultPageList.size());
                    if (totalPageList.size() > limit) {
                        resultPageList = totalPageList.subList(0, limit);
                    } else {
                        resultPageList = totalPageList;
                    }
                }
                searchResponse = new SearchResponse(true, countPages,
                        resultPageList);
            } else searchResponse = new SearchResponse(true, "Страниц не найдено либо запущена индексация страниц");
        } else searchResponse = new SearchResponse(false, "Задан пустой поисковый запрос");

        if (searchResponse.isResult()) {
            return ResponseEntity.ok(searchResponse);
        } else return new ResponseEntity<>(searchResponse, HttpStatus.FORBIDDEN);
    }

    @SneakyThrows
    @Override
    public void run() {

        if (indexingService.runFullIndexing()) {
            isRun = true;
        }

    }

}
