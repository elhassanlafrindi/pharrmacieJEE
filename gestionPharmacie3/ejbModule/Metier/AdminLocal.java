package Metier;

import java.util.List;

import javax.ejb.Local;

import Metier.Entities.Admin;
import Metier.Entities.Commande;
import Metier.Entities.Notification;
import Metier.Entities.Patient;
import Metier.Entities.Pharmacien;
import Metier.Entities.Statut_Commande;
import Metier.Entities.Statut_Pharmacie;
import Metier.Entities.User;
@Local
public interface AdminLocal {
	void changerStatutPharmacie(int id,int idAdmin);
	List<Pharmacien> consulterPharmacies();
	List<Pharmacien> consulterPharmacies(Statut_Pharmacie status);
	List<User>  consulterUtilisateurs();
	  List<Notification>  recevoirNotificationLastTen(int idAdmin);
	int countUnseenNotifications(int idAdmin);
    public int markNotificationsAsSeen(int idAdmin);
    Admin seConnecter(String email,String pass);
    public boolean updateAdmin(int idAdmin, String username, String email, String telephone);
    void créerAdmin( String username, String email, String telephone,String pass);
    boolean deleteUser(int id);
	void mot_de_passe(String email);
	 public List<Patient> consulterPatients();
	 public void mettreAJourStatutPharmacieEtnotifierPatient(int idPharmacie, Statut_Pharmacie statut);
	 ///-------------notif
	 List<Notification> findByDestinataireId(int destinataireId);
	    List<Notification> findByUserId(int userId);
	    public void deleteNotificationByDestinataireId(int destinataireId);
	    public void deleteNotificationByUserId(int userId);
	    public void deleteAll(List<Notification> n);
	    //-----------------commande
	    List<Commande> findBypatientIdCom(int patientId);
	    List<Commande> findBypharmacienIdCom(int pharmacienId);
	    public void deleteCommandeBypatientIdCom(int patientId);
	    public void deleteCommandeBypharmacienIdCom(int pharmacienId);
	    public void deleteAllCom(List<Commande> n);
	    
}
