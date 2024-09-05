package com.muybien.youtube_clone.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/history")
    @ResponseStatus(OK)
    public List<Integer> getUserHistory(Authentication connectedUser) {
        return userService.getUserWatchVideosHistory(connectedUser);
    }

    @PostMapping("/toggle/subscribe/{targetUserId}")
    @ResponseStatus(OK)
    public void toggleSubscription(@PathVariable Integer targetUserId, Authentication connectedUser) {
        userService.toggleUserSubscription(targetUserId, connectedUser);
    }
}
