package io.hexlet.controller.api;

import io.hexlet.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UsersController {

    @Value("${app.page-size}")
    private int maxPageSize;

    private List<User> users = new ArrayList<User>();

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK) // 200
    public List<User> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int elementsPerPage) {

        int pageSize = elementsPerPage;
        if (pageSize > maxPageSize) {
            pageSize = maxPageSize;
        }

        return users.stream().skip((int)((page - 1) * pageSize)).limit(elementsPerPage).toList();
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        var email = user.getEmail();

        if (email != null && !email.isBlank()) {
            users.add(user);

            URI location = URI.create("/users/" + user.getId());
            return ResponseEntity.created(location).body(user);
        } else {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Email should be not empty");
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void deleteUser(@PathVariable Long id) {
        boolean removed = users.removeIf(user -> user.getId().equals(id));

        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}
