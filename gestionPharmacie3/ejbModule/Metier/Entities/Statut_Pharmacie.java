package Metier.Entities;

public enum Statut_Pharmacie {
	Actif,
	Non_Actif;
	

    @Override
    public String toString() {
        return this.name();
    }

    public static Statut_Pharmacie fromString(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Statut cannot be null.");
        }

        String sanitizedInput = input.trim().replace("_", " ").toLowerCase();

        for (Statut_Pharmacie statut : Statut_Pharmacie.values()) {
            if (statut.name().replace("_", " ").toLowerCase().equals(sanitizedInput)) {
                return statut;
            }
        }

        throw new IllegalArgumentException("Invalid statut: " + input);
    }
}
