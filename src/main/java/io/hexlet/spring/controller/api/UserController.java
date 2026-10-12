package io.hexlet.spring.controller.api;

import io.hexlet.spring.dto.*;
import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.mapper.UserMapper;
import io.hexlet.spring.model.User;
import io.hexlet.spring.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Comparator;
import java.util.List;


@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserRepository repository, UserMapper mapper) {
        userRepository = repository;
        userMapper = mapper;
    }

    @Value("${app.page-size}")
    private int maxPageSize;

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK) // 200
    public List<UserDTO> index(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        int pageSize = limit;
        if (pageSize > maxPageSize) {
            pageSize = maxPageSize;
        }

        var users = userRepository.findAll();

        return users.stream()
                .sorted(Comparator.comparing(User::getId))
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .map(userMapper::makeDTO)
                .toList();
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserCreateDTO dto) {
        var user = userRepository.save(userMapper.makeEntity(dto));

        URI location = URI.create("/users/" + user.getId());
        return ResponseEntity.created(location).body(userMapper.makeDTO(user));
    }

    @PutMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO data) {
        var user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User with the id = " + id + " not found"));

        userMapper.updateEntity(user, data);

        return userMapper.makeDTO(userRepository.save(user));
    }

    @GetMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO show(@PathVariable Long id) {
        var user  = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with the id =" + id + " not found"));

        return userMapper.makeDTO(user);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void delete(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Usser with id = " + id + " was not found");
        }
    }
}
