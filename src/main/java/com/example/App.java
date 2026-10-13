package com.example;

import com.example.model.Equipement;
import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class App {

    private static final DateTimeFormatter FORMAT_DATE =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        EntityManagerFactory fabriqueEntityManager = null;
        EntityManager entityManager = null;
        EntityTransaction transactionLocale = null;

        try {
            fabriqueEntityManager =
                Persistence.createEntityManagerFactory("gestion-reservations-pu");
            entityManager = fabriqueEntityManager.createEntityManager();
            transactionLocale = entityManager.getTransaction();
            executerDemonstration(entityManager, transactionLocale);
        } catch (RuntimeException exception) {
            if (transactionLocale != null && transactionLocale.isActive()) {
                try {
                    transactionLocale.rollback();
                } catch (RuntimeException erreurRollback) {
                    exception.addSuppressed(erreurRollback);
                }
            }
            System.err.println("Échec de la démonstration : " + exception.getMessage());
            throw exception;
        } finally {
            // Les ressources sont fermées même si la démonstration échoue.
            try {
                if (entityManager != null && entityManager.isOpen()) {
                    entityManager.close();
                }
            } finally {
                if (fabriqueEntityManager != null && fabriqueEntityManager.isOpen()) {
                    fabriqueEntityManager.close();
                }
            }
        }

        System.out.println("\nDémonstration terminée avec succès.");
    }

    private static void executerDemonstration(EntityManager entityManager,
                                             EntityTransaction transactionLocale) {
        afficherSection("1. CRÉATION DES SALLES");
        // Une transaction regroupe la création des données initiales.
        transactionLocale.begin();

        Salle salleAtlas = new Salle("Salle Atlas", 30);
        Salle salleMenara = new Salle("Salle Menara", 18);
        Salle salleMajorelle = new Salle("Salle Majorelle", 60);
        entityManager.persist(salleAtlas);
        entityManager.persist(salleMenara);
        entityManager.persist(salleMajorelle);
        System.out.println(salleAtlas);
        System.out.println(salleMenara);
        System.out.println(salleMajorelle);

        afficherSection("2. ASSOCIATION DES ÉQUIPEMENTS");
        Equipement videoprojecteur = new Equipement("Vidéoprojecteur");
        Equipement tableauInteractif = new Equipement("Tableau interactif");
        Equipement systemeAudio = new Equipement("Système audio");
        Equipement cameraVisioconference = new Equipement("Caméra visioconférence");

        salleAtlas.ajouterEquipement(videoprojecteur);
        salleAtlas.ajouterEquipement(tableauInteractif);
        salleMenara.ajouterEquipement(videoprojecteur);
        salleMenara.ajouterEquipement(cameraVisioconference);
        salleMajorelle.ajouterEquipement(systemeAudio);
        salleMajorelle.ajouterEquipement(tableauInteractif);

        // persist propage PERSIST aux nouveaux équipements des salles déjà gérées.
        entityManager.persist(salleAtlas);
        entityManager.persist(salleMenara);
        entityManager.persist(salleMajorelle);
        afficherEquipements(salleAtlas);
        afficherEquipements(salleMenara);
        afficherEquipements(salleMajorelle);

        afficherSection("3. CRÉATION DES UTILISATEURS");
        Utilisateur utilisateurSara =
            new Utilisateur("sara.elmansouri@example.com", "Sara El Mansouri");
        Utilisateur utilisateurYassine =
            new Utilisateur("yassine.amrani@example.com", "Yassine Amrani");
        Utilisateur utilisateurLina =
            new Utilisateur("lina.berrada@example.com", "Lina Berrada");
        afficherUtilisateur(utilisateurSara);
        afficherUtilisateur(utilisateurYassine);
        afficherUtilisateur(utilisateurLina);

        afficherSection("4. CRÉATION DES RÉSERVATIONS");
        Reservation reservationReunion = creerReservation("Réunion du projet de fin d'études",
            LocalDateTime.of(2026, 11, 16, 9, 0), utilisateurSara, salleAtlas);
        Reservation reservationAtelier = creerReservation("Atelier de conception UML",
            LocalDateTime.of(2026, 11, 17, 14, 0), utilisateurSara, salleMenara);
        Reservation reservationVisioconference = creerReservation("Échange avec les partenaires",
            LocalDateTime.of(2026, 11, 18, 10, 30), utilisateurYassine, salleMenara);
        Reservation reservationPresentation = creerReservation("Présentation des prototypes",
            LocalDateTime.of(2026, 11, 19, 15, 0), utilisateurLina, salleMajorelle);

        // CascadeType.ALL persiste les réservations ; leurs salles sont déjà gérées.
        entityManager.persist(utilisateurSara);
        entityManager.persist(utilisateurYassine);
        entityManager.persist(utilisateurLina);
        transactionLocale.commit();

        afficherReservation(reservationReunion);
        afficherReservation(reservationAtelier);
        afficherReservation(reservationVisioconference);
        afficherReservation(reservationPresentation);

        Long identifiantSalleAtlas = salleAtlas.getId();
        Long identifiantSalleMenara = salleMenara.getId();
        Long identifiantVideoprojecteur = videoprojecteur.getId();
        Long identifiantUtilisateurSara = utilisateurSara.getId();
        Long identifiantReservationReunion = reservationReunion.getId();

        afficherSection("5. VÉRIFICATION DES RELATIONS");
        entityManager.clear();
        transactionLocale.begin();
        List<Utilisateur> utilisateurs = entityManager.createQuery(
            "select utilisateur from Utilisateur utilisateur order by utilisateur.nom",
            Utilisateur.class).getResultList();
        for (Utilisateur utilisateur : utilisateurs) {
            System.out.println(utilisateur.getNom() + " : "
                + utilisateur.getReservations().size() + " réservation(s)");
            utilisateur.getReservations().stream()
                .sorted(Comparator.comparing(Reservation::getDateReservation))
                .forEach(App::afficherReservation);
            for (Reservation reservation : utilisateur.getReservations()) {
                verifier(reservation.getUtilisateur() == utilisateur
                    && reservation.getSalle().getReservations().contains(reservation),
                    "Relations bidirectionnelles de la réservation #" + reservation.getId());
            }
        }
        List<Salle> salles = entityManager.createQuery(
            "select salle from Salle salle order by salle.nom", Salle.class).getResultList();
        for (Salle salle : salles) {
            afficherEquipements(salle);
            for (Equipement equipement : salle.getEquipements()) {
                verifier(equipement.getSalles().contains(salle),
                    "Relation bidirectionnelle : " + salle.getNom() + " / " + equipement.getNom());
            }
        }
        transactionLocale.commit();

        afficherSection("6. TEST MANY-TO-MANY");
        transactionLocale.begin();
        salleAtlas = entityManager.find(Salle.class, identifiantSalleAtlas);
        salleMenara = entityManager.find(Salle.class, identifiantSalleMenara);
        videoprojecteur = entityManager.find(Equipement.class, identifiantVideoprojecteur);
        afficherSallesEquipement(videoprojecteur);
        verifier(salleAtlas.getEquipements().contains(videoprojecteur)
            && salleMenara.getEquipements().contains(videoprojecteur),
            "Le même Vidéoprojecteur est partagé entre Atlas et Menara.");
        System.out.println("Retrait du Vidéoprojecteur de Salle Atlas uniquement.");
        salleAtlas.retirerEquipement(videoprojecteur);
        transactionLocale.commit();

        entityManager.clear();
        transactionLocale.begin();
        salleAtlas = entityManager.find(Salle.class, identifiantSalleAtlas);
        salleMenara = entityManager.find(Salle.class, identifiantSalleMenara);
        videoprojecteur = entityManager.find(Equipement.class, identifiantVideoprojecteur);
        verifier(videoprojecteur != null, "Le Vidéoprojecteur existe toujours en base.");
        verifier(!salleAtlas.getEquipements().contains(videoprojecteur)
            && !videoprojecteur.getSalles().contains(salleAtlas),
            "L'association avec Salle Atlas a disparu en base.");
        verifier(salleMenara.getEquipements().contains(videoprojecteur)
            && videoprojecteur.getSalles().contains(salleMenara),
            "L'association avec Salle Menara est conservée en base.");
        afficherSallesEquipement(videoprojecteur);
        System.out.println("Sans cascade REMOVE, supprimer un lien ne supprime pas l'équipement."
            + "\nUne cascade REMOVE risquerait de supprimer un équipement partagé"
            + " lors de la suppression d'une salle.");
        transactionLocale.commit();

        afficherSection("7. TEST ORPHAN REMOVAL");
        transactionLocale.begin();
        utilisateurSara = entityManager.find(Utilisateur.class, identifiantUtilisateurSara);
        reservationReunion = entityManager.find(Reservation.class, identifiantReservationReunion);
        int nombreReservationsAvant = utilisateurSara.getReservations().size();
        System.out.println("Utilisateur : " + utilisateurSara.getNom());
        System.out.println("Nombre de réservations avant suppression : " + nombreReservationsAvant);
        verifier(nombreReservationsAvant >= 2, "Sara possède au moins deux réservations.");
        System.out.println("Réservation qui va être retirée :");
        afficherReservation(reservationReunion);
        // orphanRemoval supprime en base la réservation retirée de son utilisateur.
        utilisateurSara.retirerReservation(reservationReunion);
        transactionLocale.commit();

        entityManager.clear();
        transactionLocale.begin();
        utilisateurSara = entityManager.find(Utilisateur.class, identifiantUtilisateurSara);
        int nombreReservationsApres = utilisateurSara.getReservations().size();
        System.out.println("Nombre de réservations après suppression : " + nombreReservationsApres);
        verifier(nombreReservationsApres == nombreReservationsAvant - 1,
            "Sara possède une réservation de moins.");
        verifier(entityManager.find(Reservation.class, identifiantReservationReunion) == null,
            "EntityManager.find renvoie null : la réservation orpheline a été supprimée.");
        transactionLocale.commit();

        afficherSection("8. SUPPRESSION EN CASCADE D'UN UTILISATEUR");
        transactionLocale.begin();
        Salle salleDemonstration = entityManager.find(Salle.class, identifiantSalleMenara);
        Utilisateur utilisateurDemonstration =
            new Utilisateur("nabil.tazi@example.com", "Nabil Tazi (démonstration)");
        Reservation reservationDemonstration = creerReservation("Préparation de la soutenance",
            LocalDateTime.of(2026, 11, 20, 11, 0), utilisateurDemonstration, salleDemonstration);
        entityManager.persist(utilisateurDemonstration);
        transactionLocale.commit();
        Long identifiantUtilisateurDemonstration = utilisateurDemonstration.getId();
        Long identifiantReservationDemonstration = reservationDemonstration.getId();

        entityManager.clear();
        transactionLocale.begin();
        utilisateurDemonstration = entityManager.find(Utilisateur.class, identifiantUtilisateurDemonstration);
        reservationDemonstration = entityManager.find(Reservation.class, identifiantReservationDemonstration);
        verifier(utilisateurDemonstration != null && reservationDemonstration != null,
            "L'utilisateur de démonstration et sa réservation existent en base.");
        afficherUtilisateur(utilisateurDemonstration);
        afficherReservation(reservationDemonstration);
        System.out.println("Suppression de l'utilisateur de démonstration.");
        entityManager.remove(utilisateurDemonstration);
        transactionLocale.commit();

        entityManager.clear();
        transactionLocale.begin();
        verifier(entityManager.find(Utilisateur.class, identifiantUtilisateurDemonstration) == null,
            "L'utilisateur de démonstration a été supprimé.");
        verifier(entityManager.find(Reservation.class, identifiantReservationDemonstration) == null,
            "Sa réservation a également été supprimée grâce à CascadeType.ALL.");
        salleMenara = entityManager.find(Salle.class, identifiantSalleMenara);
        videoprojecteur = entityManager.find(Equipement.class, identifiantVideoprojecteur);
        verifier(salleMenara != null && videoprojecteur != null
            && salleMenara.getEquipements().contains(videoprojecteur),
            "Salle Menara, le Vidéoprojecteur et leur association sont conservés.");
        transactionLocale.commit();

        afficherSection("9. ÉTAT FINAL");
        transactionLocale.begin();
        long nombreSalles = entityManager.createQuery(
            "select count(salle) from Salle salle", Long.class).getSingleResult();
        long nombreEquipements = entityManager.createQuery(
            "select count(equipement) from Equipement equipement", Long.class).getSingleResult();
        long nombreUtilisateurs = entityManager.createQuery(
            "select count(utilisateur) from Utilisateur utilisateur", Long.class).getSingleResult();
        long nombreReservations = entityManager.createQuery(
            "select count(reservation) from Reservation reservation", Long.class).getSingleResult();
        transactionLocale.commit();

        System.out.println("Nombre de salles       : " + nombreSalles);
        System.out.println("Nombre d'équipements   : " + nombreEquipements);
        System.out.println("Nombre d'utilisateurs  : " + nombreUtilisateurs);
        System.out.println("Nombre de réservations : " + nombreReservations);
        verifier(nombreSalles == 3 && nombreEquipements == 4
            && nombreUtilisateurs == 3 && nombreReservations == 3,
            "L'état final correspond aux créations et suppressions attendues.");
    }

    private static Reservation creerReservation(String objet, LocalDateTime dateReservation,
                                                Utilisateur utilisateur, Salle salle) {
        Reservation reservation = new Reservation();
        reservation.setObjet(objet);
        reservation.setDateReservation(dateReservation);
        utilisateur.ajouterReservation(reservation);
        salle.ajouterReservation(reservation);
        return reservation;
    }

    private static void afficherSection(String titre) {
        System.out.println("\n========================================");
        System.out.println(titre);
        System.out.println("========================================");
    }

    private static void afficherUtilisateur(Utilisateur utilisateur) {
        System.out.println(utilisateur.getNom() + " <" + utilisateur.getEmail() + ">");
    }

    private static void afficherReservation(Reservation reservation) {
        System.out.println("  - Réservation #" + reservation.getId() + " : " + reservation.getObjet()
            + " | " + reservation.getDateReservation().format(FORMAT_DATE)
            + " | " + reservation.getUtilisateur().getNom()
            + " | " + reservation.getSalle().getNom());
    }

    private static void afficherEquipements(Salle salle) {
        String nomsEquipements = salle.getEquipements().stream()
            .map(Equipement::getNom).sorted().collect(Collectors.joining(", "));
        System.out.println(salle.getNom() + " -> " + nomsEquipements);
    }

    private static void afficherSallesEquipement(Equipement equipement) {
        String nomsSalles = equipement.getSalles().stream()
            .map(Salle::getNom).sorted().collect(Collectors.joining(", "));
        System.out.println(equipement.getNom() + " (#" + equipement.getId() + ") -> " + nomsSalles);
    }

    private static void verifier(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("Vérification échouée : " + message);
        }
        System.out.println("[OK] " + message);
    }
}
