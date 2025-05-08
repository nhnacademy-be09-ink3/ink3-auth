package shop.ink3.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import shop.ink3.auth.client.dto.AuthResponse;
import shop.ink3.auth.client.dto.CommonResponse;

@FeignClient(name = "user-api")
public interface UserClient {
    @RequestMapping(method = RequestMethod.GET, value = "/users/auth/{loginId}")
    CommonResponse<AuthResponse> getUser(@PathVariable String loginId);

    @RequestMapping(method = RequestMethod.PATCH, value = "/users/{userId}/last-login")
    ResponseEntity<Void> updateUserLastLogin(@PathVariable long userId);

    @RequestMapping(method = RequestMethod.GET, value = "/admins/auth/{loginId}")
    CommonResponse<AuthResponse> getAdmin(@PathVariable String loginId);

    @RequestMapping(method = RequestMethod.PATCH, value = "/admins/{adminId}/last-login")
    ResponseEntity<Void> updateAdminLastLogin(@PathVariable long adminId);
}
