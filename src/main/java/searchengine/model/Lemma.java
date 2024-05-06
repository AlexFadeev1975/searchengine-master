package searchengine.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "lemma")
public class Lemma implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_site")
    private int idSite;

    @Column(name = "lemma")
    private String lemma;

    @Column(name = "frequency")
    private int frequency;


}
