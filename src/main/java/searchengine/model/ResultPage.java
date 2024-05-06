package searchengine.model;

import lombok.Data;

@Data
public class ResultPage {

    private String site;

    private String siteName;

    private String url;

    private String title;

    private String snippet;

    private float relevance;

}
