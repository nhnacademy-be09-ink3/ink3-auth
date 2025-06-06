package shop.ink3.auth.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import shop.ink3.auth.client.dto.CommonResponse;
import shop.ink3.auth.client.user.dto.AuthResponse;
import shop.ink3.auth.client.user.dto.UserStatusResponse;
import shop.ink3.auth.dto.SocialUserResponse;

@FeignClient(name = "shop-service")
public interface UserClient {
    @GetMapping("/shop/users/{loginId}/auth")
    CommonResponse<AuthResponse> getUser(@PathVariable String loginId);

    @GetMapping("/shop/users/social/{provider}/{providerId}")
    CommonResponse<SocialUserResponse> getSocialUser(@PathVariable String provider, @PathVariable String providerId);

    @GetMapping("/shop/users/status")
    CommonResponse<UserStatusResponse> getUserStatus(@RequestParam String loginId);

    @PatchMapping("/shop/users/{userId}/last-login")
    void updateUserLastLogin(@PathVariable long userId);

    @PatchMapping("/shop/users/{loginId}/activate")
    void activateUser(@PathVariable String loginId);

    @GetMapping("/shop/admins/{loginId}/auth")
    CommonResponse<AuthResponse> getAdmin(@PathVariable String loginId);

    @PatchMapping("/shop/admins/{adminId}/last-login")
    ResponseEntity<Void> updateAdminLastLogin(@PathVariable long adminId);
}
