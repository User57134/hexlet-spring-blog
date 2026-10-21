package io.hexlet.spring.component;

import io.hexlet.spring.dto.UserCreateDTO;
import io.hexlet.spring.mapper.UserMapper;
import io.hexlet.spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Component
@Profile("dev") // 👈 Выполнится во всех профилях, кроме профиля 'test'.
// ApplicationRunner используется для выполнения определенного кода сразу после полного запуска приложения
public class DataInitializer implements ApplicationRunner {

    private final ModelGenerator modelGenerator;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    DataInitializer(ModelGenerator generator, UserRepository repository, UserMapper mapper) {
        modelGenerator = generator;
        userRepository = repository;
        userMapper = mapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("🚀 Старт автоматической генерации данных для DEV...");

        var userData = new UserCreateDTO();
        userData.setFirstName("hexlet");
        userData.setLastName("CO");
        userData.setEmail("hexlet@hexlet.io");
        userData.setPassword("159753");
        var user = userMapper.makeEntity(userData);
        var admin = userRepository.save(user);

        System.out.println("Admin ID: " + admin.getId());

        modelGenerator.generateData(5);

        System.out.println("✅ База данных успешно наполнена!");
    }
}
