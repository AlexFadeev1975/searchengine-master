package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;
import searchengine.model.StatusSite;

import java.util.Date;
import java.util.List;


@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {

    List<Site> findByUrl(String url);

    List<Site> findByStatus(StatusSite status);

    @Transactional
    @Modifying
    @Query(value = "update Site s set s.status = ?1 , s.statusTime = ?2 where s.id = ?3")
    void updateStatusSiteAndStatusTime(StatusSite statusSite, Date statusTime, int id);


}

