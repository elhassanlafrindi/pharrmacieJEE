package rest;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import Metier.PharmacieImpl;
import Metier.PharmacieLocal;
import Metier.Entities.Commande;
import Metier.Entities.Notification;
import Metier.Entities.Patient;
import Metier.Entities.Pharmacien;
import Metier.Entities.Statut_Commande;

@Path("/pharmacien")
public class PharmacienRestService {
	  @EJB 
	  private PharmacieLocal pharmacie;

	public PharmacienRestService() {
		super();
		this.pharmacie=new PharmacieImpl();
		
	}

	@POST
	@Path("/commande/{idCommande}/updateStatut")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response mettreAJourStatutCommande(
	        @PathParam("idCommande") int idCommande,
	        @FormParam("statut") String statut,
	        @FormParam("montant") double montant) {

	    try {
	        Statut_Commande statutCommande;
	        try {
	        	 statutCommande = Statut_Commande.fromString(statut);
	        } catch (IllegalArgumentException e) {
	        	return Response.status(Response.Status.BAD_REQUEST)
	                    .entity("{\"error\": \"Statut invalide. Les valeurs possibles sont: [en_attente, Reçu, En_Préparation, Prête_à_Récupérée].\"}")
	                    .build();
	        }

	      
	        pharmacie.mettreAJourStatutCommandeEtnotifierPatient(idCommande, statutCommande, montant);

	        return Response.ok("{\"message\": \"Le statut de la commande a été mis à jour avec succès et le patient a été notifié.\"}")
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
	@Path("/{idPharmacie}/Commandes/{statut}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response consulterCommandesParStatut(
	        @PathParam("idPharmacie") int idPharmacie,
	        @PathParam("statut") String statut) {
	    try {
	        Statut_Commande statutCommande;
	        try {
	            statutCommande = Statut_Commande.valueOf(statut.toUpperCase());
	        } catch (IllegalArgumentException e) {
	            return Response.status(Response.Status.BAD_REQUEST)
	                           .entity("Statut invalide. Veuillez fournir un statut valide.")
	                           .build();
	        }

	        List<Commande> commandes = pharmacie.consulterCommande(idPharmacie, statutCommande);

	        if (commandes != null && !commandes.isEmpty()) {
	            List<Map<String, Object>> response = new ArrayList<>();
	            for (Commande commande : commandes) {
	                Map<String, Object> commandeMap = new HashMap<>();
	                commandeMap.put("idCommande", commande.getIdCommande());
	                commandeMap.put("nomPatient", commande.getPatient().getUsername());
                    commandeMap.put("telePatient", commande.getPatient().getTelephone());

	                commandeMap.put("email", commande.getPatient().getEmail());
	                commandeMap.put("statut", commande.getStatut());
	                commandeMap.put("montant", commande.getMountant());
	                if (commande.getImage() != null) {
	                    commandeMap.put("image", encodeImageToBase64(commande.getImage()));
	                } else {
	                    commandeMap.put("image", null);
	                }
	                response.add(commandeMap);
	            }
	            return Response.ok(response).build();
	        } else {
	            return Response.status(Response.Status.NO_CONTENT)
	                           .entity("Aucune commande trouvée pour le statut spécifié.")
	                           .build();
	        }
	    } catch (Exception e) {
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	                       .entity("Erreur lors de la récupération des commandes : " + e.getMessage())
	                       .build();
	    }
	}

	
    @GET
    @Path("/{idPharmacie}/AllCommande")
    @Produces(MediaType.APPLICATION_JSON)
    public Response consulterAllCommande(@PathParam("idPharmacie") int idPharmacie) {
        try {
            List<Commande> commandes = pharmacie.consulterCommandes(idPharmacie);
            if (commandes != null && !commandes.isEmpty()) {
                List<Map<String, Object>> response = new ArrayList<>();
                for (Commande commande : commandes) {
                    Map<String, Object> commandeMap = new HashMap<>();
                    commandeMap.put("idCommande", commande.getIdCommande());
                    
                    commandeMap.put("nomPatient", commande.getPatient().getUsername());
                    commandeMap.put("telePatient", commande.getPatient().getTelephone());

                    commandeMap.put("email", commande.getPatient().getEmail());
                    commandeMap.put("statut", commande.getStatut());
                    commandeMap.put("mountant", commande.getMountant());
                    if (commande.getImage() != null) {
                        commandeMap.put("image", encodeImageToBase64(commande.getImage()));
                    } else {
                        commandeMap.put("image", null);
                    }
                    response.add(commandeMap);
                }
                return Response.ok(response).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT)
                               .entity("Aucune commande trouvée.")
                               .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Erreur lors de la récupération des commandes : " + e.getMessage())
                           .build();
        }
    }

    @DELETE
	  @Path("/commande/delete/{id}")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response deleteCommande(@PathParam("id") int id) {
	      try {
	          pharmacie.deleteCommande(id); 
	          return Response.ok("{\"message\": \"Commande supprimée avec succès.\"}").build();
	      } catch (IllegalArgumentException e) {
	          return Response.status(Response.Status.NOT_FOUND)
	                         .entity("{\"error\": \"" + e.getMessage() + "\"}")
	                         .build();
	      } catch (Exception e) {
	          return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	                         .entity("{\"error\": \"Une erreur est survenue lors de la suppression de la commande.\"}")
	                         .build();
	      }
	  }
	
	 @PUT
	    @Path("/{id}/account")
	 @Consumes(MediaType.APPLICATION_JSON) 
	 @Produces(MediaType.APPLICATION_JSON) 
	    public Response updatePatientAccount(
	        @PathParam("id") int id, 
	        @QueryParam("name") String name,
	        @QueryParam("email") String email,
	        @QueryParam("password") String password,@QueryParam("telephone") String telephone,
	        @QueryParam("nomPharmacie")String nomPharmacie, @QueryParam("latitude") double latitude,
	        @QueryParam("longitude") double longitude
	    		) {
	        pharmacie.updatePharmacie(id, name, email, telephone, nomPharmacie, latitude, longitude);
	        return Response.status(Response.Status.OK).build();
	    }
	 @PUT
	  @Path("/{id}")
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response updatePharmacien(
	      @PathParam("id") int id,
	      Pharmacien pharmacienData
	  ) {
	      System.out.println("Received pharmacien data: Nom=" + pharmacienData.getUsername() + ", Email=" + pharmacienData.getEmail() + ", Telephone=" + pharmacienData.getTelephone()+", nomPharmacie="+pharmacienData.getNomPharmacie()+", latitude="+pharmacienData.getLatitude()+", longitude="+pharmacienData.getLongitude());
	      
	      boolean updated = pharmacie.updatePharmacien(id, pharmacienData.getUsername(), pharmacienData.getEmail(), pharmacienData.getTelephone(),pharmacienData.getNomPharmacie(),pharmacienData.getLatitude(),pharmacienData.getLongitude());

	      if (updated) {
	          return Response.ok().entity("{\"success\": true, \"message\": \"Profile updated successfully!\"}").build();
	      } else {
	          return Response.status(Response.Status.NOT_FOUND)
	                  .entity("{\"success\": false, \"message\": \"pharmacien not found.\"}")
	                  .build();
	      }
	  }
	 @POST
	  @Path("/createAccount")
	  @Consumes(MediaType.APPLICATION_FORM_URLENCODED) 
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response créerComptePharmacie(@FormParam("name") String name, 
			  @FormParam("email") String email,
			  @FormParam("password") String password,
			  @FormParam("telephone") String telephone
			  ) {
		 pharmacie.créerComptePharmacie(name, email, password, telephone);
		  return Response.status(Response.Status.OK).build();
	  }
	 
	 @POST
	 @Path("/login")
	 @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	 @Produces(MediaType.APPLICATION_JSON)
	 public Response login(@FormParam("email") String email, @FormParam("password") String password) {
	     Pharmacien pharmacienData = pharmacie.seConnecter(email, password);

	     if (pharmacienData != null) {
	         String pharmacienJson = "{"
	             + "\"success\": true, "
	             + "\"id\": " + pharmacienData.getId() + ", "
	             + "\"username\": \"" + pharmacienData.getUsername() + "\", "
	             + "\"email\": \"" + pharmacienData.getEmail() + "\", "
	             + "\"telephone\": \"" + pharmacienData.getTelephone() + "\", "
	             + "\"nomPharmacie\": \"" + pharmacienData.getNomPharmacie() + "\", "
	             + "\"latitude\": " + pharmacienData.getLatitude() + ", "
	             + "\"longitude\": " + pharmacienData.getLongitude()
	             + "}";

	         return Response.ok()
	                 .entity(pharmacienJson)
	                 .build();
	     } else {
	         return Response.status(Response.Status.UNAUTHORIZED)
	                 .entity("{\"success\": false, \"message\": \"Email ou mot de passe incorrect.\"}")
	                 .build();
	     }
	 }

	 @GET
	    @Path("/{id}/notifications/last-ten")
	  @Produces(MediaType.APPLICATION_JSON)
	    public Response getLastTenNotifications(@PathParam("id") int idPharmacie) {
	        List<Notification> notifications = pharmacie.recevoirNotificationLastTen(idPharmacie);
	        if (notifications.isEmpty()) {
	            return Response.status(Response.Status.NO_CONTENT).build();
	        }
	        return Response.ok(notifications).build();
	    }
	 
	 
	  @DELETE
	  @Path("/notifications/delete/{id}")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response deletNotification(@PathParam("id") int id) {
	      try {
	    	  pharmacie.deleteNotefication(id);
	          return Response.ok().entity("{\"message\": \"Notification deleted successfully.\"}").build();
	      } catch (IllegalArgumentException e) {
	          return Response.status(Response.Status.NOT_FOUND).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
	      } catch (Exception e) {
	          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"An error occurred while deleting the notification.\"}").build();
	      }
	  }
	 
	 
	 @GET
	  @Path("/{id}/notifications/unseen")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response getUnseenNotifications(@PathParam("id") int idPharmacie) {
	      int notifications = pharmacie.countUnseenNotifications(idPharmacie); 
	      return Response.ok()
	              .entity("{\"size\": " + notifications + "}")
	              .build();
	  }
	 @POST
	  @Path("/{id}/notifications/mark-as-seen")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response markNotificationsAsSeen(@PathParam("id") int idPharmacie) {
	      int updatedCount = pharmacie.markNotificationsAsSeen(idPharmacie); 
	      return Response.ok()
	              .entity("{\"updated\": " + updatedCount + "}")
	              .build();
	  }
	 public String encodeImageToBase64(byte[] imageBytes) {
		    return Base64.getEncoder().encodeToString(imageBytes);
		}
}
