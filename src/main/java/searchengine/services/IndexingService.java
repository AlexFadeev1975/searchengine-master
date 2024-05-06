package searchengine.services;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface IndexingService {


    boolean runFullIndexing() throws InterruptedException, IOException, ExecutionException;

    void stopFullIndexing() throws InterruptedException;

    boolean oneIndexingSite(searchengine.config.Site link) throws InterruptedException, IOException, ExecutionException;

}
