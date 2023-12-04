package com.javamini02.controller;

import com.javamini02.exception.ValidationException;
import com.javamini02.model.GenderInfoDTO;
import com.javamini02.model.NationalityInfoDTO;
import com.javamini02.model.RandomUserDTO;
import com.javamini02.model.User;
import com.javamini02.service.UserService;
import com.javamini02.validator.EnglishAlphabetsValidator;
import com.javamini02.validator.Validator;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<List<User>> createUsers(@RequestParam("size") int size) {
        List<User> users = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            while (size > 0) {
                User user = new User();
                WebClient webClientForRandomUser = createWebClientWithTimeout(4000);
                RandomUserDTO randomUserDTO = webClientForRandomUser.get().uri("https://randomuser.me/api/")
                        .retrieve().bodyToMono(RandomUserDTO.class).block();

                assert randomUserDTO != null;
                String firstName = randomUserDTO.getResults().get(0).getName().getFirst();
                String nat = randomUserDTO.getResults().get(0).getNat();
                String gender = randomUserDTO.getResults().get(0).getGender();

                // Validate input parameters using custom validators
                validateInput("firstName", firstName, EnglishAlphabetsValidator.getInstance());
                validateInput("nat", nat, EnglishAlphabetsValidator.getInstance());
                validateInput("gender", gender, EnglishAlphabetsValidator.getInstance());

                CompletableFuture<NationalityInfoDTO> nationalityFuture = CompletableFuture.supplyAsync(() -> {
                    WebClient webClientForNationalityInfo = createWebClientWithTimeout(6000);
                    return webClientForNationalityInfo.get().uri("https://api.nationalize.io/?name=" + firstName)
                            .retrieve().bodyToMono(NationalityInfoDTO.class).block();
                }, executorService);

                CompletableFuture<GenderInfoDTO> genderFuture = CompletableFuture.supplyAsync(() -> {
                    WebClient webClientForGenderInfo = createWebClientWithTimeout(6000);
                    return webClientForGenderInfo.get().uri("https://api.genderize.io/?name=" + firstName)
                            .retrieve().bodyToMono(GenderInfoDTO.class).block();
                }, executorService);

                CompletableFuture.allOf(nationalityFuture, genderFuture).join();

                NationalityInfoDTO nationalityInfoDTO = nationalityFuture.join();
                GenderInfoDTO genderInfo = genderFuture.join();

                boolean isNationalized = false;
                assert nationalityInfoDTO != null;
                for (NationalityInfoDTO.CountryInfo countryInfo : nationalityInfoDTO.getCountry()) {
                    if (Objects.equals(countryInfo.getCountry_id(), nat)) {
                        isNationalized = true;
                        break;
                    }
                }

                boolean isGender = false;
                assert genderInfo != null;
                if (Objects.equals(genderInfo.getGender(), gender))
                    isGender = true;

                if (isNationalized && isGender)
                    user.setVerificationStatus("VERIFIED");
                else
                    user.setVerificationStatus("TO_BE_VERIFIED");

                user.setName(firstName + " " + randomUserDTO.getResults().get(0).getName().getLast());
                user.setAge(randomUserDTO.getResults().get(0).getDob().getAge());
                LocalDateTime dob = randomUserDTO.getResults().get(0).getDob().getDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd'-'MMM yyyy HH:mm:ss");
                String formattedDob = dob.format(formatter);
                user.setDob(formattedDob);

                user.setGender(genderInfo.getGender());
                user.setNationality(nat);

                users.add(user);
                size--;
            }
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred", e);

        } finally {
            executorService.shutdown();
        }

        return ResponseEntity.ok(userService.createUser(users));
    }

    private void validateInput(String parameter, String value, Validator validator) {
        if (!validator.validate(value)) {
            throw new ValidationException("Invalid value for parameter " + parameter, HttpStatus.BAD_REQUEST);
        }
    }



    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @RequestParam("sortType") String sortType,
            @RequestParam("sortOrder") String sortOrder,
            @RequestParam("limit") int limit,
            @RequestParam("offset") int offset) {

        // Fetch all users from the service
        List<User> allUsers = userService.getAllUsers();

        // Apply sorting based on sortType and sortOrder
        sortUsers(allUsers, sortType, sortOrder);

        // Apply limit and offset
        List<User> filteredUsers = applyLimitAndOffset(allUsers, limit, offset);

        return ResponseEntity.ok(filteredUsers);
    }

    private void sortUsers(List<User> users, String sortType, String sortOrder) {
        Comparator<User> comparator = switch (sortType.toUpperCase()) {
            case "AGE" -> Comparator.comparing(User::getAge);
            case "NAME" -> Comparator.comparing(User::getName);
            default -> throw new IllegalArgumentException("Invalid sortType: " + sortType);
        };

        if ("ODD".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.thenComparing(user -> user.getName().length() % 2);
        } else if ("EVEN".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.thenComparing(user -> (user.getName().length() + 1) % 2);
        } else {
            throw new IllegalArgumentException("Invalid sortOrder: " + sortOrder);
        }

        users.sort(comparator);
    }

    private List<User> applyLimitAndOffset(List<User> users, int limit, int offset) {
        if (limit < 1 || limit > 5) {
            throw new IllegalArgumentException("Invalid limit value. Should be between 1 and 5.");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset value. Should be greater than or equal to 0.");
        }

        int endIndex = Math.min(offset + limit, users.size());
        return users.subList(offset, endIndex);
    }


    private WebClient createWebClientWithTimeout(int time) {
        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, time).responseTimeout(Duration.ofMillis(time)).doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(time, TimeUnit.MILLISECONDS)).addHandlerLast(new WriteTimeoutHandler(time, TimeUnit.MILLISECONDS)));

        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        return WebClient.builder().clientConnector(connector).build();
    }


}
