package app.weather.service;

import app.weather.config.QWeatherApiConfig;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
@Slf4j
public class JwtService {
    private final QWeatherApiConfig qWeatherApiConfig;

    private static final String JWT_CACHE_KEY = "'qweather_jwt_token'";

    @Autowired
    public JwtService(QWeatherApiConfig qWeatherApiConfig) {
        this.qWeatherApiConfig = qWeatherApiConfig;
    }

    @Cacheable(key = JWT_CACHE_KEY, value = "jwtTokenCache")
    public String generateJwtToken() {
        log.info("Generating JWT token");

        try {
            JSONObject header = new JSONObject();
            header.put("alg", "EdDSA");
            header.put("kid", qWeatherApiConfig.getKeyId());
            // payload
            JSONObject payload = null;
            payload = new JSONObject();
            payload.put("sub", qWeatherApiConfig.getProjectId());
            payload.put("iat", DateTime.now().getMillis() / 1000);
            // 过期时间 一天
            payload.put("exp", DateTime.now().plusDays(1).getMillis() / 1000);

            // Base64url header+payload
            String headerEncoded = base64UrlEncode(header.toString().getBytes(StandardCharsets.UTF_8));
            String payloadEncoded = base64UrlEncode(payload.toString().getBytes(StandardCharsets.UTF_8));

            // Create signing input
            String data = headerEncoded + "." + payloadEncoded;

            // sign
            byte[] signature = null;
            try {
                signature = sign(data.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.error("Failed to sign data, error: {}", Throwables.getStackTraceAsString(e));
                throw new RuntimeException(e);
            }
            String signatureEncoded = base64UrlEncode(signature);

            return data + "." + signatureEncoded;
        } catch (RuntimeException e) {
            log.error("Failed to generate JWT token, error: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        }
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private byte[] sign(byte[] data) throws Exception {
        // Decode private key from Base64
        byte[] privateKeyBytes = Base64.getDecoder().decode(qWeatherApiConfig.getPrivateKey());

        // Create EdDSA private key
        PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey signingKey = new EdDSAPrivateKey(encodedKeySpec);

        // Sign
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        final Signature s = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
        s.initSign(signingKey);
        s.update(data);
        return s.sign();
    }
}
