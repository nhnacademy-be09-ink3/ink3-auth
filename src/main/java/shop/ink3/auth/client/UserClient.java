package shop.ink3.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shop.ink3.auth.client.dto.AuthResponse;
import shop.ink3.auth.client.dto.CommonResponse;

@FeignClient(name = "shop-service")
public interface UserClient {
    @GetMapping("/shop/users/auth/{loginId}")
    CommonResponse<AuthResponse> getUser(@PathVariable String loginId);

    @GetMapping("/shop/users/auth/social/{provider}/{providerUserId}")
    CommonResponse<AuthResponse> getSocialUser(@PathVariable String provider, @PathVariable String providerUserId);

    @PatchMapping("/shop/users/{userId}/last-login")
    ResponseEntity<Void> updateUserLastLogin(@PathVariable long userId);

    @GetMapping("/shop/admins/auth/{loginId}")
    CommonResponse<AuthResponse> getAdmin(@PathVariable String loginId);

    @PatchMapping("/shop/admins/{adminId}/last-login")
    ResponseEntity<Void> updateAdminLastLogin(@PathVariable long adminId);
}
