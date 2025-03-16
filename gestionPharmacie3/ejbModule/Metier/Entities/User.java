package Metier.Entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "USERS")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username", unique = true, nullable = false) 
    private String username;
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Notification> notifications;


    @Column(unique = true, nullable = false)
    private String email;
    private String telephone;
    private String mot_de_passe;

    public User() {
        super();
    }

    public User(int id, String username, String email, String telephone) {
        super();
        this.id = id;
        this.username = username;
        this.email = email;
        this.telephone = telephone;
    }

    public User(String username, String email, String telephone) {
        super();
        this.username = username;
        this.email = email;
        this.telephone = telephone;
    }

    public User(int id, String username, String email, String telephone, String mot_de_passe) {
        super();
        this.id = id;
        this.username = username;
        this.email = email;
        this.telephone = telephone;
        this.mot_de_passe = mot_de_passe;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() { // تعديل اسم الدالة
        return username;
    }

    public void setUsername(String username) { // تعديل اسم الدالة
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMot_de_passe() {
        return mot_de_passe;
    }

    public void setMot_de_passe(String mot_de_passe) {
        this.mot_de_passe = mot_de_passe;
    }

    
    
}
