package io.hexlet.spring.specification;

import io.hexlet.spring.dto.PostParamsDTO;
import io.hexlet.spring.model.Post;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;


@Component // Для возможности автоматической инъекции
public class PostSpecification {
    // Генерация спецификации на основе параметров внутри DTO
    // Для удобства каждый фильтр вынесен в свой метод
    public Specification<Post> build(PostParamsDTO params) {
        return withAuthorId(params.getAuthorId())
                .and(withCreatedAtGt(params.getCreatedAtGt()))
                .and(withCreatedAtLt(params.getCreatedAtLt()))
                .and(withNameContained(params.getNameCont()))
                .and(withTagId(params.getTagId()))
                .and(withTagsIn(params.getTagsIn()));
    }

    private Specification<Post> withAuthorId(Long authorId) {
        return (root, query, cb) ->
                authorId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("author").get("id"), authorId);
    }

    private Specification<Post> withCreatedAtGt(LocalDate date) {
        return (root, query, cb) ->
                date == null ? cb.conjunction() : cb.greaterThan(root.get("createdAt"), date);
    }

    private Specification<Post> withCreatedAtLt(LocalDate date) {
        return (root, query, cb) ->
                date == null ? cb.conjunction() : cb.lessThan(root.get("createdAt"), date);
    }

    private Specification<Post> withNameContained(String name) {
        return (root, query, cb) ->
                (name == null || name.trim().isEmpty())
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("title")), "%" + name.toLowerCase() + "%");
    }

    private Specification<Post> withTagId(Long tagId) {
        return (root, query, cb) ->
                tagId == null ? cb.conjunction() : cb.equal(root.join("tags").get("id"), tagId);
    }

    private Specification<Post> withTagsIn(List<Long> tagsIn) {
        return (root, query, cb) -> {
            if (tagsIn == null || tagsIn.isEmpty()) {
                return cb.conjunction();
            }

            query.distinct(true);

            return root.join("tags").get("id").in(tagsIn);
        };
    }
}
