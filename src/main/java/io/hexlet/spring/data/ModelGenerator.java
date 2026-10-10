package io.hexlet.spring.data;

import io.hexlet.spring.model.Post;
import io.hexlet.spring.model.User;
import io.hexlet.spring.repository.PostRepository;
import io.hexlet.spring.repository.UserRepository;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

@Component
public class ModelGenerator {

    private final Faker faker;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public ModelGenerator(
            Faker faker, UserRepository userRepository, PostRepository postRepository) {
        this.faker = faker;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public void generateData(int size) {
        for (int i = 0; i < size; i++) {
            var user = makeUser();
            userRepository.save(user);

            var post = makePost(user);
            postRepository.save(post);
        }
    }

    public User makeUser() {
        var user = new User();
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setEmail(faker.internet().emailAddress());
        user.setBirthday(faker.timeAndDate().past());
        return user;
    }

    public Post makePost(User user) {
        var post = new Post();
        post.setTitle(faker.book().title());
        post.setContent(faker.lorem().maxLengthSentence(255));
        post.setPublished(faker.bool().bool());

        if (user != null) {
            post.setAuthorId(user.getId());
        }

        return post;
    }

}
