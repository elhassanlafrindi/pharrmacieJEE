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
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import Metier.Entities.Commande;
import Metier.Entities.Notification;
import Metier.Entities.Patient;
import Metier.Entities.Pharmacien;
import Metier.Entities.Statut_Commande;
import Metier.Entities.Statut_Pharmacie;

@Stateless
public class PatientImpl  implements PatientLocal,PatientRemote{
	//@PersistenceContext(unitName = "GestionPharmacie3")
	//   private EntityManager em;
	EntityManagerFactory emf  =Persistence.createEntityManagerFactory("GestionPharmacie3"); 
	private EntityManager em=emf.createEntityManager();
	@Override
	public List<Commande> consulterHistoriqueCommande(int idPatient) {
		
		List<Commande> list=null;
		Query q=em.createQuery("select c from Commande c where c.patient.id= :idPatient").setParameter("idPatient", idPatient);
		list=q.getResultList();
		return list;
	}

	@Override
	public String suivreEtatCommande(int idPharmacie, int idPatient) {
		Query q = em.createQuery(
			    "select c.statut from Commande c where c.patient.patient_id = :idPatient and c.pharmacien.pharmacien_id = :idPharmacie"
			)
			.setParameter("idPatient", idPatient)
			.setParameter("idPharmacie", idPharmacie);
		Statut_Commande statut = (Statut_Commande) q.getSingleResult();
	    return statut != null ? statut.toString() : null;
	}

	

	@Override
	public void créerCommande(int idPatient,byte [] image,Statut_Commande statut,int idPharmacie) {
		Pharmacien pharmacien=em.find(Pharmacien.class, idPharmacie);
		Patient p = em.find(Patient.class, idPatient);
		Commande c=new Commande();
		c.setImage(image);
		c.setPatient(p);
		c.setPharmacien(pharmacien);
	    c.setStatut(statut);
	    em.getTransaction().begin();
	    em.persist(c);
		em.getTransaction().commit();	
	    Notification notification = new Notification();
	    notification.setDestinataire_id(idPharmacie);
	    notification.setUser(p);
	    String message = "Vous avez une nouvelle commande de " + p.getUsername();
	    notification.setMessage(message);
	    em.getTransaction().begin();
		    em.persist(notification);
		    em.getTransaction().commit();
		  
		    String email=pharmacien.getEmail();
	        if (email != null && !email.isEmpty()) {
	            envoyerEmail(email, message);
	        } else {
	            System.out.println("Email introuvable pour le Pharmacien avec ID: " + idPharmacie);
	        }
	}
	@Override
	public List<Pharmacien> consulterCartePharmacies() {
		Query query = em.createQuery("SELECT p FROM Pharmacien p  WHERE  p.statutPharmacie = :statut").setParameter("statut", Statut_Pharmacie.Actif);
		List<Pharmacien> pharmacies = query.getResultList();
return pharmacies; 
	
	}

	@Override
	public List<Pharmacien> consulterCartePharmacies(String nom, Statut_Pharmacie statut) {
		Query query = em.createQuery("SELECT p FROM Pharmacien p WHERE p.nomPharmacie LIKE :nom AND p.statutPharmacie = :statut")
                .setParameter("nom", "%" + nom + "%") 
                .setParameter("statut", statut);
		List<Pharmacien> pharmacies = query.getResultList();
return pharmacies; 
	}

	@Override
	public void mettreAJourCompte(int id, String nom, String email, String mot_passe,String tele) {
		Patient patient=em.find(Patient.class, id);
		patient.setUsername(nom);
		patient.setEmail(email);
		patient.setMot_de_passe(mot_passe);
		patient.setTelephone(tele);
		em.getTransaction().begin();
		em.merge(patient);
		em.getTransaction().commit();
		
	}

	@Override
	public int recevoirNotificationNeverSeen(int idPatient) {
		Query query = em.createQuery(
		        "SELECT n FROM Notification n WHERE n.destinataire_id = :idPatient AND n.seen = false ORDER BY n.id DESC"
		    );
		query.setParameter("idPatient", idPatient);
		List<Notification> notifications = query.getResultList();
		if (!notifications.isEmpty()) {
			for(int i=0;i<notifications.size();i++) {
				Notification notification = notifications.get(i);
		        notification.setSeen(true);
		        em.getTransaction().begin();;
		        em.merge(notification); 
		        em.getTransaction().commit();
			}
	        
	    }
		return notifications.size();
	}

	@Override
	public List<Notification> recevoirNotificationLastTen(int idPatient) {
		Query query = em.createQuery(
		        "SELECT n FROM Notification n WHERE n.destinataire_id = :idPatient ORDER BY n.id DESC"
		    );
		query.setParameter("idPatient", idPatient);
		  query.setMaxResults(30);
		List<Notification> notifications = query.getResultList();
		return notifications;
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
	                return new PasswordAuthentication("hmoha4731@gmail.com", ""); 
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
	public void créerComptePatient(String nom, String email, String mot_passe, String telephone) {
		long count = em.createQuery("SELECT COUNT(p) FROM Patient p WHERE p.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
 
 if (count > 0) {
     throw new IllegalArgumentException("Email already exists");
 }

 Patient patient = new Patient();
 patient.setUsername(nom);
 patient.setEmail(email);
 patient.setMot_de_passe(mot_passe);
 patient.setTelephone(telephone);

 try {
     em.getTransaction().begin();
     em.persist(patient);
     em.getTransaction().commit();  // Commit the transaction if no error occurs
     Notification notification = new Notification(); 
		notification.setDestinataire_id(10);
		notification.setUser(patient);
		  String message = "une nouvelle creaction de compte de type patient" ;
		    notification.setMessage(message);
		    em.getTransaction().begin();
		    em.persist(notification);
		    em.getTransaction().commit();
		    envoyerEmail("admin@gamil.com", message);
 } catch (Exception e) {
     if (em.getTransaction().isActive()) {
         em.getTransaction().rollback();  
     }
     e.printStackTrace();  
     throw new RuntimeException("Error while creating the patient account", e);  
 }
		
	}
	/*public int seConnecter(String email,String pass) {
		Query q=em.createQuery("select p.id from Patient p where p.email =:email and p.mot_de_passe =:pass").setParameter("email",email)
				.setParameter("pass", pass);
		 List<Integer> result = q.getResultList();
		    
		    if (!result.isEmpty()) {
		        return result.get(0); // Return the first (and only) ID
		    }
		    
		    return -1;
		
	}*/
	

	@Override
	public int countUnseenNotifications(int idPatient) {
		Query query = em.createQuery(
	            "SELECT COUNT(n) FROM Notification n WHERE n.destinataire_id = :idPatient AND n.seen = false"
	        );
	        query.setParameter("idPatient", idPatient);
	        return ((Long) query.getSingleResult()).intValue();
	}

	@Override
	public int markNotificationsAsSeen(int idPatient) {
		 Query query = em.createQuery(
			        "SELECT n FROM Notification n WHERE n.destinataire_id = :idPatient AND n.seen = false"
			    );
			    query.setParameter("idPatient", idPatient);
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

	@Override
	public Patient seConnecter(String email, String pass) {
		 Query q = em.createQuery("SELECT p FROM Patient p WHERE p.email = :email AND p.mot_de_passe = :pass")
	                .setParameter("email", email)
	                .setParameter("pass", pass);
	    List<Patient> result = q.getResultList();

	    if (!result.isEmpty()) {
	        return result.get(0); 
	    }

	    return null; 
	}
	
	public boolean updatePatient(int patientId, String username, String email, String telephone) {
	    Patient patient = em.find(Patient.class, patientId);
	    
	    if (patient != null) {
	        em.getTransaction().begin();
	        patient.setUsername(username);
	        patient.setEmail(email);
	        patient.setTelephone(telephone);
	        em.merge(patient);  
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
	public void deleteCommande(int idCommande) {
		Commande c=em.find(Commande.class, idCommande);
		if(c!=null) {
		em.getTransaction().begin();
		em.remove(c);
		em.getTransaction().commit();
		Notification notification = new Notification(); 
		notification.setDestinataire_id(c.getPharmacien().getId());
		notification.setUser(c.getPatient());
		  String message = "Je m'excuse, j'ai supprimé la commande numéro "+c.getIdCommande()+" car je n'en ai plus besoin" ;
		    notification.setMessage(message);
		    em.getTransaction().begin();
		    em.persist(notification);
		    em.getTransaction().commit();
		    envoyerEmail(c.getPharmacien().getEmail(), message);
		}
		else {
			throw new IllegalArgumentException("La commande avec l'ID " + idCommande + " n'existe pas.");
		}}
	@Override
	public boolean resetPassword(int patientId,String newPassword) {
	    Patient patient = em.find(Patient.class, patientId);

	    if (patient != null) {
	        
	            em.getTransaction().begin();
	            patient.setMot_de_passe(newPassword);;    
	            em.merge(patient);
	            em.getTransaction().commit();
	            return true; 
	        
	    } else {
	        throw new IllegalArgumentException("Patient introuvable avec l'ID : " + patientId);
	    }
	}



}
