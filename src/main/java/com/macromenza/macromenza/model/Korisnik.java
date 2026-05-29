package com.macromenza.macromenza.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "korisnik")
public class Korisnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supabase_uid", unique = true)
    private UUID supabaseUid;

    private String ime;
    private String prezime;
    private String email;
    private String lozinka_hash;

    public Korisnik() {
    }

    public Long getId() {
        return id;
    }

    public UUID getSupabaseUid() {
        return supabaseUid;
    }

    public String getIme() {
        return ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public String getEmail() {
        return email;
    }

    public String getLozinka_hash() {
        return lozinka_hash;
    }

    public void setSupabaseUid(UUID supabaseUid) {
        this.supabaseUid = supabaseUid;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLozinka_hash(String lozinka_hash) {
        this.lozinka_hash = lozinka_hash;
    }
}