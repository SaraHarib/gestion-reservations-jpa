package com.example.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nom;

    // Une réservation retirée de l'utilisateur sera supprimée en base.
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reservation> reservations = new HashSet<>();

    public Utilisateur() {
    }

    public Utilisateur(String email, String nom) {
        this.email = email;
        this.nom = nom;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Set<Reservation> getReservations() {
        return reservations;
    }

    public void ajouterReservation(Reservation reservation) {
        Objects.requireNonNull(reservation, "La réservation est obligatoire");
        reservation.setUtilisateur(this);
    }

    public void retirerReservation(Reservation reservation) {
        Objects.requireNonNull(reservation, "La réservation est obligatoire");
        if (reservation.getUtilisateur() == this) {
            reservation.setUtilisateur(null);
        }
    }

    @Override
    public String toString() {
        return "Utilisateur{id=" + id + ", email='" + email + "', nom='" + nom + "'}";
    }
}
