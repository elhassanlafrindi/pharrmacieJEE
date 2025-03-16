
package rest;

import java.io.InputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
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

import Metier.PatientImpl;
import Metier.PatientLocal;
import Metier.Entities.Commande;
import Metier.Entities.Notification;
import Metier.Entities.Patient;
import Metier.Entities.Pharmacien;
import Metier.Entities.Statut_Commande;
import Metier.Entities.Statut_Pharmacie;

@Path("/patient")
public class PatientRestservice {
    @EJB
    private PatientLocal patient;

    public PatientRestservice() {
		super();
		this.patient=new PatientImpl();
		// TODO Auto-generated constructor stub
	}

    @GET
    @Path("/{idPatient}/historiqueCommande")
    @Produces(MediaType.APPLICATION_JSON)
    public Response consulterHistoriqueCommande(@PathParam("idPatient") int idPatient) {
        try {
            List<Commande> commandes = patient.consulterHistoriqueCommande(idPatient);

            if (commandes != null && !commandes.isEmpty()) {
                List<Map<String, Object>> response = new ArrayList<>();
                for (Commande commande : commandes) {
                    Map<String, Object> commandeMap = new HashMap<>();
                    commandeMap.put("idCommande", commande.getIdCommande());
                    
                    commandeMap.put("pharmacienNom", commande.getPharmacien().getNomPharmacie());
                    commandeMap.put("statut", commande.getStatut());
                    
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
                               .entity("Aucune commande trouvée pour ce patient.")
                               .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Erreur lors de la récupération des commandes : " + e.getMessage())
                           .build();
        }
    }
	
	//-----------------------
	public String encodeImageToBase64(byte[] imageBytes) {
	    return Base64.getEncoder().encodeToString(imageBytes);
	}
//-------------------------------
    @GET
    @Path("/{idPatient}/historiqueCommande/{idPharmacie}/statut")
    @Produces(MediaType.APPLICATION_JSON)
    public Response suivreEtatCommande(@PathParam("idPatient") int idPatient, @PathParam("idPharmacie") int idPharmacie) {
        try {
            String statut = patient.suivreEtatCommande(idPharmacie, idPatient);

            if (statut != null && !statut.isEmpty()) {
                return Response.ok(statut).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT)
                               .entity("No status found for this order.")
                               .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error retrieving order status: " + e.getMessage())
                           .build();
        }
    }

    @POST
    @Path("/{idPatient}/commande")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response créerCommande(@PathParam("idPatient") int idPatient, MultipartFormDataInput input) {
        try {
            Map<String, List<InputPart>> formParts = input.getFormDataMap();
            
            InputPart imagePart = formParts.get("image").get(0); 
            InputStream uploadedInputStream = imagePart.getBody(InputStream.class, null);
            
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = uploadedInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            
            String statutString = formParts.get("statut").get(0).getBodyAsString();
            
            Statut_Commande statutEnum = Statut_Commande.valueOf(statutString);
            
            String idPharmacieString = formParts.get("idPharmacie").get(0).getBodyAsString();
            int idPharmacie = Integer.parseInt(idPharmacieString);
            patient.créerCommande(idPatient, imageBytes, statutEnum,idPharmacie);

            return Response.status(Response.Status.CREATED)
                           .entity("Commande créée avec succès.")
                           .build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Erreur lors du traitement de l'image : " + e.getMessage())
                           .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Statut invalide : " + e.getMessage())
                           .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Erreur lors de la création de la commande : " + e.getMessage())
                           .build();
        }
}
  
    
    @GET
    @Path("/pharmacies/map")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPharmaciesMap() {
        List<Pharmacien> pharmacies = patient.consulterCartePharmacies();
        if (pharmacies.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        List<Map<String, Object>> pharmaciesData = pharmacies.stream().map(pharmacy -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", pharmacy.getId());
            data.put("pharmacyName", pharmacy.getNomPharmacie());
            data.put("latitude", pharmacy.getLatitude());
            data.put("longitude", pharmacy.getLongitude());
            data.put("tele", pharmacy.getTelephone());
            data.put("email", pharmacy.getEmail());
            data.put("statut", pharmacy.getStatutPharmacie());

            return data;
        }).collect(Collectors.toList());
        return Response.ok(pharmaciesData).build();
    }

    @GET
    @Path("/pharmacies/map/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPharmaciesMap(
        @QueryParam("name") String name, 
        @QueryParam("status") Statut_Pharmacie status) {

        List<Pharmacien> pharmacies = patient.consulterCartePharmacies(name, status);

        if (pharmacies.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build(); 
        }

        List<Map<String, Object>> pharmaciesData = pharmacies.stream().map(pharmacy -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", pharmacy.getId());
            data.put("pharmacyName", pharmacy.getNomPharmacie());
            data.put("latitude", pharmacy.getLatitude());
            data.put("longitude", pharmacy.getLongitude());
            data.put("tele", pharmacy.getTelephone());
            data.put("email", pharmacy.getEmail());
            data.put("statut", pharmacy.getStatutPharmacie());
            return data;
        }).collect(Collectors.toList());

        return Response.ok(pharmaciesData).build();
    }

	 @PUT
	    @Path("/{id}/account")
	 @Consumes(MediaType.APPLICATION_JSON) 
	 @Produces(MediaType.APPLICATION_JSON) 
	    public Response updatePatientAccount(
	        @PathParam("id") int id, 
	        @QueryParam("name") String name,
	        @QueryParam("email") String email,
	        @QueryParam("password") String password,@QueryParam("telephone") String telephone) {
	        patient.mettreAJourCompte(id, name, email, password,telephone);
	        return Response.status(Response.Status.OK).build();
	    }
	 /* @GET
	    @Path("/{id}/notifications/unseen")
	  @Produces(MediaType.APPLICATION_JSON)
	    public Response getUnseenNotifications(@PathParam("id") int patientId) {
		  int notifications = patient.recevoirNotificationNeverSeen(patientId);
	        
	        return Response.ok()
	                  .entity("{\"size\": " + notifications + "}")
	                  .build();
	    }*/
	  @GET
	    @Path("/{id}/notifications/last-ten")
	  @Produces(MediaType.APPLICATION_JSON)
	    public Response getLastTenNotifications(@PathParam("id") int patientId) {
	        List<Notification> notifications = patient.recevoirNotificationLastTen(patientId);
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
	          patient.deleteNotefication(id); 
	          return Response.ok().entity("{\"message\": \"Notification deleted successfully.\"}").build();
	      } catch (IllegalArgumentException e) {
	          return Response.status(Response.Status.NOT_FOUND).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
	      } catch (Exception e) {
	          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"An error occurred while deleting the notification.\"}").build();
	      }
	  }

	  @DELETE
	  @Path("/commande/delete/{id}")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response deleteCommande(@PathParam("id") int id) {
	      try {
	          patient.deleteCommande(id); 
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
	  
	  
	  @POST
	  @Path("/createAccount")
	  @Consumes(MediaType.APPLICATION_FORM_URLENCODED) 
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response créerComptePatient(@FormParam("name") String name, 
			  @FormParam("email") String email,
			  @FormParam("password") String password,
			  @FormParam("telephone") String telephone) {
		  patient.créerComptePatient(name, email, password, telephone);
		  return Response.status(Response.Status.OK).build();
	  }
	  
	 /* @POST
	  @Path("/login")
	  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response login(@FormParam("email") String email, @FormParam("password") String password) {
	      int isAuthenticated = patient.seConnecter(email, password);


	      if (isAuthenticated > 0) {
	          return Response.ok()
	                  .entity("{\"success\": true, \"id\": " + isAuthenticated + "}")
	                  .build();
	      } else {
	          return Response.status(Response.Status.UNAUTHORIZED)
	                  .entity("{\"success\": false, \"message\": \"Email ou mot de passe incorrect.\"}")
	                  .build();
	      }
	  }*/
	  @POST
	  @Path("/login")
	  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response login(@FormParam("email") String email, @FormParam("password") String password) {
		  Patient patientData = patient.seConnecter(email, password);

	      if (patient != null) {
	          String patientJson = "{"
	              + "\"success\": true, "
	              + "\"id\": " + patientData.getId() + ", "
	              + "\"username\": \"" + patientData.getUsername() + "\", "
	              + "\"email\": \"" + patientData.getEmail() + "\", "
	              + "\"telephone\": \"" + patientData.getTelephone() + "\""
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

	  
	  
	  @GET
	  @Path("/{id}/notifications/unseen")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response getUnseenNotifications(@PathParam("id") int patientId) {
	      int notifications = patient.countUnseenNotifications(patientId); 
	      return Response.ok()
	              .entity("{\"size\": " + notifications + "}")
	              .build();
	  }
	  
	  @POST
	  @Path("/{id}/notifications/mark-as-seen")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response markNotificationsAsSeen(@PathParam("id") int patientId) {
	      int updatedCount = patient.markNotificationsAsSeen(patientId); 
	      return Response.ok()
	              .entity("{\"updated\": " + updatedCount + "}")
	              .build();
	  }
	  @PUT
	  @Path("/{id}")
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response updatePatient(
	      @PathParam("id") int id,
	      Patient patientData
	  ) {
	      System.out.println("Received patient data: Nom=" + patientData.getUsername() + ", Email=" + patientData.getEmail() + ", Telephone=" + patientData.getTelephone());
	      
	      boolean updated = patient.updatePatient(id, patientData.getUsername(), patientData.getEmail(), patientData.getTelephone());

	      if (updated) {
	          return Response.ok().entity("{\"success\": true, \"message\": \"Profile updated successfully!\"}").build();
	      } else {
	          return Response.status(Response.Status.NOT_FOUND)
	                  .entity("{\"success\": false, \"message\": \"Patient not found.\"}")
	                  .build();
	      }
	  }


	

	    
	  @PUT
	  @Path("/{id}/resetPassword")
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response resetPassword(@PathParam("id") int id, Map<String, String> request) {
	      try {
	          String newPassword = request.get("newPassword");

	          if (newPassword == null) {
	              return Response.status(Response.Status.BAD_REQUEST)
	                      .entity("newPassword is required.")
	                      .build();
	          }

	          boolean isReset = patient.resetPassword(id, newPassword);
	          if (isReset) {
	        	  return Response.ok("{\"message\": \"Password reset successfully.\"}").build();
	          } else {
	              return Response.status(Response.Status.BAD_REQUEST)
	                      .entity("Failed to reset password.")
	                      .build();
	          }
	      } catch (IllegalArgumentException e) {
	          return Response.status(Response.Status.BAD_REQUEST)
	                  .entity(e.getMessage())
	                  .build();
	      } catch (Exception e) {
	          return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	                  .entity("Error occurred while resetting password.")
	                  .build();
	      }
	  }


	  @POST
	  @Path("/test")
	  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response test(@FormParam("email") String email, @FormParam("password") String password) {
	      return Response.status(Response.Status.OK).entity("Received email: " + email + ", password: " + password).build();
	  }
}










