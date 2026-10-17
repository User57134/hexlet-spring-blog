package io.hexlet.spring.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "posts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(includeFieldNames = true, onlyExplicitlyIncluded = true)
public class Post implements BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @ToString.Include
    private Long id;

    @NotNull
    @Column(unique = true)
    @ToString.Include
    @EqualsAndHashCode.Include
    private String slug;

    @NotBlank(message = "Заглавие не должно быть пустым")
    @Size(min=3, max=100, message = "Заглавие должно быть от 3 до 100 символов длинной")
    @Column(length = 100, nullable = false)
    @ToString.Include
    private String title;

    @NotBlank(message = "Содержание не должно быть пустым")
    @Size(min=10, message = "Содержание должно быть от 10 символов длинной")
    @Column(nullable = false, columnDefinition = "TEXT")
    @ToString.Include
    private String content;

    @ManyToOne(
            fetch = FetchType.LAZY          // ленивая загрузка пользователя - при запросе из базы подгружаются только данные
                                            // поста, данные об авторе подгружаются если сделать непосредственный запрос:
                                            // post.getAuthor();
    )
    @JoinColumn(name = "user_id")
    private User author;

    @Column(nullable = false)
    private boolean published;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name="post_tag",
            joinColumns=  @JoinColumn(name="post_id", referencedColumnName="id"),
            inverseJoinColumns= @JoinColumn(name="tag_id", referencedColumnName="id")
    )
    private Set<Tag> tags = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;


    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getPosts().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getPosts().remove(this);
    }
}