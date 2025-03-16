package Metier.Entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@DiscriminatorValue("PHARMACIEN")

public class Pharmacien extends User  implements Serializable{
	@OneToMany(mappedBy = "pharmacien", cascade = CascadeType.REMOVE, orphanRemoval = true)
	 private List<Commande> commandesRecues;
	
	 private String nomPharmacie;
	 private double  latitude ;
	 private double longitude ;
	 @Enumerated(EnumType.STRING)
	 private Statut_Pharmacie statutPharmacie;
	
	

	public Pharmacien() {
		super();
		// TODO Auto-generated constructor stub
	}


	public Pharmacien(int id, String nom, String email, String telephone, String mot_de_passe) {
		super(id, nom, email, telephone, mot_de_passe);
		// TODO Auto-generated constructor stub
	}


	public Pharmacien(int id, String nom, String email, String telephone) {
		super(id, nom, email, telephone);
		// TODO Auto-generated constructor stub
	}


	public Pharmacien(String nomPharmacie) {
		super();
		this.nomPharmacie = nomPharmacie;
	
	}
	
	
	public Pharmacien(String nomPharmacie, Statut_Pharmacie statutPharmacie) {
		super();
		this.nomPharmacie = nomPharmacie;
		this.statutPharmacie = statutPharmacie;
	
	}


	public Statut_Pharmacie getStatutPharmacie() {
		return statutPharmacie;
	}


	public void setStatutPharmacie(Statut_Pharmacie statutPharmacie) {
		this.statutPharmacie = statutPharmacie;
	}


	public double getLatitude() {
		return latitude;
	}


	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}


	public double getLongitude() {
		return longitude;
	}


	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}


	public String getNomPharmacie() {
		return nomPharmacie;
	}


	public void setNomPharmacie(String nomPharmacie) {
		this.nomPharmacie = nomPharmacie;
	}



	
	 

	
	

}
