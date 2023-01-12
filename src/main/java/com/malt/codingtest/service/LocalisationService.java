package com.malt.codingtest.service;

import com.malt.codingtest.dto.ApiStackResponseDto;
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

    public ApiStackResponseDto localizeFromIp(String ip) {
        return restTemplate.getForObject(apiStackBaseUrl + "/" + ip + "?access_key=" + apiStackAccessKey, ApiStackResponseDto.class);
    }
}
