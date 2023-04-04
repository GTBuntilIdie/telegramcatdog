package pro.sky.telegramcatdog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegramcatdog.constants.DocType;
import pro.sky.telegramcatdog.model.AdoptionDoc;

@Repository
public interface AdoptionDocRepository extends JpaRepository<AdoptionDoc, DocType> {
}
