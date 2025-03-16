package Metier;

import java.util.List;

import javax.ejb.Local;

import Metier.Entities.Commande;
import Metier.Entities.Notification;
import Metier.Entities.Patient;
import Metier.Entities.Pharmacien;
import Metier.Entities.Statut_Commande;
import Metier.Entities.Statut_Pharmacie;

@Local
public interface PatientLocal {
	List<Commande> consulterHistoriqueCommande(int idPatient);
	String suivreEtatCommande(int idPharmacie,int idPatient);
	void créerCommande(int idPatient,byte [] image,Statut_Commande statut,int idPharmacie);
	List<Pharmacien> consulterCartePharmacies();
    List<Pharmacien>  consulterCartePharmacies(String nom,Statut_Pharmacie statut);
    void mettreAJourCompte(int id, String nom,String email,String mot_passe,String tele);
    int  recevoirNotificationNeverSeen(int idPatient);
    
    List<Notification>  recevoirNotificationLastTen(int idPatient);
    
    void créerComptePatient(String nom,String email,String mot_passe,String telephone);
   // int seConnecter(String email,String pass);
    int countUnseenNotifications(int idPatient);
    public int markNotificationsAsSeen(int idPatient);
    Patient seConnecter(String email,String pass);
    public boolean updatePatient(int patientId, String username, String email, String telephone);
    void deleteNotefication(int id);
    void deleteCommande(int idCommande);
    public boolean resetPassword(int patientId, String newPassword);
    


}
