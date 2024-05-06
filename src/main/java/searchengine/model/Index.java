package searchengine.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "\"index\"")
public class Index implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "page_id")
    private int pageId;

    @Column(name = "lemma_id")
    private int lemmaId;

    @Column(name = "\"rank\"")
    private float rank;

    public Index() {
    }

    public Index(int pageId, int lemmaId) {
        int id = 0;
        this.pageId = pageId;
        this.lemmaId = lemmaId;
        float rank = 0;
    }

}