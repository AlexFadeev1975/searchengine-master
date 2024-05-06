package searchengine.dto.statistics;

import lombok.Data;
import searchengine.dto.indexing.ErrorResponse;

@Data
public class StartIndexingResponse {
    private boolean result;
    private ErrorResponse errorResponse;
}
