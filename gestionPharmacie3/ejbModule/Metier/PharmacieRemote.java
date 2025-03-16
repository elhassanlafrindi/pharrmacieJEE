package Metier;

import java.util.List;

import javax.ejb.Remote;

import Metier.Entities.Commande;
import Metier.Entities.Notification;
import Metier.Entities.Pharmacien;
import Metier.Entities.Statut_Commande;


@Remote
public interface PharmacieRemote {
	void mettreAJourStatutCommandeEtnotifierPatient(int idCommande, Statut_Commande statut, double mountant);
 //   void notifierPatient(int idPatient, String message);
    //void preparerTraitement(int id,double mountant);
    List<Commande> consulterCommandes(int idPharmacie);
    List<Commande> consulterCommande(int idPharmacie,Statut_Commande statut);
    Pharmacien seConnecter(String email,String pass);
    public boolean updatePharmacie(int idPharmacien, String username, String email, String telephone,String nomPharmacie,double latitude,double longitude);
    void créerComptePharmacie(String nom,String email,String mot_passe,String telephone);
    int countUnseenNotifications(int idPharmacie);
    List<Notification>  recevoirNotificationLastTen(int idPharmacie);
    public int markNotificationsAsSeen(int idPharmacie);
    public void deleteNotefication(int id);
    public void deleteCommande(int idCommande);
	public boolean updatePharmacien(int id, String username, String email, String telephone,String nomPharmacie,double Latitude,double longitude);
}
