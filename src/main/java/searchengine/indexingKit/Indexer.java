package searchengine.indexingKit;

import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Indexer {

    Morpholog morpholog = new Morpholog();
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public Indexer() throws InterruptedException {
    }

    public List<Index> indexer(List<Page> pages, List<Lemma> lemmas) throws IOException {

        List<Index> indexList = new ArrayList<>();
        for (Page x : pages) {

            String content = x.getContent();
            if (x.getCode() == 200 && content.contains("zzz")) {
                List<String> titleLemmas = new ArrayList<>();
                List<String> bodyLemmas = new ArrayList<>();
                AtomicInteger idLemma = new AtomicInteger();
                AtomicReference<Float> rank = new AtomicReference(0.0F);
                int pageId = x.getId();

                String[] splitContent = content.split("zzz");
                try {
                    titleLemmas = morpholog.getLemmas(splitContent[0]);
                    bodyLemmas = morpholog.getLemmas(splitContent[1]);
                    List<String> tempList = new ArrayList<>();
                    List<String> finalBodyLemmas = bodyLemmas;
                    for (String tl : titleLemmas) {

                        if (!tempList.contains(tl)) {
                            float count = (Collections.frequency(finalBodyLemmas, tl));
                            rank.set((float) (1.0 + count * 0.8));
                            int id = findIdLemma(lemmas, tl);
                            if (id != 0) {
                                idLemma.set(id);
                                Index index = new Index();
                                index.setLemmaId(idLemma.get());
                                index.setPageId(pageId);
                                index.setRank(rank.get());
                                indexList.add(index);
                                tempList.add(tl);

                            }
                        }
                    }
                    List<String> finalBodyLemmas1 = bodyLemmas;
                    for (String bl : bodyLemmas) {

                        if (!tempList.contains(bl)) {
                            float count = (Collections.frequency(finalBodyLemmas1, bl));
                            rank.set((float) (count * 0.8));
                            int id = findIdLemma(lemmas, bl);
                            if (id != 0) {
                                idLemma.set(id);
                                Index index = new Index();
                                index.setLemmaId(idLemma.get());
                                index.setPageId(pageId);
                                index.setRank(rank.get());
                                indexList.add(index);
                                tempList.add(bl);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return indexList;
    }

    private int findIdLemma(List<Lemma> lemmas, String lemma) {
        AtomicInteger idLemma = new AtomicInteger();
        for (Lemma lemma1 : lemmas) {
            String currentLemma = lemma1.getLemma();
            if (currentLemma.equals(lemma)) {
                idLemma.set(lemma1.getId());
            }
        }
        ;
        return idLemma.get();
    }
}
