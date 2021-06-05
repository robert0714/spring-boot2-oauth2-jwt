package com.marcosbarbero.lab.sec.oauth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo; 

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static  org.junit.jupiter.api.Assertions.*;

import io.restassured.RestAssured;
import io.restassured.response.Response;

// need both oauth2-jwt-server and oauth2-jwt-resource to be running

public class TokenRevocationLiveTest {
	private static final String CLIENT_ID = "clientId";
	private static final String CLIENT_SECRET = "secret";
	private static final String ACCOUNT_ID = "user";
	private static final String ACCOUNT_PASWD = "pass";
	private static final String AUTH_URL = "http://localhost:9000/oauth/token";
	private static final String AUTH_URL_CLIENT = "http://localhost:9000/oauth/authorize";

    @Test
    public void whenObtainingAccessToken_thenCorrect() { 
        final String accessToken = obtainAccessToken(CLIENT_ID, ACCOUNT_ID, ACCOUNT_PASWD);
        assertNotNull(accessToken);

        final Response resourceServerResponse = RestAssured.given().header("Authorization", "Bearer " + accessToken).get("http://localhost:9100/me");
        assertEquals(resourceServerResponse.getStatusCode(), 200);
    }

    //

    

    private String obtainRefreshToken(String clientId, final String refreshToken) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("grant_type", "refresh_token");
        params.put("client_id", clientId);
        params.put("refresh_token", refreshToken);
        final Response response = RestAssured.given().auth().preemptive().basic(clientId, CLIENT_SECRET).and().with()
        		.params(params).when().post(CLIENT_SECRET);
        return response.jsonPath().getString("access_token");
    }

    private void authorizeClient(String clientId) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("response_type", "code");
        params.put("client_id", clientId);
        params.put("scope", "read,write");
        final Response response = RestAssured.given().auth().preemptive().basic(clientId, CLIENT_SECRET).and().with()
        		.params(params).when().post(AUTH_URL_CLIENT);
    }
    
	protected Response obtainResponse(String clientId, String username, String password) {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "password");
//		params.put("client_id", clientId); 
//		params.put("client_secret", CLIENT_SECRET); 
		params.put("username", username);
		params.put("password", password);
		final Response response = RestAssured.given().auth().preemptive().basic(clientId, CLIENT_SECRET).and().with()
				.params(params).when().post(AUTH_URL);

		return response;
	}

	protected String obtainAccessToken(String clientId, String username, String password) {
		final Response response = obtainResponse(clientId, username, password);
		String content = response.getBody().asString();
		System.out.println(content);
		return response.jsonPath().getString("access_token");
	}

	protected String obtainRefreshToken(String clientId, String username, String password) {
		final Response response = obtainResponse(clientId, username, password);
		String content = response.getBody().asString();
		System.out.println(content);
		return response.jsonPath().getString("refresh_token");
	}
}