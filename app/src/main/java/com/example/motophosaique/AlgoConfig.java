package com.example.motophosaique;

public class AlgoConfig {
    public static boolean hasUserSelected = false;

    /** La clé de l’algorithme choisi (“average”, “histo”, “distribution”, etc.) */
    public static String selectedAlgo = "average";

    /** true si on est en mode couleur, false si mode gris */
    public static boolean isColor = false;

    /** Avec répétition ou pas (pour l’algo distribution) */
    public static boolean withRep = false;
}
