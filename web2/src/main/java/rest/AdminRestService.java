package rest;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import Metier.AdminImpl;
import Metier.AdminLocal;
import Metier.Entities.Admin;
import Metier.Entities.Commande;
import Metier.Entities.Notification;
import Metier.Entities.Patient;
import Metier.Entities.Pharmacien;
import Metier.Entities.Statut_Commande;
import Metier.Entities.Statut_Pharmacie;
import Metier.Entities.User;

@Path("/admin")
public class AdminRestService {
	@EJB
	private AdminLocal adminImpl;
		
	  public AdminRestService() {
		super();
		adminImpl=new AdminImpl();
	}

	@PUT
	    @Path("{idAdmin}/changerStatutPharmacie/{id}")
	    @Consumes(MediaType.APPLICATION_JSON)
	    @Produces(MediaType.APPLICATION_JSON)
	    public Response changerStatutPharmacie(@PathParam("idAdmin") int idAdmin,@PathParam("id") int id) {
	        try {
	            adminImpl.changerStatutPharmacie(id,idAdmin);
	            return Response.ok().build();
	        } catch (EntityNotFoundException e) {
	            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
	        }
	    }
	@POST
	@Path("/pharmacie/{idPharmacie}/updateStatut")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response mettreAJourStatutCommande(
	        @PathParam("idPharmacie") int idPharmacie,
	        @FormParam("statut") String statut) {

	    try {
	        Statut_Pharmacie statutPharmacie;
	        try {
	        	 statutPharmacie = Statut_Pharmacie.fromString(statut);
	        } catch (IllegalArgumentException e) {
	        	return Response.status(Response.Status.BAD_REQUEST)
	                    .entity("{\"error\": \"Statut invalide. Les valeurs possibles sont: [en_attente, Reçu, En_Préparation, Prête_à_Récupérée].\"}")
	                    .build();
	        }

	      
	        adminImpl.mettreAJourStatutPharmacieEtnotifierPatient(idPharmacie, statutPharmacie);

	        return Response.ok("{\"message\": \"Le statut de la pharmacie a été mis à jour avec succès et le pharmacien a été notifié.\"}")
	                       .build();
	    } catch (IllegalArgumentException e) {
	        return Response.status(Response.Status.NOT_FOUND)
	                       .entity("{\"error\": \"" + e.getMessage() + "\"}")
	                       .build();
	    } catch (Exception e) {
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	                       .entity("{\"error\": \"Une erreur s'est produite lors de la mise à jour de la commande.\"}")
	                       .build();
	    }
	}

	    @GET
	    @Path("/consulterPharmacies")
	    @Produces(MediaType.APPLICATION_JSON)
	    public List<Pharmacien> consulterPharmacies() {
	        return adminImpl.consulterPharmacies();
	    }
	    @GET
	    @Path("/consulterPatients")
	    @Produces(MediaType.APPLICATION_JSON)
	    public List<Patient> consulterPatients() {
	        return adminImpl.consulterPatients();
	    }

	    @GET
	    @Path("/consulterUtilisateurs")
	    @Produces(MediaType.APPLICATION_JSON)
	    public List<User> consulterUtilisateurs() {
	        return adminImpl.consulterUtilisateurs();
	    }
  
	
	    
	    @GET
	    @Path("/consulterPharmaciesByStatus/{status}")
	    @Produces(MediaType.APPLICATION_JSON)
	    public List<Pharmacien> consulterPharmaciesByStatus(@PathParam("status") Statut_Pharmacie status) {
	        return adminImpl.consulterPharmacies(status);
	    }
	    @DELETE
	    @Path("/delete/{id}")
	    @Consumes(MediaType.APPLICATION_JSON) 
	    @Produces(MediaType.APPLICATION_JSON)
	    public Response deleteUser(@PathParam("id") int id) {
	        boolean success = adminImpl.deleteUser(id);  
	        if (success) {
	            return Response.status(Response.Status.NO_CONTENT).build();
	        } else {
	            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
	        }
	    }

	    @GET
	    @Path("/{id}/notifications/last-ten")
		@Consumes(MediaType.APPLICATION_JSON) 
	  @Produces(MediaType.APPLICATION_JSON)
	    public Response getLastTenNotifications(@PathParam("id") int idAdmin) {
	        List<Notification> notifications = adminImpl.recevoirNotificationLastTen(idAdmin);
	        if (notifications.isEmpty()) {
	            return Response.status(Response.Status.NO_CONTENT).build();
	        }
	        return Response.ok(notifications).build();
	    }
	    @GET
		  @Path("/{id}/notifications/unseen")
		  @Produces(MediaType.APPLICATION_JSON)
		  public Response getUnseenNotifications(@PathParam("id") int idAdmin) {
		      int notifications = adminImpl.countUnseenNotifications(idAdmin); 
		      return Response.ok()
		              .entity("{\"size\": " + notifications + "}")
		              .build();
		  }
		  @POST
		  @Path("/{id}/notifications/mark-as-seen")
		  @Produces(MediaType.APPLICATION_JSON)
		  public Response markNotificationsAsSeen(@PathParam("id") int idAdmin) {
		      int updatedCount = adminImpl.markNotificationsAsSeen(idAdmin); 
		      return Response.ok()
		              .entity("{\"updated\": " + updatedCount + "}")
		              .build();
		  }
		  @POST
		  @Path("/login")
		  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		  @Produces(MediaType.APPLICATION_JSON)
		  public Response login(@FormParam("email") String email, @FormParam("password") String password) {
			  Admin adminData = adminImpl.seConnecter(email, password);
			  System.out.println("Email: " + email + ", Password: " + password);
	          System.out.println("Admin Data: " + adminData);

		      if (adminData != null) {
		         
		          String patientJson = "{"
		              + "\"success\": true, "
		              + "\"id\": " + adminData.getId() + ", "
		              + "\"username\": \"" + adminData.getUsername() + "\", "
		              + "\"email\": \"" + adminData.getEmail() + "\", "
		              + "\"telephone\": \"" + adminData.getTelephone() + "\""
		              + "}";
		          
		          return Response.ok()
		                  .entity(patientJson)
		                  .build();
		      } else {
		          return Response.status(Response.Status.UNAUTHORIZED)
		                  .entity("{\"success\": false, \"message\": \"Email ou mot de passe incorrect.\"}")
		                  .build();
		      }
		  }
		  
		  @PUT
		  @Path("/{id}")
		  @Consumes(MediaType.APPLICATION_JSON)
		  @Produces(MediaType.APPLICATION_JSON)
		  public Response updateAdmin(
		      @PathParam("id") int id,
		      Admin adminData
		  ) {
		      System.out.println("Received admin data: Nom=" + adminData.getUsername() + ", Email=" + adminData.getEmail() + ", Telephone=" + adminData.getTelephone());
		      
		      boolean updated = adminImpl.updateAdmin(id, adminData.getUsername(), adminData.getEmail(), adminData.getTelephone());

		      if (updated) {
		          return Response.ok().entity("{\"success\": true, \"message\": \"Profile updated successfully!\"}").build();
		      } else {
		          return Response.status(Response.Status.NOT_FOUND)
		                  .entity("{\"success\": false, \"message\": \"Admin not found.\"}")
		                  .build();
		      }
		  }
		  @POST
		  @Path("/createAdmin")
		  @Consumes(MediaType.APPLICATION_FORM_URLENCODED) 
		  @Produces(MediaType.APPLICATION_JSON)
		  public Response créerCompteAdmin(@FormParam("name") String name, 
				  @FormParam("email") String email,
				  @FormParam("password") String password,
				  @FormParam("telephone") String telephone) {
			  try {
		            adminImpl.créerAdmin(name, email, telephone, password);
		            return Response.status(Response.Status.CREATED) 
		                           .entity("{\"message\": \"Admin créé avec succès\"}")
		                           .build();
		        } catch (Exception e) {		           
		            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
		                           .entity("{\"error\": \"" + e.getMessage() + "\"}")
		                           .build();
		        }
		  }
		  
		  @POST
		    @Path("/envoyerMotDePasse")
		    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		    @Produces(MediaType.APPLICATION_JSON)
		    public Response envoyerMotDePasse(@FormParam("email") String email) {
		        try {
		        	adminImpl.mot_de_passe(email);
		            return Response.ok("{\"message\": \"Le mot de passe a été envoyé à votre adresse email.\"}").build();
		        } catch (IllegalArgumentException e) {
		            return Response.status(Response.Status.NOT_FOUND)
		                           .entity("{\"error\": \"" + e.getMessage() + "\"}")
		                           .build();
		        } catch (Exception e) {
		            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
		                           .entity("{\"error\": \"Une erreur s'est produite lors de l'envoi du mot de passe.\"}")
		                           .build();
		        }
		    }
}
