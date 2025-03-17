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
import javax.persistence.EntityNotFoundException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import Metier.Entities.Admin;
import Metier.Entities.Commande;
import Metier.Entities.Notification;
import Metier.Entities.Patient;
import Metier.Entities.Pharmacien;
import Metier.Entities.Statut_Commande;
import Metier.Entities.Statut_Pharmacie;
import Metier.Entities.User;
@Stateless
public class AdminImpl implements AdminLocal, AdminRemote {
	//@PersistenceContext(unitName = "GestionPharmacie3")
EntityManagerFactory emf  =Persistence.createEntityManagerFactory("GestionPharmacie3"); 
	private EntityManager em=emf.createEntityManager();
//	 @PersistenceContext(unitName = "GestionPharmacie3")
	//   private EntityManager em;
    @Override
    public void changerStatutPharmacie(int id,int idAdmin) {
        Pharmacien pharmacien = em.find(Pharmacien.class, id);
        if (pharmacien != null) {
        	 if(pharmacien.getStatutPharmacie() == Statut_Pharmacie.Actif) {
        		  pharmacien.setStatutPharmacie(Statut_Pharmacie.Non_Actif);
        		  em.getTransaction().begin();
                  em.merge(pharmacien); 
                  em.getTransaction().commit();
                  Admin admin=em.find(Admin.class, idAdmin);
             	 Notification notification=new Notification();
             	 notification.setDestinataire_id(pharmacien.getId());
             	 notification.setUser(admin);
             	 String message="votre Pharmacien est en statut: "+Statut_Pharmacie.Non_Actif.toString();
             	 notification.setMessage(message);
             	em.getTransaction().begin();
        	    em.persist(notification);
        	    em.getTransaction().commit();
        	    envoyerEmail(pharmacien.getEmail(), message);
        	 }
        	 else {
        		 pharmacien.setStatutPharmacie(Statut_Pharmacie.Actif);
        		 em.getTransaction().begin();
                 em.merge(pharmacien); 
                 em.getTransaction().commit();
                 Admin admin=em.find(Admin.class, idAdmin);
            	 Notification notification=new Notification();
            	 notification.setDestinataire_id(pharmacien.getId());
            	 notification.setUser(admin);
            	 String message="votre Pharmacien est en statut: "+Statut_Pharmacie.Actif.toString();
            	 notification.setMessage(message);
              	em.getTransaction().begin();
         	    em.persist(notification);
         	    em.getTransaction().commit();
         	    envoyerEmail(pharmacien.getEmail(), message);
        	 }
        	 
        	 
          
        } else {
            throw new EntityNotFoundException("Pharmacien with id " + id + " not found.");
        }
    }
    @Override
	public void mettreAJourStatutPharmacieEtnotifierPatient(int idPharmacien, Statut_Pharmacie statut) {
		Pharmacien s=em.find(Pharmacien.class, idPharmacien);
		Statut_Pharmacie lastStatut=s.getStatutPharmacie();
		if(s!=null) {
		s.setStatutPharmacie(statut);
		em.getTransaction().begin();
		em.merge(s);
		em.getTransaction().commit();
		if(!statut.equals(lastStatut)) {
			Notification notification = new Notification(); 
			notification.setDestinataire_id(s.getId());
			  String message = "votre statut de pharmacie est changé "+s.getStatutPharmacie() ;
			    notification.setMessage(message);
			    em.getTransaction().begin();
			    em.persist(notification);
			    em.getTransaction().commit();
			    envoyerEmail(s.getEmail(), message); 
			}
			else {
				throw new IllegalArgumentException("le phrmacien avec l'ID " + idPharmacien + " n'existe pas.");
			}
		}
		
		
		
	}

    @Override
    public List<Pharmacien> consulterPharmacies() {
    	 if (em == null) {
    	        throw new IllegalStateException("EntityManager is not injected.");
    	    }
    	   // return em.createQuery("SELECT p FROM User p WHERE user_type = :type", User.class).setParameter("type", "PHARMACIEN").getResultList();
    	     return em.createQuery("SELECT p FROM Pharmacien p ", Pharmacien.class).getResultList();
    }
    @Override
    public List<Patient> consulterPatients() {
    	 if (em == null) {
    	        throw new IllegalStateException("EntityManager is not injected.");
    	    }
    	   // return em.createQuery("SELECT p FROM User p WHERE user_type = :type", User.class).setParameter("type", "PHARMACIEN").getResultList();
    	     return em.createQuery("SELECT p FROM Patient p ", Patient.class).getResultList();
    }

    @Override
    public List<User> consulterUtilisateurs() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    @Override
    public List<Pharmacien> consulterPharmacies(Statut_Pharmacie status) {
        return em.createQuery("SELECT p FROM Pharmacien p WHERE p.statutPharmacie = :status", Pharmacien.class)
                 .setParameter("status", status)
                 .getResultList();
    }
    @Override
	public boolean deleteUser(int id) {
    	
		Patient u=em.find(Patient.class, id);
		
		if(u!=null) {
			em.getTransaction().begin();
			//this.deleteNotificationByDestinataireId(id);
			//this.deleteNotificationByUserId(id);
			//this.deleteCommandeBypatientIdCom(id);
			//this.deleteCommandeBypharmacienIdCom(id);
			
			Query deleteOrdonnancesQuery = em.createQuery("DELETE FROM Commande o WHERE o.patient.id = :id");
            deleteOrdonnancesQuery.setParameter("id", id);
            deleteOrdonnancesQuery.executeUpdate();
			 System.out.println("User found: " + u.getId());
		        em.remove(u);
		        em.getTransaction().commit();
		        System.out.println("User removed successfully.");
		        return true;
		}
		else {
			Pharmacien u1=em.find(Pharmacien.class, id);
			if(u1!=null) {
				em.getTransaction().begin();
				//this.deleteNotificationByDestinataireId(id);
				//this.deleteNotificationByUserId(id);
				//this.deleteCommandeBypatientIdCom(id);
				//this.deleteCommandeBypharmacienIdCom(id);
				
				Query deleteOrdonnancesQuery = em.createQuery("DELETE FROM Commande o WHERE o.pharmacien.id = :id");
	            deleteOrdonnancesQuery.setParameter("id", id);
	            deleteOrdonnancesQuery.executeUpdate();
				 System.out.println("User found: " + u1.getId());
			        em.remove(u1);
			        em.getTransaction().commit();
			        System.out.println("User removed successfully.");
			        return true;
			}else {
            System.out.println("User not found!");
            return false;}
        }
	}

	@Override
	public List<Notification> recevoirNotificationLastTen(int idAdmin) {
		Query query = em.createQuery(
		        "SELECT n FROM Notification n WHERE n.destinataire_id = :idAdmin ORDER BY n.id DESC"
		    );
		query.setParameter("idAdmin", idAdmin);
		  query.setMaxResults(30);
		List<Notification> notifications = query.getResultList();
		return notifications;
		
	}

	@Override
	public int countUnseenNotifications(int idAdmin) {
		Query query = em.createQuery(
	            "SELECT COUNT(n) FROM Notification n WHERE n.destinataire_id = :idAdmin AND n.seen = false"
	        );
	        query.setParameter("idAdmin", idAdmin);
	        return ((Long) query.getSingleResult()).intValue();
	}

	@Override
	public int markNotificationsAsSeen(int idAdmin) {
		 Query query = em.createQuery(
			        "SELECT n FROM Notification n WHERE n.destinataire_id = :idAdmin AND n.seen = false"
			    );
			    query.setParameter("idAdmin", idAdmin);
			    List<Notification> notifications = query.getResultList();

			    int updatedCount = 0;
			    if (!notifications.isEmpty()) {
			        em.getTransaction().begin();
			        for (Notification notification : notifications) {
			            notification.setSeen(true); 
			            em.merge(notification);    //  
			            updatedCount++;
			        }
			        em.getTransaction().commit(); //  
			    }
			    return updatedCount;
	}

	@Override
	public Admin seConnecter(String email, String pass) {
		Query q = em.createQuery("SELECT a FROM Admin a WHERE a.email = :email AND a.mot_de_passe = :pass")
                .setParameter("email", email)
                .setParameter("pass", pass);
    List<Admin> result = q.getResultList();

    if (!result.isEmpty()) {
        return result.get(0);
    }

    return null;
	}

	@Override
	public boolean updateAdmin(int idAdmin, String username, String email, String telephone) {
		Admin a=em.find(Admin.class, idAdmin);
		if(a!=null) {
			em.getTransaction().begin();
			a.setUsername(username);
			a.setEmail(email);
			a.setTelephone(telephone);
			em.merge(a);
			em.getTransaction().commit();
			return true;
		}
		return false;
	}

	@Override
	public void créerAdmin(String username, String email, String telephone, String pass) {
	Admin admin=new Admin();
	admin.setUsername(username);
	admin.setEmail(email);
	admin.setTelephone(telephone);
	admin.setMot_de_passe(pass);
	em.getTransaction().begin();
	em.persist(admin);
	em.getTransaction().commit();	
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
	public void mot_de_passe(String email) {
		TypedQuery<User> q = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email);

List<User> list = q.getResultList();

if (list != null && !list.isEmpty()) {
String motDePasse = list.get(0).getMot_de_passe();
String message = "Votre mot de passe est : " + motDePasse;
envoyerEmail(email, message);
} else {
throw new IllegalArgumentException("Aucun utilisateur trouvé avec cet email : " + email);
}
		
	}
	
	
	 //-----------------notif
	@Override
	public List<Notification> findByDestinataireId(int destinataireId){
		 TypedQuery<Notification> q = em.createQuery("SELECT n FROM Notification n WHERE n.destinataire_id = :destinataireId", Notification.class)
	                .setParameter("destinataireId", destinataireId);

	List<Notification> list = q.getResultList();

	return list; 
	 }
	@Override
	public List<Notification> findByUserId(int userId){
		TypedQuery<Notification> q = em.createQuery("SELECT n FROM Notification n WHERE n.user.id = :userId", Notification.class)
                .setParameter("userId", userId);

List<Notification> list = q.getResultList();

return list;
	    	
	    }
	@Override
	public void deleteAll(List<Notification> notifications) {
	    for (Notification notification : notifications) {
	    	em.getTransaction().begin();
	        em.remove(notification);
	        em.getTransaction().commit();
	    }
	}

	
	@Override
	public void deleteNotificationByDestinataireId(int destinataireId) {
	    List<Notification> notifications = this.findByDestinataireId(destinataireId);
	    if (notifications != null && !notifications.isEmpty()) {
	        this.deleteAll(notifications);
	    } else {
	        throw new RuntimeException("No notifications found for destinataire_id: " + destinataireId);
	    }
	}

	@Override
	public void deleteNotificationByUserId(int userId) {
	    List<Notification> notifications = this.findByUserId(userId);
	    if (notifications != null && !notifications.isEmpty()) {
	        this.deleteAll(notifications);
	    } else {
	        throw new RuntimeException("No notifications found for user_id: " + userId);
	    }
	}
	//----------------------------command
	 //-----------------
		@Override
		public List<Commande>  findBypatientIdCom(int patientId){
			 TypedQuery<Commande> q = em.createQuery("SELECT n FROM Commande n WHERE n.patient.id = :patientId", Commande.class)
		                .setParameter("patientId", patientId);

		List<Commande> list = q.getResultList();

		return list; 
		 }
		@Override
		public List<Commande> findBypharmacienIdCom(int pharmacienId){
			TypedQuery<Commande> q = em.createQuery("SELECT n FROM Commande n WHERE n.pharmacien.id = :pharmacienId", Commande.class)
	                .setParameter("pharmacienId", pharmacienId);

	List<Commande> list = q.getResultList();

	return list;
		    	
		    }
		@Override
		public void deleteAllCom(List<Commande> commandes) {
		    for (Commande commande : commandes) {
		    	em.getTransaction().begin();
		        em.remove(commande);
		        em.getTransaction().commit();
		    }
		}

		
		@Override
		public void deleteCommandeBypatientIdCom(int patientId) {
		    List<Commande> commandes = this.findBypatientIdCom(patientId);
		    if (commandes != null && !commandes.isEmpty()) {
		        this.deleteAllCom(commandes);
		    } else {
		        throw new RuntimeException("No notifications found for destinataire_id: " + patientId);
		    }
		}

		@Override
		public void deleteCommandeBypharmacienIdCom(int pharmacienId) {
		    List<Commande> commandes = this.findBypharmacienIdCom(pharmacienId);
		    if (commandes != null && !commandes.isEmpty()) {
		        this.deleteAllCom(commandes);
		    } else {
		        throw new RuntimeException("No notifications found for user_id: " + pharmacienId);
		    }
		}


}
