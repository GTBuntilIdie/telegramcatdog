package pro.sky.telegramcatdog.service;

import org.springframework.stereotype.Service;
import pro.sky.telegramcatdog.constants.DocType;
import pro.sky.telegramcatdog.exception.AdoptionDocNotFoundException;
import pro.sky.telegramcatdog.model.AdoptionDoc;
import pro.sky.telegramcatdog.repository.AdoptionDocRepository;

@Service
public class AdoptionDocService {

    private final AdoptionDocRepository adoptionDocRepository;
    public AdoptionDocService(AdoptionDocRepository adoptionDocRepository) {
        this.adoptionDocRepository = adoptionDocRepository;
    }

    public AdoptionDoc createAdoptionDoc(AdoptionDoc adoptionDoc) {
        return adoptionDocRepository.save(adoptionDoc);
    }

    public AdoptionDoc readAdoptionDoc(DocType id) {
        AdoptionDoc adoptionDoc = adoptionDocRepository.findById(id).orElse(null);
        if (adoptionDoc == null) {
            throw new AdoptionDocNotFoundException(id.ordinal());
        }
        return adoptionDoc;
    }

    public AdoptionDoc updateAdoptionDoc(AdoptionDoc adoptionDoc) {
        if (adoptionDocRepository.findById(adoptionDoc.getId()).orElse(null) == null) {
            return null;
        }
        return adoptionDocRepository.save(adoptionDoc);
    }


}
