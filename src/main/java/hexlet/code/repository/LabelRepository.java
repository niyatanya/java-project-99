package hexlet.code.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import hexlet.code.model.Label;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByName(String name);
}
