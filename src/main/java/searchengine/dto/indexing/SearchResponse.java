package searchengine.dto.indexing;

import lombok.Data;
import searchengine.model.ResultPage;

import java.util.List;

@Data
public class SearchResponse {

    boolean result;

    int count;

    String error;

    List<ResultPage> data;

    public SearchResponse(boolean result, int count, List<ResultPage> data) {
        this.result = result;
        this.count = count;
        this.data = data;
    }

    public SearchResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
