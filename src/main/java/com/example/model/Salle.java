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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

@Entity
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(nullable = false)
    private int capacite;

    @OneToMany(mappedBy = "salle")
    private Set<Reservation> reservations = new HashSet<>();

    // La salle porte la relation ; les équipements peuvent être partagés.
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "salle_equipement",
        joinColumns = @JoinColumn(name = "salle_id"),
        inverseJoinColumns = @JoinColumn(name = "equipement_id")
    )
    private Set<Equipement> equipements = new HashSet<>();

    public Salle() {
    }

    public Salle(String nom, int capacite) {
        this.nom = nom;
        this.capacite = capacite;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public Set<Reservation> getReservations() {
        return reservations;
    }

    public Set<Equipement> getEquipements() {
        return equipements;
    }

    public void ajouterReservation(Reservation reservation) {
        Objects.requireNonNull(reservation, "La réservation est obligatoire");
        reservation.setSalle(this);
    }

    public void retirerReservation(Reservation reservation) {
        Objects.requireNonNull(reservation, "La réservation est obligatoire");
        if (reservation.getSalle() == this) {
            reservation.setSalle(null);
        }
    }

    public void ajouterEquipement(Equipement equipement) {
        Objects.requireNonNull(equipement, "L'équipement est obligatoire");
        equipements.add(equipement);
        equipement.getSalles().add(this);
    }

    public void retirerEquipement(Equipement equipement) {
        Objects.requireNonNull(equipement, "L'équipement est obligatoire");
        equipements.remove(equipement);
        equipement.getSalles().remove(this);
    }

    @Override
    public String toString() {
        return "Salle{id=" + id + ", nom='" + nom + "', capacite=" + capacite + "}";
    }
}
