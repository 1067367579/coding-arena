import com.example.common.security.utils.JWTUtils;
import io.jsonwebtoken.Claims;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Test {

    @org.junit.jupiter.api.Test
    public void testForJWT() {
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",78902266L);
        //secret不能硬编码 定期更换
        String key = "9070xt";
        String token = JWTUtils.createToken(claims, key);
        System.out.println(token);
        Claims parseClaims = JWTUtils.parseToken(token, key);
        System.out.println(parseClaims);
    }

    @org.junit.jupiter.api.Test
    public void testForUUID() {
        System.out.println(UUID.randomUUID());
    }
}
