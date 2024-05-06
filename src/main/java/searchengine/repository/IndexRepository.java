package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Index;

import java.util.List;

public interface IndexRepository extends JpaRepository<Index, Integer> {

    List<Index> findDistinctByLemmaIdIn(List<Integer> lemmaId);

    List<Index> findByPageIdIn(List<Integer> pageId);
}
