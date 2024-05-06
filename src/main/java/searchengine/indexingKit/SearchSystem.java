package searchengine.indexingKit;

import org.tartarus.snowball.ext.RussianStemmer;
import searchengine.model.*;

import java.util.*;

public class SearchSystem {

    public SearchSystem() {
    }

    public HashMap<Integer, Float> pageIdAndRelRankFinder(List<Index> mapPageIdLemmaId, List<Lemma> lemmaList) {

        if (!lemmaList.isEmpty()) {

            HashMap<Integer, Float> mapPageIdToRank = new HashMap<>();
            List<Integer> listPageId = new ArrayList<>();
            Set<String> tempSet = new HashSet<>();
            lemmaList.forEach(l -> tempSet.add(l.getLemma()));

            mapPageIdLemmaId.forEach(x -> {
                int pageID = x.getPageId();
                listPageId.add(pageID);
                int pageFrequency = Collections.frequency(listPageId, x.getPageId());
                int passedFrequency = tempSet.size();
                if (pageFrequency == passedFrequency) {
                    int pageId = x.getPageId();
                    float rank = x.getRank();
                    float absRank = (mapPageIdToRank.containsKey(pageId)) ? mapPageIdToRank.get(pageId) + rank : rank;
                    mapPageIdToRank.put(pageId, absRank);
                }
            });
            mapPageIdToRank.forEach((pageId, rank) -> {
                mapPageIdToRank.put(pageId, (rank / Collections.max(mapPageIdToRank.values())));
            });
            return mapPageIdToRank;
        }
        return null;
    }

    public List<ResultPage> getResultPages(HashMap<Integer, Float> mapPageIdToRank, List<Page> resultPages, List<Lemma> lemmaList, List<Site> listSites, int offset, int limit) {
        List<ResultPage> resultPageList = new ArrayList<>();
        if (!mapPageIdToRank.isEmpty()) {
            for (int i = 0; i < resultPages.size(); i++) {
                Page page = resultPages.get(i);
                ResultPage resultPage = new ResultPage();
                Site site = getSite(page.getIdSite(), listSites);
                if (site == null) {
                    continue;
                }
                resultPage.setSite(site.getUrl());
                resultPage.setSiteName(site.getName());
                resultPage.setUrl(page.getPath());
                String[] arrContent = page.getContent().split("zzz");
                resultPage.setTitle(arrContent[0]);
                resultPage.setSnippet(getSnippet(arrContent[1], lemmaList));//
                resultPage.setRelevance(mapPageIdToRank.get(page.getId()));
                resultPageList.add(resultPage);
            }
            resultPageList.sort(Comparator.comparing(ResultPage::getRelevance).reversed());

            return resultPageList;
        } else return null;
    }

    private String getStem(String word) {

        RussianStemmer stemmer = new RussianStemmer();
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent();
    }

    private String getSnippet(String content, List<Lemma> lemmas) {

        String clearedContent = content.replaceAll("[\"*»*«*\\.*\\,*(*)*]", " ");
        String[] arrContent = clearedContent.split(" ");
        List<String> stemList = new ArrayList<>();
        String snippet = "";
        int startWord = arrContent.length;
        int endWord = 0;
        Set<String> lemmaSet = new HashSet<>();
        lemmas.forEach(l -> lemmaSet.add(l.getLemma()));
        lemmaSet.forEach(l -> stemList.add(getStem(l)));
        List<String> compareList = new ArrayList<>();
        for (int i = 0; i < arrContent.length; i++) {
            String currentWord = arrContent[i].toLowerCase(Locale.ROOT);
            String newWord = currentWord.replace("\"", "");
            String currentStem = getStem(newWord);

            if (compareList.isEmpty() & stemList.contains(currentStem)) {
                startWord = i;
                endWord = i + 50;
                compareList.add(currentStem);
                arrContent[i] = "<b>" + arrContent[i] + "</b>";
            }
            if (compareList.contains(currentStem)) {
                arrContent[i] = "<b>" + arrContent[i] + "</b>";
            }
            if (stemList.contains(currentStem) & !compareList.contains(currentStem) & (compareList.size() <= lemmaSet.size())) {
                endWord = i;
                compareList.add(currentStem);
                arrContent[i] = "<b>" + arrContent[i] + "</b>";
            }
        }
        startWord = (startWord != 0) ? startWord - 1 : startWord;
        endWord = startWord + 50;
        //       endWord = (endWord != arrContent.length - 1) ? endWord + 2: endWord;
        //      if ((endWord - startWord) < 30) {
        //         endWord = startWord + 30;
        //      }
        String[] arrSnippet = Arrays.copyOfRange(arrContent, startWord, endWord);
        snippet = String.join(" ", arrSnippet);
        return snippet;
    }

    private Site getSite(int idSite, List<Site> listSites) {
        Site site = null;
        for (int i = 0; i < listSites.size(); i++) {
            if (listSites.get(i).getId() == idSite) {
                site = listSites.get(i);
            }
        }
        return site;
    }

}
