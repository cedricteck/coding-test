package com.malt.codingtest.service;

import com.malt.codingtest.model.ApiStackResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LocalisationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${apistack.baserURl}")
    private String apiStackBaseUrl;

    @Value("${apistack.accessKey}")
    private String apiStackAccessKey;

    public ApiStackResponse localizeFromIp(String ip) {
        return restTemplate.getForObject(apiStackBaseUrl + "/" + ip + "?access_key=" + apiStackAccessKey, ApiStackResponse.class);
    }
}
