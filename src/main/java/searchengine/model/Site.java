package searchengine.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@Entity
@Table(name = "site")
public class Site implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "\"status\"")
    @Enumerated(EnumType.STRING)
    private StatusSite status;

    @Column(name = "status_time", columnDefinition = "TIMESTAMP")
    private Date statusTime;

    @Column(name = "last_error", nullable = true)
    private String lastError;

    @Column(name = "url")
    private String url;

    @Column(name = "\"name\"")
    private String name;

    @OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "id_site")
    private Set<Page> pages;

    @OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "id_site")
    private Set<Lemma> lemmas;

    public String toString() {

        return "(\"" + getStatus() + "\", " + "now()" + " ,\"" + getLastError() + "\",\"" + getUrl() + "\",\"" + getName() + "\")";
    }

}
