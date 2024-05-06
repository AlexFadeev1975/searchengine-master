package searchengine.indexingKit;

import lombok.NonNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.logging.Logger;

public class HTMLAnalyzer {

    public Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    String path, link, site;
    int code;
    String content;

    public String getContent() {
        return content;
    }

    public int getCode() {
        return code;
    }

    public String getPath() {
        return path;
    }

    public HTMLAnalyzer(String link, String site) throws IOException, InterruptedException {
        this.link = link;
        this.site = site;
        analyser();
    }

    public void analyser() throws IOException {
        @NonNull
        Connection.Response response;

        response = Jsoup.connect(link).userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; rv:2.2) Gecko/20110201")
                .referrer("www.google.com").validateTLSCertificates(false).maxBodySize(0).ignoreHttpErrors(true).ignoreContentType(true).timeout(120000).execute();

        code = response.statusCode();

        if ((code == 200) & (!link.contains(".pdf")) & (!link.contains(".jpg"))) {

            Document doc = getDocumentFromUrl(link);
            if (doc != null & link.getBytes().length < 300) {
                String[] splitlinkBySite = link.split(site);
                if (splitlinkBySite.length > 1) {
                    path = splitlinkBySite[1];
                } else path = link;

                String title = doc.title();
                content = title + " zzz " + doc.body().text();
                if (content.contains("'")) {
                    String cont = content.replaceAll("'", "");
                    content = cont;
                }
            }

        } else {

            logger.info("Код " + response.statusCode());
        }
    }

    public Document getDocumentFromUrl(String url) {
        Connection.Response response = null;
        try {
            response = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; rv:2.2) Gecko/20110201")
                    .referrer("www.google.com").validateTLSCertificates(false).maxBodySize(0).ignoreHttpErrors(true).ignoreContentType(true).timeout(120000).execute();
            Thread.sleep(150);
            Document doc = response.parse();
            return doc;
        } catch (IllegalArgumentException | InterruptedException | IOException ioe) {

            return null;
        }
    }
}
