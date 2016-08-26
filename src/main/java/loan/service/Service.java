package loan.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import loan.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by jurikolo on 26.08.16.
 */
public class Service {
    private final static Logger log = LoggerFactory.getLogger(Service.class);
    private static final int cntLimit = 2;
    private static final long timeLimit = 10;
    private static final Cache<String, Integer> limitMap = CacheBuilder.newBuilder()
            .expireAfterWrite(timeLimit, TimeUnit.SECONDS)
            .build();

    public static Boolean validateRequest(Map<String, String> request, Optional<Customer> customer, String countryCode) {
        //verify entered parameters exists
        log.info("Verify amount parameter is present in a request");
        if (null == request.get("amount")) return false;
        log.info("Verify term parameter is present in a request");
        if (null == request.get("term")) return false;
        log.info("Verify name parameter is present in a request");
        if (null == request.get("name")) return false;
        log.info("Verify surname parameter is present in a request");
        if (null == request.get("surname")) return false;
        log.info("Verify personalId parameter is present in a request");
        if (null == request.get("personalId")) return false;

        //verify blacklist
        log.info("Verify whether customer is blacklisted");
        if (!customer.isPresent() || customer.get().getBlackListed()) return false;

        //verify tps
        if (limitMap.asMap().containsKey(countryCode)) {
            if (limitMap.asMap().get(countryCode) > cntLimit) {
                HttpHeaders httpHeaders = new HttpHeaders();
                return false;
            } else {
                limitMap.asMap().put(countryCode, limitMap.asMap().get(countryCode) + 1);
            }
        } else {
            limitMap.asMap().put(countryCode, 1);
        }
        return true;
    }

    public static String getCountryByIp(String ip) {
        final String uri = "http://ip-api.com/json/" + ip;
        Map<String, String> result = Collections.emptyMap();
        RestTemplate restTemplate = new RestTemplate();
        try {
            result = restTemplate.getForObject(uri, Map.class);
        } catch (Exception e) {
            log.error("Unable to resolve country code by ip: " + e.getMessage(), e);
            return "LV";
        }
        if (result.containsKey("countryCode")) return result.get("countryCode");
        else return "LV";
    }
}
