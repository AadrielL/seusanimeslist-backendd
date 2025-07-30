package com.seusanimes.model; // PACOTE CORRETO PARA O SEUSANIMELIST

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects; // Manter Objects para equals/hashCode customizados se precisar

@NoArgsConstructor
@AllArgsConstructor
@Data // Anotação do Lombok para gerar getters, setters, toString, equals, hashCode automaticamente
@Entity
@Table(name = "animes") // Nome da tabela que já tem os dados no banco de dados
public class Anime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // O banco de dados vai gerar o ID automaticamente
    private Long id;

    @Column(nullable = false, unique = true, name = "titulo") // O nome da coluna no DB é 'titulo'
    private String titulo; // Corresponde ao 'nome_do_anime' do seu antigo model, mas 'titulo' é mais consistente com a API externa

    @Column(columnDefinition = "TEXT")
    private String sinopse;

    private Integer episodios;
    private String imagemUrl;
    private String status;

    private LocalDate anoLancamento; // Campo para o ano de lançamento (LocalDate)

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "anime_categoria", // Nome da tabela de junção
        joinColumns = @JoinColumn(name = "anime_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categorias = new HashSet<>();

    // O Lombok gera construtores, getters, setters, toString, equals e hashCode
    // mas se você tiver alguma lógica específica em equals/hashCode como a original,
    // pode sobrescrever (o Lombok @Data já gera um padrão baseado em todos os campos não-transientes)

    // Métodos auxiliares para gerenciar categorias (importante para a relação)
    public void addCategoria(Categoria categoria) {
        if (this.categorias == null) {
            this.categorias = new HashSet<>();
        }
        this.categorias.add(categoria);
        // Garante a bidirecionalidade se Categoria também for configurada corretamente
        if (categoria.getAnimes() == null) {
            categoria.setAnimes(new HashSet<>());
        }
        categoria.getAnimes().add(this);
    }

    public void removeCategoria(Categoria categoria) {
        this.categorias.remove(categoria);
        categoria.getAnimes().remove(this);
    }

    // Você pode manter o equals e hashCode customizados da versão antiga se a lógica for crítica,
    // mas o @Data do Lombok já fornece uma implementação padrão razoável.
    // Se a intenção é que 'titulo' seja único, a validação no DB já ajuda.
    /*
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Anime anime = (Anime) o;
        return Objects.equals(id, anime.id) &&
               Objects.equals(titulo, anime.titulo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titulo);
    }
    */
}