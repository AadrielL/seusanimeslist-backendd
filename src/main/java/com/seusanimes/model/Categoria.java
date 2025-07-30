package com.seusanimes.model; // PACOTE CORRETO PARA O SEUSANIMELIST

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// Opcional: Você pode adicionar as anotações do Lombok aqui também, se quiser
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;
// @Data
// @NoArgsConstructor
// @AllArgsConstructor

@Entity
@Table(name = "categorias") // Nome da tabela no banco de dados
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    // mappedBy aponta para o nome do atributo na classe "dona" do relacionamento (Anime)
    @ManyToMany(mappedBy = "categorias", fetch = FetchType.LAZY)
    @JsonIgnore // Evita loops infinitos em serialização JSON ao retornar categorias
    private Set<Anime> animes = new HashSet<>(); // HashSet é uma boa escolha para coleções

    public Categoria() {
    }

    public Categoria(String nome) {
        this.nome = nome;
    }

    // Getters e Setters (se não estiver usando Lombok, eles são necessários)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Set<Anime> getAnimes() {
        return animes;
    }

    public void setAnimes(Set<Anime> animes) {
        this.animes = animes;
    }

    // Métodos equals e hashCode (importantes para coleções e comparações)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        return Objects.equals(id, categoria.id) &&
               Objects.equals(nome, categoria.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome);
    }

    @Override
    public String toString() {
        return "Categoria{" +
               "id=" + id +
               ", nome='" + nome + '\'' +
               '}';
    }
}