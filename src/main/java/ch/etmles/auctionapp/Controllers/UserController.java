package ch.etmles.auctionapp.Controllers;

import ch.etmles.auctionapp.Entities.User;
import ch.etmles.auctionapp.Repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    private final UserRepository userRepository;

    UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    List<User> all() {
        return userRepository.findAll();
    }

    @PostMapping("/users")
    public ResponseEntity<User> newUser(@RequestBody User newUser) {
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("A user with this email address already exists.");
        }
        User savedUser = userRepository.save(newUser);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @GetMapping("/users/{id}")
    User one(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @PutMapping("/users/{id}")
    User replaceUser(@RequestBody User newUser, @PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(newUser.getName());
                    user.setEmail(newUser.getEmail());
                    user.setConnected(newUser.isConnected());
                    user.setWallet(newUser.getWallet());
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    newUser.setId(id);
                    return userRepository.save(newUser);
                });
    }

    @PutMapping("/users/{id}/isConnected")
    User updateIsConnected(@PathVariable Long id, @RequestBody Map<String, Boolean> isConnectedMap) {
    boolean isConnected = isConnectedMap.get("isConnected");
    return userRepository.findById(id)
            .map(user -> {
                user.setConnected(isConnected);
                return userRepository.save(user);
            })
            .orElseThrow(() -> new UserNotFoundException(id));
    }


    @DeleteMapping("/users/{id}")
    void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    @PutMapping("/users/{id}/email")
    User updateEmail(@PathVariable Long id, @RequestBody Map<String, String> emailMap) {
        String newEmail = emailMap.get("email");
        return userRepository.findById(id)
                .map(user -> {
                    user.setEmail(newEmail);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new UserNotFoundException(id));
    }


    @PutMapping("/users/{id}/wallet")
    User updateWallet(@PathVariable Long id, @RequestBody Map<String, BigDecimal> walletMap) {
        BigDecimal amount = walletMap.get("amount");
        return userRepository.findById(id)
                .map(user -> {
                    user.setWallet(user.getWallet().add(amount));
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @PutMapping("/users/{id}/wallet/deduct")
    User deductFromWallet(@PathVariable Long id, @RequestBody Map<String, BigDecimal> walletMap) {
        BigDecimal amount = walletMap.get("amount");
        return userRepository.findById(id)
                .map(user -> {
                    if (user.getWallet().compareTo(amount) < 0) {
                        throw new UserInsufficientFundsException(id, amount);
                    }
                    user.setWallet(user.getWallet().subtract(amount));
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new UserNotFoundException(id));
    }


}
