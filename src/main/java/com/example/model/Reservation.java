package com.example.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String objet;

    @Column(nullable = false)
    private LocalDateTime dateReservation;

    // La réservation porte les clés étrangères des deux relations.
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    public Reservation() {
    }

    public Reservation(String objet, LocalDateTime dateReservation,
                       Utilisateur utilisateur, Salle salle) {
        this.objet = objet;
        this.dateReservation = dateReservation;
        setUtilisateur(utilisateur);
        setSalle(salle);
    }

    public Long getId() {
        return id;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    // Les setters maintiennent aussi le côté inverse, y compris lors d'un changement.
    public void setUtilisateur(Utilisateur utilisateur) {
        if (this.utilisateur != null) {
            this.utilisateur.getReservations().remove(this);
        }
        this.utilisateur = utilisateur;
        if (utilisateur != null) {
            utilisateur.getReservations().add(this);
        }
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        if (this.salle != null) {
            this.salle.getReservations().remove(this);
        }
        this.salle = salle;
        if (salle != null) {
            salle.getReservations().add(this);
        }
    }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", objet='" + objet
            + "', dateReservation=" + dateReservation + "}";
    }
}
