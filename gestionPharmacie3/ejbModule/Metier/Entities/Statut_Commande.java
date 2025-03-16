package Metier.Entities;

public enum Statut_Commande {
    en_attente,
    Recu,                  
    En_Preparation,         
    Prete_a_Recuperee;      

    @Override
    public String toString() {
        return this.name();
    }

    public static Statut_Commande fromString(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Statut cannot be null.");
        }

        // Nettoyer la chaîne d'entrée pour ignorer majuscules et underscores
        String sanitizedInput = input.trim().replace("_", " ").toLowerCase();

        // Comparaison avec les valeurs de l'énumération
        for (Statut_Commande statut : Statut_Commande.values()) {
            if (statut.name().replace("_", " ").toLowerCase().equals(sanitizedInput)) {
                return statut;
            }
        }

        throw new IllegalArgumentException("Invalid statut: " + input);
    }
}
