package Metier.Entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "commande")
public class Commande  implements Serializable{
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commande")
    private int idCommande; // Primary key

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id",nullable = true)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pharmacien_id",nullable = true)
    private Pharmacien pharmacien;
    
    @Lob 
	private byte[] image;
    private double mountant;
    @Enumerated(EnumType.STRING)
    private Statut_Commande statut;
 
    

	public Commande( byte[] image, double mountant, Statut_Commande statut) {
		super();
		
		this.image = image;
		this.mountant = mountant;
		this.statut = statut;
	}
	public Commande( Statut_Commande statut) {
		super();
		
		this.statut = statut;
	}
	public Commande( Statut_Commande statut, byte []image) {
		super();
		
		this.statut = statut;
		
		this.image=image;
				}
	
	public Patient getPatient() {
		return patient;
	}
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	public Pharmacien getPharmacien() {
		return pharmacien;
	}
	public void setPharmacien(Pharmacien pharmacien) {
		this.pharmacien = pharmacien;
	}
	public int getIdCommande() {
		return idCommande;
	}
	public void setIdCommande(int idCommande) {
		this.idCommande = idCommande;
	}
	public Commande() {
		super();
		// TODO Auto-generated constructor stub
	}

	public double getMountant() {
		return mountant;
	}
	public void setMountant(double mountant) {
		this.mountant = mountant;
	}

	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	
	public Statut_Commande getStatut() {
		return statut;
	}

	public void setStatut(Statut_Commande statut) {
		this.statut = statut;
	}

	
    
    

}