package pro.sky.telegramcatdog.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Volunteer {

    @Id
    private int id;

    public Volunteer() {
    }

    public int getId() {
        return id;
    }
}
