package Metier.Entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@DiscriminatorValue("PATIENT")

public class Patient extends User  implements Serializable{
	
	@OneToMany(mappedBy = "patient",fetch=FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Commande> historiqueCommandes;


	public Patient() {
		super();
	
	}

	public Patient(int id, String nom, String email, String telephone, String mot_de_passe) {
		super(id, nom, email, telephone, mot_de_passe);
		// TODO Auto-generated constructor stub
	}

	public Patient(int id, String nom, String email, String telephone) {
		super(id, nom, email, telephone);
		// TODO Auto-generated constructor stub
	}

	
	

}
