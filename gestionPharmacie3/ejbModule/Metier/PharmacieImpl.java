package Metier;

import java.util.List;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import Metier.Entities.Commande;
import Metier.Entities.Notification;
import Metier.Entities.Patient;
import Metier.Entities.Pharmacien;
import Metier.Entities.Statut_Commande;
import Metier.Entities.Statut_Pharmacie;
@Stateless
public class PharmacieImpl implements PharmacieLocal , PharmacieRemote{

	EntityManagerFactory emf  =Persistence.createEntityManagerFactory("GestionPharmacie3"); 
	private EntityManager em=emf.createEntityManager();
	@Override
	public void mettreAJourStatutCommandeEtnotifierPatient(int idCommande, Statut_Commande statut,  double mountant) {
		Commande c=em.find(Commande.class, idCommande);
		Statut_Commande lastStatut=c.getStatut();
		if(c!=null) {
		c.setStatut(statut);
		c.setMountant(mountant);
		em.getTransaction().begin();
		em.merge(c);
		em.getTransaction().commit();
		if(!statut.equals(lastStatut)) {
			Notification notification = new Notification(); 
			notification.setDestinataire_id(c.getPatient().getId());
			notification.setUser(c.getPharmacien());
			  String message = "votre commande est en statut: "+c.getStatut() ;
			    notification.setMessage(message);
			    em.getTransaction().begin();
			    em.persist(notification);
			    em.getTransaction().commit();
			    envoyerEmail(c.getPatient().getEmail(), message); 
			}
			else {
				throw new IllegalArgumentException("La commande avec l'ID " + idCommande + " n'existe pas.");
			}
		}
		
		
		
	}

	@Override
	public List<Commande> consulterCommandes(int idPharmacie) {
		Query q=em.createQuery("select c from Commande c where c.pharmacien.id =:idPharmacie");
		q.setParameter("idPharmacie", idPharmacie);
		
		 List<Commande> list =q.getResultList();
		 return list;
	}

	@Override
	public List<Commande> consulterCommande(int idPharmacie, Statut_Commande statut) {
		Query q=em.createQuery("select c from Commande c where c.pharmacien.id =:idPharmacie and c.statut =:statut");
		q.setParameter("idPharmacie", idPharmacie);
		q.setParameter("statut", statut);
		 List<Commande> list =q.getResultList();
		 return list;
		
	}

	@Override
	public Pharmacien seConnecter(String email, String pass) {
		 Query q = em.createQuery("SELECT p FROM Pharmacien p WHERE p.email = :email AND p.mot_de_passe = :pass")
	                .setParameter("email", email)
	                .setParameter("pass", pass);
		 List<Pharmacien> list=q.getResultList();
		 if (!list.isEmpty()) {
		        return list.get(0); 
		    }
		return null;
	}

	@Override
	public boolean updatePharmacie(int idPharmacien, String username, String email, String telephone,String nomPharmacie,double latitude,double longitude) {
		Pharmacien p=em.find(Pharmacien.class, idPharmacien);
		if(p!=null) {
			p.setUsername(username);
			p.setEmail(email);
			p.setTelephone(telephone);
			p.setNomPharmacie(nomPharmacie);
			p.setLatitude(latitude);
			p.setLongitude(longitude);
			em.getTransaction().begin();
			em.merge(p);
			em.getTransaction().commit();
			
			return true;
		}
		return false;
	}
	public boolean updatePharmacien(int id, String username, String email, String telephone,String nomPharmacie,double Latitude,double longitude) {
	    Pharmacien pharmacien = em.find(Pharmacien.class, id);
	    
	    if (pharmacien != null) {
	        em.getTransaction().begin();
            pharmacien.setUsername(username);
            pharmacien.setEmail(email);
            pharmacien.setTelephone(telephone);
            pharmacien.setNomPharmacie(nomPharmacie);
            pharmacien.setLatitude(Latitude);
            pharmacien.setLongitude(longitude);
	        em.merge(pharmacien);  
	        em.getTransaction().commit();
	        
	        return true;  
	    }
	    return false; 
	}
	
	@Override
	public void deleteNotefication(int id) {
	    Notification notification = em.find(Notification.class, id); 
	    if (notification != null) {
	        em.getTransaction().begin(); 
	        em.remove(notification); 
	        em.getTransaction().commit(); 
	    } else {
	        throw new IllegalArgumentException("Notification with ID " + id + " not found.");
	    }
	}

	@Override
	public void créerComptePharmacie(String nom, String email, String mot_passe, String telephone) {
		long count = em.createQuery("SELECT COUNT(p) FROM Pharmacien p WHERE p.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
 
 if (count > 0) {
     throw new IllegalArgumentException("Email already exists");
 }
		Pharmacien pharmacien =new Pharmacien();
		Statut_Pharmacie s=Statut_Pharmacie.fromString("Non_Actif");
		pharmacien.setEmail(email);
		pharmacien.setMot_de_passe(mot_passe);
		pharmacien.setUsername(nom);
		pharmacien.setTelephone(telephone);
		pharmacien.setStatutPharmacie(s);
		
		em.getTransaction().begin();
		em.persist(pharmacien);
		em.getTransaction().commit();
		 Notification notification = new Notification(); 
			notification.setDestinataire_id(10);
			notification.setUser(pharmacien);
			  String message = "une nouvelle creaction de compte de type pharmacien" ;
			    notification.setMessage(message);
			    em.getTransaction().begin();
			    em.persist(notification);
			    em.getTransaction().commit();
			    envoyerEmail("admin@gamil.com", message);
		
	}

	@Override
	public int countUnseenNotifications(int idPharmacie) {
		
		Query query = em.createQuery(
	            "SELECT COUNT(n) FROM Notification n WHERE n.destinataire_id = :idPharmacie AND n.seen = false"
	        );
	        query.setParameter("idPharmacie", idPharmacie);
	        return ((Long) query.getSingleResult()).intValue();
	}

	@Override
	public List<Notification> recevoirNotificationLastTen(int idPharmacie) {
		Query query = em.createQuery(
		        "SELECT n FROM Notification n WHERE n.destinataire_id = :idPharmacie ORDER BY n.id DESC"
		    );
		query.setParameter("idPharmacie", idPharmacie);
		  query.setMaxResults(30);
		List<Notification> notifications = query.getResultList();
		return notifications;
	}

	@Override
	public int markNotificationsAsSeen(int idPharmacie) {
		 Query query = em.createQuery(
			        "SELECT n FROM Notification n WHERE n.destinataire_id = :idPharmacie AND n.seen = false"
			    );
			    query.setParameter("idPharmacie", idPharmacie);
			    List<Notification> notifications = query.getResultList();

			    int updatedCount = 0;
			    if (!notifications.isEmpty()) {
			        em.getTransaction().begin();
			        for (Notification notification : notifications) {
			            notification.setSeen(true); 
			            em.merge(notification);    
			            updatedCount++;
			        }
			        em.getTransaction().commit();  
			    }
			    return updatedCount;
	}

	private void envoyerEmail(String email, String message) {
	    try {
	        Properties properties = System.getProperties();
	        properties.put("mail.smtp.host", "smtp.gmail.com"); 
	        properties.put("mail.smtp.port", "587"); 
	        properties.put("mail.smtp.starttls.enable", "true"); 
	        properties.put("mail.smtp.auth", "true"); 

	        Session session = Session.getInstance(properties, new Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication("hmoha4731@gmail.com", "uzzbizrhmzcewypw");
	            }
	        });

	        MimeMessage mimeMessage = new MimeMessage(session);
	        mimeMessage.setFrom(new InternetAddress("hmoha4731@gmail.com"));

	        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));

	        mimeMessage.setSubject("Notification de commande");

	        mimeMessage.setText(message);

	        Transport.send(mimeMessage);
	        System.out.println("Email envoyé avec succès à " + email);
	    } catch (MessagingException e) {
	        e.printStackTrace();
	        System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
	    }
	}
	
	
	@Override
	public void deleteCommande(int idCommande) {
		Commande c=em.find(Commande.class, idCommande);
		if(c!=null) {
		em.getTransaction().begin();
		em.remove(c);
		em.getTransaction().commit();
		Notification notification = new Notification(); 
		notification.setDestinataire_id(c.getPharmacien().getId());
		notification.setUser(c.getPatient());
		  String message = "Je m'excuse, j'ai pas ces medecamentes "+c.getIdCommande() ;
		    notification.setMessage(message);
		    em.getTransaction().begin();
		    em.persist(notification);
		    em.getTransaction().commit();
		    envoyerEmail(c.getPatient().getEmail(), message);
		}
		else {
			throw new IllegalArgumentException("La commande avec l'ID " + idCommande + " n'existe pas.");
		}}

}
