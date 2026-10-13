package com.example.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class Equipement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    // Côté inverse de la relation définie dans Salle.
    @ManyToMany(mappedBy = "equipements")
    private Set<Salle> salles = new HashSet<>();

    public Equipement() {
    }

    public Equipement(String nom) {
        this.nom = nom;
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

    public Set<Salle> getSalles() {
        return salles;
    }

    public void ajouterSalle(Salle salle) {
        Objects.requireNonNull(salle, "La salle est obligatoire");
        salle.ajouterEquipement(this);
    }

    public void retirerSalle(Salle salle) {
        Objects.requireNonNull(salle, "La salle est obligatoire");
        salle.retirerEquipement(this);
    }

    @Override
    public String toString() {
        return "Equipement{id=" + id + ", nom='" + nom + "'}";
    }
}
