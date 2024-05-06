package searchengine.model;

import lombok.Data;

import javax.persistence.Index;
import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "page", indexes = @Index(columnList = "path"))
public class Page implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_site")
    private int idSite;

    @Column(name = "path", length = 300)
    private String path;

    @Column(name = "code")
    private int code;

    @Column(name = "content", length = 3000000)
    private String content;


}