package searchengine.indexingKit;

import lombok.SneakyThrows;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IndexingSite implements Runnable {


    public SiteRepository siteRepository;
    public PageRepository pageRepository;
    public LemmaRepository lemmaRepository;
    public IndexRepository indexRepository;

    private Morpholog morpholog;
    private Indexer indexer;
    private GetLinks getLinks;
    private searchengine.config.Site link;

    public IndexingSite(SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, Morpholog morpholog, Indexer indexer, GetLinks getLinks, searchengine.config.Site link) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.morpholog = morpholog;
        this.indexer = indexer;
        this.getLinks = getLinks;
        this.link = link;
    }

    @SneakyThrows
    @Override
    public void run() {

        if (link != null) {

            morpholog = new Morpholog();
            indexer = new Indexer();

            List<Site> siteList = siteRepository.findByUrl(link.getUrl());

            if (!siteList.isEmpty()) {
                int idSite = siteList.get(0).getId();
                List<Page> pageList = pageRepository.findAllByIdSite(idSite);
                List<Integer> pageIdList = new ArrayList<>();
                pageList.forEach(page -> {
                    pageIdList.add(page.getId());
                });
                List<Index> indexList = indexRepository.findByPageIdIn(pageIdList);
                indexRepository.deleteAll(indexList);
                siteRepository.deleteById(idSite);
            }
            Site site = new Site();
            site.setName(link.getName());
            site.setUrl(link.getUrl());
            site.setStatus(StatusSite.INDEXING);
            site.setStatusTime(new Date());
            site.setLastError(null);
            Site savedSite = siteRepository.saveAndFlush(site);
            int idSite = savedSite.getId();

            getLinks = new GetLinks(site.getUrl());

            List<Page> pages = getLinks.linkStorage(GetLinks.ReadAllLinks.resultLinks, idSite);

            pageRepository.saveAll(pages);

            List<Lemma> lemmaList = morpholog.getListPageContents(pages, idSite, pages.size());

            lemmaRepository.saveAllAndFlush(lemmaList);

            List<Page> listPages = pageRepository.findAllByIdSite(idSite);

            List<Lemma> listLemmas = lemmaRepository.findAllByIdSite(idSite);

            List<Index> indexList = indexer.indexer(listPages, listLemmas);

            indexRepository.saveAll(indexList);
            indexList.clear();

            if (!listPages.isEmpty() && !listLemmas.isEmpty()) {
                site.setStatus(StatusSite.INDEXED);
            } else {
                site.setStatus(StatusSite.FAILED);
            }

            siteRepository.updateStatusSiteAndStatusTime(site.getStatus(), new Date(), site.getId());

        }

    }
}
