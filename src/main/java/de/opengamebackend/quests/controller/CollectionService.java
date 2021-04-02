package de.opengamebackend.quests.controller;

import de.opengamebackend.collection.model.requests.AddCollectionItemsRequest;
import de.opengamebackend.net.ApiErrors;
import de.opengamebackend.net.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Service
public class CollectionService {
    private DiscoveryClient discoveryClient;

    @Autowired
    public CollectionService(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public void addCollectionItems(String playerId, AddCollectionItemsRequest request) throws ApiException {
        // Locate service.
        List<ServiceInstance> instances = this.discoveryClient.getInstances("open-game-backend-collection");

        if (instances.isEmpty()) {
            throw new ApiException(ApiErrors.COLLECTION_SERVICE_UNAVAILABLE_CODE,
                    ApiErrors.COLLECTION_SERVICE_UNAVAILABLE_MESSAGE);
        }

        ServiceInstance instance = instances.get(0);
        URI serviceUri = instance.getUri();

        // Send request.
        String relativeUri = "/admin/collection/" + playerId + "/items";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AddCollectionItemsRequest> httpEntity = new HttpEntity<>(request, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForLocation(serviceUri + relativeUri, httpEntity);
    }
}
