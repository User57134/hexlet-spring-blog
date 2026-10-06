package io.hexlet.controller.api;

import io.hexlet.exception.ResourceNotFoundException;
import io.hexlet.model.User;
import io.hexlet.repository.UserRepository;
import org.hibernate.type.descriptor.java.spi.CollectionJavaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.util.Comparator;
import java.util.List;


@RestController
@RequestMapping("/api")
public class UsersController {

    private final UserRepository repository;

    @Autowired
    public  UsersController(UserRepository userRepository) {
        repository = userRepository;
    }

    @Value("${app.page-size}")
    private int maxPageSize;

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK) // 200
    public List<User> index(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        int pageSize = limit;
        if (pageSize > maxPageSize) {
            pageSize = maxPageSize;
        }

        var users = repository.findAll();

        return users.stream()
                .sorted(Comparator.comparing(User::getId))
                .skip((page - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    @PostMapping("/users")
    public ResponseEntity<User> create(@RequestBody User user) {
        var email = user.getEmail();

        if (email != null && !email.isBlank()) {
            var savedUser = repository.save(user);

            URI location = URI.create("/users/" + savedUser.getId());
            return ResponseEntity.created(location).body(savedUser);
        } else {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Email should be not empty");
        }
    }

    @GetMapping("/users/{id}") // Вывод страницы
    public ResponseEntity<User> show(@PathVariable Long id) {
        var user = repository.findById(id);

        return ResponseEntity.of(user);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void delete(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Usser with id = " + id + " was not found");
        }
    }
}
