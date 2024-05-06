package searchengine.services;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Page;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {


    private final SitesList sites;
    public Page page;

    @Autowired
    public SiteRepository siteRepository;
    @Autowired
    public PageRepository pageRepository;
    @Autowired
    public LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = formDSItem(site);
            total.setPages(total.getPages() + item.getPages());
            total.setLemmas(total.getLemmas() + item.getLemmas());
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    private DetailedStatisticsItem formDSItem (Site site) {

        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setName(site.getName());
        item.setUrl(site.getUrl());
        List<searchengine.model.Site> links = siteRepository.findByUrl(site.getUrl());
        searchengine.model.Site link = (!links.isEmpty()) ? links.get(0) : null;
        int pages = (link != null) ? pageRepository.findAllByIdSite(link.getId()).size() : 0;
        int lemmas = (link != null) ? lemmaRepository.findAllByIdSite(link.getId()).size() : 0;

        item.setPages(pages);
        item.setLemmas(lemmas);
        String status = (link != null) ? link.getStatus().toString() : "FAILED";
        item.setStatus(status);

        String lastError = (link != null) ? link.getLastError() : "Страница не индексирована";
        item.setError(lastError);

        long statusTime = (link != null) ? link.getStatusTime().getTime() : 0;
        item.setStatusTime(statusTime);

        return item;
    }
}
