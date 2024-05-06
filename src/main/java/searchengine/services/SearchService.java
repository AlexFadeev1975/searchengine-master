package searchengine.services;

import searchengine.model.ResultPage;

import java.io.IOException;
import java.util.List;

public interface SearchService {

    List<ResultPage> searchEngine(String searchString, String site, int offset, int limit) throws IOException;
}
