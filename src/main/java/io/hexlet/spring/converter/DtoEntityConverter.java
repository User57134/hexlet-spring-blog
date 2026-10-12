package io.hexlet.spring.converter;

import io.hexlet.spring.dto.PostCreateDTO;
import io.hexlet.spring.dto.PostDTO;
import io.hexlet.spring.dto.UserCreateDTO;
import io.hexlet.spring.dto.UserDTO;
import io.hexlet.spring.model.Post;
import io.hexlet.spring.model.User;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class DtoEntityConverter {

    public PostDTO makeDTO(Post post) {
        var dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setPublished(post.isPublished());
        dto.setUserId(post.getAuthorId());
        dto.setCreatedAt(LocalDateTime.ofInstant(post.getCreatedAt(), ZoneId.systemDefault()));
        dto.setUpdatedAt(LocalDateTime.ofInstant(post.getUpdatedAt(), ZoneId.systemDefault()));

        return dto;
    }

    public UserDTO makeDTO(User user) {
        var dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setBirthday(user.getBirthday());
        dto.setCreatedAt(LocalDateTime.ofInstant(user.getCreatedAt(), ZoneId.systemDefault()));
        dto.setUpdatedAt(LocalDateTime.ofInstant(user.getUpdatedAt(), ZoneId.systemDefault()));

        return dto;
    }

    public User makeEntity(UserCreateDTO dto) {
        var user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setBirthday(dto.getBirthday());

        return user;
    }

    public Post makeEntity(PostCreateDTO dto) {
        var post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        return post;
    }
}
