package com.example.model;

import java.time.LocalDateTime;
import junit.framework.TestCase;

public class RelationsTest extends TestCase {

    public void testConstructeurReservationSynchroniseLesRelations() {
        Utilisateur utilisateur = new Utilisateur("sarah@example.com", "Sarah");
        Salle salle = new Salle("Salle de réunion", 12);

        Reservation reservation = new Reservation("Réunion de projet",
            LocalDateTime.of(2026, 10, 15, 10, 0), utilisateur, salle);

        assertSame(utilisateur, reservation.getUtilisateur());
        assertTrue(utilisateur.getReservations().contains(reservation));
        assertSame(salle, reservation.getSalle());
        assertTrue(salle.getReservations().contains(reservation));
    }

    public void testAjoutChangementEtRetraitUtilisateur() {
        Utilisateur premierUtilisateur = new Utilisateur("sarah@example.com", "Sarah");
        Utilisateur secondUtilisateur = new Utilisateur("amine@example.com", "Amine");
        Reservation reservation = new Reservation();

        premierUtilisateur.ajouterReservation(reservation);
        premierUtilisateur.ajouterReservation(reservation);
        assertEquals(1, premierUtilisateur.getReservations().size());
        assertSame(premierUtilisateur, reservation.getUtilisateur());

        secondUtilisateur.ajouterReservation(reservation);
        assertTrue(premierUtilisateur.getReservations().isEmpty());
        assertTrue(secondUtilisateur.getReservations().contains(reservation));
        assertSame(secondUtilisateur, reservation.getUtilisateur());

        premierUtilisateur.retirerReservation(reservation);
        assertSame(secondUtilisateur, reservation.getUtilisateur());

        secondUtilisateur.retirerReservation(reservation);
        secondUtilisateur.retirerReservation(reservation);
        assertNull(reservation.getUtilisateur());
        assertTrue(secondUtilisateur.getReservations().isEmpty());
    }

    public void testAjoutChangementEtRetraitSalle() {
        Salle premiereSalle = new Salle("Salle A", 12);
        Salle secondeSalle = new Salle("Salle B", 20);
        Reservation reservation = new Reservation();

        premiereSalle.ajouterReservation(reservation);
        premiereSalle.ajouterReservation(reservation);
        assertEquals(1, premiereSalle.getReservations().size());
        assertSame(premiereSalle, reservation.getSalle());

        secondeSalle.ajouterReservation(reservation);
        assertTrue(premiereSalle.getReservations().isEmpty());
        assertTrue(secondeSalle.getReservations().contains(reservation));
        assertSame(secondeSalle, reservation.getSalle());

        premiereSalle.retirerReservation(reservation);
        assertSame(secondeSalle, reservation.getSalle());

        secondeSalle.retirerReservation(reservation);
        secondeSalle.retirerReservation(reservation);
        assertNull(reservation.getSalle());
        assertTrue(secondeSalle.getReservations().isEmpty());
    }

    public void testSettersReservationSynchronisentLesDeuxParents() {
        Utilisateur premierUtilisateur = new Utilisateur("sarah@example.com", "Sarah");
        Utilisateur secondUtilisateur = new Utilisateur("amine@example.com", "Amine");
        Salle premiereSalle = new Salle("Salle A", 12);
        Salle secondeSalle = new Salle("Salle B", 20);
        Reservation reservation = new Reservation("Réunion",
            LocalDateTime.of(2026, 10, 15, 10, 0), premierUtilisateur, premiereSalle);

        reservation.setUtilisateur(secondUtilisateur);
        reservation.setSalle(secondeSalle);
        assertTrue(premierUtilisateur.getReservations().isEmpty());
        assertTrue(premiereSalle.getReservations().isEmpty());
        assertTrue(secondUtilisateur.getReservations().contains(reservation));
        assertTrue(secondeSalle.getReservations().contains(reservation));

        reservation.setUtilisateur(null);
        reservation.setSalle(null);
        assertTrue(secondUtilisateur.getReservations().isEmpty());
        assertTrue(secondeSalle.getReservations().isEmpty());
    }

    public void testEquipementPartageEtSynchronisationDepuisLesDeuxCotes() {
        Salle premiereSalle = new Salle("Salle A", 12);
        Salle secondeSalle = new Salle("Salle B", 20);
        Equipement equipement = new Equipement("Vidéoprojecteur");

        premiereSalle.ajouterEquipement(equipement);
        premiereSalle.ajouterEquipement(equipement);
        equipement.ajouterSalle(secondeSalle);
        equipement.ajouterSalle(secondeSalle);
        assertEquals(1, premiereSalle.getEquipements().size());
        assertEquals(1, secondeSalle.getEquipements().size());
        assertEquals(2, equipement.getSalles().size());
        assertTrue(equipement.getSalles().contains(premiereSalle));
        assertTrue(equipement.getSalles().contains(secondeSalle));

        premiereSalle.retirerEquipement(equipement);
        premiereSalle.retirerEquipement(equipement);
        assertTrue(premiereSalle.getEquipements().isEmpty());
        assertFalse(equipement.getSalles().contains(premiereSalle));
        assertTrue(secondeSalle.getEquipements().contains(equipement));
        assertTrue(equipement.getSalles().contains(secondeSalle));

        equipement.retirerSalle(secondeSalle);
        equipement.retirerSalle(secondeSalle);
        assertTrue(secondeSalle.getEquipements().isEmpty());
        assertTrue(equipement.getSalles().isEmpty());
    }
}
