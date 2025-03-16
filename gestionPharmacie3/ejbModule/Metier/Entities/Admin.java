package Metier.Entities;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@DiscriminatorValue("ADMIN")

public class Admin extends User  implements Serializable {

	public Admin() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Admin(int id, String nom, String email, String telephone, String mot_de_passe) {
		super(id, nom, email, telephone, mot_de_passe);
		// TODO Auto-generated constructor stub
	}

	public Admin(int id, String nom, String email, String telephone) {
		super(id, nom, email, telephone);
		// TODO Auto-generated constructor stub
	}

}
