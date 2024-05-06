package searchengine.indexingKit;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Morpholog {

    public Morpholog() {
    }

    public List<String> getLemmas(String text) throws IOException {

        List<String> usefulWords = new ArrayList<>();
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        List<String> listLemmas = new ArrayList<>();
        String clearedText = text.replaceAll("[\"*»*«*\\.*\\,*(*)*\'*]", " ");
        String[] splitText = clearedText.split(" ");
        for (int i = 0; i < splitText.length; i++) {

            String x = splitText[i];
            if (x.matches("[а-яА-ЯёЁ]+")) {
                String normalWord = x.toLowerCase();
                String normalWordTrim = normalWord.trim();
                usefulWords.add(normalWordTrim);
            }
        }
        for (int i = 0; i < usefulWords.size(); i++) {

            String x = usefulWords.get(i);
            List<String> listBasedForms = luceneMorphology.getNormalForms(x);
            String basedForm = listBasedForms.get(0);
            List<String> wordInfo = luceneMorphology.getMorphInfo(basedForm);
            String[] info = wordInfo.get(0).split("\\|");
            String[] wordReview = info[1].split(" ");
            if (wordReview[1].matches("С") || (wordReview[1].matches("П")) ||
                    (wordReview[1].matches("Г"))) {
                listLemmas.add(basedForm);
            }
        }
        return listLemmas;
    }

    private List<String> getSingleLemmas(List<String> listLemmas) {
        List<String> listSingleLemmas = new ArrayList<>();
        listLemmas.forEach(l -> {
            if (!listSingleLemmas.contains(l)) {
                listSingleLemmas.add(l);
            }
        });
        return listSingleLemmas;
    }

    public List<Lemma> getListPageContents(List<Page> pages, int idSite, int maxFrequencyWord) {

        HashMap<String, Integer> lemmaHolder = new HashMap<>();
        HashMap<String, Integer> lemmaHolderCleared = new HashMap<>();

        for (Page page : pages) {

            Page x = page;
            try {
                if ((x.getCode() == 200) && (x.getContent() != null)) {
                    List<String> lemmas = getSingleLemmas(getLemmas(x.getContent()));
                    lemmas.forEach(a -> {
                        int count = (lemmaHolder.containsKey(a)) ? lemmaHolder.get(a) + 1 : 1;
                        lemmaHolder.put(a, count);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        lemmaHolder.forEach((key, value) -> {
            if (value < maxFrequencyWord) {
                lemmaHolderCleared.put(key, value);
            }
        });
        List<Lemma> lemmaList = new ArrayList<>();
        lemmaHolderCleared.forEach((key, value) -> {
            Lemma lemma = new Lemma();
            lemma.setLemma(key);
            lemma.setFrequency(value);
            lemma.setIdSite(idSite);
            lemmaList.add(lemma);
        });

        return lemmaList;
    }


}

