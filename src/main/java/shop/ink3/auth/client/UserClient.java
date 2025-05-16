package shop.ink3.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import shop.ink3.auth.client.dto.AuthResponse;
import shop.ink3.auth.client.dto.CommonResponse;

@FeignClient(name = "shop-service")
public interface UserClient {
    @RequestMapping(method = RequestMethod.GET, value = "/shop/users/auth/{loginId}")
    CommonResponse<AuthResponse> getUser(@PathVariable String loginId);

    @RequestMapping(method = RequestMethod.GET, value = "/shop/users/auth/social/{provider}/{providerUserId}")
    CommonResponse<AuthResponse> getSocialUser(@PathVariable String provider, @PathVariable String providerUserId);

    @RequestMapping(method = RequestMethod.PATCH, value = "/shop/users/{userId}/last-login")
    ResponseEntity<Void> updateUserLastLogin(@PathVariable long userId);

    @RequestMapping(method = RequestMethod.GET, value = "/shop/admins/auth/{loginId}")
    CommonResponse<AuthResponse> getAdmin(@PathVariable String loginId);

    @RequestMapping(method = RequestMethod.PATCH, value = "/shop/admins/{adminId}/last-login")
    ResponseEntity<Void> updateAdminLastLogin(@PathVariable long adminId);
}
