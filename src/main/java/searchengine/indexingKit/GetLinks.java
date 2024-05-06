package searchengine.indexingKit;

import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Logger;

@Data
public class GetLinks {

    ForkJoinPool forkJoinPool = new ForkJoinPool();
    public Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    @Setter
    public String url;


    public GetLinks(String url) {
        this.url = url;
        ReadAllLinks links = new ReadAllLinks(this.url);
        ReadAllLinks.site = clearLine(this.url);
        ReadAllLinks.setLinks = new HashSet<>();
        ReadAllLinks.resultLinks = new TreeSet<>();
        this.forkJoinPool = new ForkJoinPool();
        Set<String> res = (Set<String>) forkJoinPool.invoke(links);
        printer(res);
    }

    public static class ReadAllLinks extends RecursiveTask {

        public static Set<String> setLinks;
        public static SortedSet<String> resultLinks;
        public static String site;
        public static String line;


        public ReadAllLinks(String line) {

            ReadAllLinks.line = line;
        }

        @Override
        protected Set<String> compute() {

            try {
                Connection.Response response = Jsoup.connect(line).userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; rv:2.2) Gecko/20110201")
                        .referrer("www.google.com").validateTLSCertificates(false).maxBodySize(0).ignoreContentType(true).ignoreHttpErrors(true).timeout(120000).execute();
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Document doc;

                if (response.statusCode() == 200) {
                    try {
                        doc = response.parse();
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                    @NonNull
                    Elements links = doc.select("a");

                    if (links.size() == 1) {
                        String reference = links.attr("abs:href");

                        if (setLinks.add(reference) && reference.contains(site)) {
                            resultLinks.add(reference);
                            new ReadAllLinks(reference);
                        }
                    } else {
                        List<ReadAllLinks> taskList = new ArrayList<>();
                        links.stream().map((link) -> link.attr("abs:href")).forEachOrdered((reference) -> {

                            if (setLinks.add(reference) && ((reference.contains("://" + site)) || (reference.contains("://www." + site)))) {
                                resultLinks.add(reference);
                                ReadAllLinks task = new ReadAllLinks(reference);
                                task.fork();
                                taskList.add(task);
                            }
                        });
                        for (ReadAllLinks task : taskList) {
                            task.join();
                        }
                    }
                } else {
                    System.out.println("код не 200");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultLinks;
        }
    }

    private void printer(Set<String> res) {
        res.stream().forEach(System.out::println);
    }

    public List<Page> linkStorage(Set<String> res, int idSite) {


        List<Page> pages = new ArrayList<>();
        Iterator<String> iterator = res.iterator();
        while (iterator.hasNext()) {
            String x = iterator.next();

            try {
                HTMLAnalyzer analyzer = new HTMLAnalyzer(x, ReadAllLinks.site);
                if ((analyzer.getPath() != null) & (analyzer.getContent() != null)) {
                    Page page = new Page();
                    page.setPath(analyzer.getPath());
                    page.setCode(analyzer.getCode());
                    page.setContent(analyzer.getContent());
                    page.setIdSite(idSite);
                    pages.add(page);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        return pages;
    }

    private String clearLine(String line) {

        String[] splitLines = line.split("://");
        if (splitLines.length > 1) {
            String[] splitLinesNext = splitLines[1].split(".");
            if (splitLinesNext.length > 2) {
                return splitLinesNext[1] + splitLinesNext[2];
            } else return splitLines[1];
        } else return line;
    }
}







