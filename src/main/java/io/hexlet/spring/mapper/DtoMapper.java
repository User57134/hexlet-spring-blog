package io.hexlet.spring.mapper;

import io.hexlet.spring.dto.PostDTO;
import io.hexlet.spring.dto.UserDTO;
import io.hexlet.spring.model.Post;
import io.hexlet.spring.model.User;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class DtoMapper {

    public PostDTO map(Post post) {
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

    public UserDTO map(User user) {
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
}
