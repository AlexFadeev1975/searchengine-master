package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.indexingKit.Morpholog;
import searchengine.indexingKit.SearchSystem;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    public SiteRepository siteRepository;
    @Autowired
    public PageRepository pageRepository;
    @Autowired
    public LemmaRepository lemmaRepository;
    @Autowired
    public IndexRepository indexRepository;
    private Morpholog morpholog;
    private SearchSystem searchSystem;

    @Override
    public List<ResultPage> searchEngine(String searchString, String site, int offset, int limit) throws IOException {

        morpholog = new Morpholog();
        searchSystem = new SearchSystem();
        List<Site> listSites = new ArrayList<>();
        List<Lemma> lemmaList = lemmaRepository.findByLemmaIn(morpholog.getLemmas(searchString));

        List<Site> siteList = siteRepository.findByStatus(StatusSite.INDEXING);

        if (!lemmaList.isEmpty() & siteList.isEmpty()) {
            lemmaList.sort(Comparator.comparingInt(Lemma::getFrequency));
            List<Index> indexList = indexRepository.findDistinctByLemmaIdIn(lemmaList.stream().map(Lemma::getId).toList());

            HashMap<Integer, Float> mapPageIdToRank = searchSystem.pageIdAndRelRankFinder(indexList, lemmaList);

            List<Page> resultPages = pageRepository.findAllById(mapPageIdToRank.keySet().stream().toList());
            if (site != null) {
                List<Site> listSite = siteRepository.findByUrl(site);
                Site findedSite = listSite.get(0);
                if (findedSite == null) {
                    return new ArrayList<>();
                } else {

                    listSites.add(findedSite);
                    return searchSystem.getResultPages(mapPageIdToRank, resultPages, lemmaList, listSites, offset, limit);
                }
            } else {
                listSites = siteRepository.findAll();

                return searchSystem.getResultPages(mapPageIdToRank, resultPages, lemmaList, listSites, offset, limit);
            }
        } else return null;

    }
}

