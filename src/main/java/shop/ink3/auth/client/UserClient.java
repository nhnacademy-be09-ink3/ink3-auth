package shop.ink3.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shop.ink3.auth.client.dto.AuthResponse;
import shop.ink3.auth.client.dto.CommonResponse;
import shop.ink3.auth.dto.SocialUserResponse;

@FeignClient(name = "shop-service", url = "https://ink3.shop/dev/gateway")
public interface UserClient {
    @GetMapping("/shop/users/{loginId}/auth")
    CommonResponse<AuthResponse> getUser(@PathVariable String loginId);

    @GetMapping("/shop/users/social/{provider}/{providerId}")
    CommonResponse<SocialUserResponse> getSocialUser(@PathVariable String provider, @PathVariable String providerId);

    @PatchMapping("/shop/users/{userId}/last-login")
    ResponseEntity<Void> updateUserLastLogin(@PathVariable long userId);

    @GetMapping("/shop/admins/{loginId}/auth")
    CommonResponse<AuthResponse> getAdmin(@PathVariable String loginId);

    @PatchMapping("/shop/admins/{adminId}/last-login")
    ResponseEntity<Void> updateAdminLastLogin(@PathVariable long adminId);
}
