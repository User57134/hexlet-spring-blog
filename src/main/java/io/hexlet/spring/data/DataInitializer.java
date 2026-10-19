package io.hexlet.spring.data;

import org.hibernate.generator.Generator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev") // 👈 Выполнится ТОЛЬКО в профиле dev. В тестах этот класс создаваться не будет.
// ApplicationRunner используется для выполнения определенного кода сразу после полного запуска приложения
public class DataInitializer implements ApplicationRunner {

    private final ModelGenerator modelGenerator;

    @Autowired
    DataInitializer(ModelGenerator generator) {
        modelGenerator = generator;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("🚀 Старт автоматической генерации данных для DEV...");
        modelGenerator.generateData(5);
        System.out.println("✅ База данных успешно наполнена!");
    }
}
