package com.marcosbarbero.lab.sec.oauth.jwt.ds;
 

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map; 
import org.junit.jupiter.api.Assertions;
import static  org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.test.context.junit.jupiter.SpringExtension; 
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

//Before running this test make sure authorization server is running   

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ResourceServerJwtApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthenticationClaimsIntegrationTest {
	private RestTemplate restTemplate;

	private static final String CLIENT_ID = "clientId";
	private static final String CLIENT_SECRET = "secret";
	private static final String ACCOUNT_ID = "user";
	private static final String ACCOUNT_PASWD = "pass";
	private static final String AUTH_URL = "http://localhost:9000/oauth/token";

	
	@LocalServerPort
	private int randomServerPort;

	@Autowired
	private JwtTokenStore tokenStore;
	
	@BeforeEach
	public void setup() {
		restTemplate = new RestTemplate();
	}

	@Test
	public void whenTokenDontContainIssuer_thenSuccess() {
		final String tokenValue = obtainAccessToken(CLIENT_ID, ACCOUNT_ID, ACCOUNT_PASWD);
		Assertions.assertNotNull(tokenValue);

		System.out.println("AccessToken:" + tokenValue);
		final OAuth2Authentication auth = tokenStore.readAuthentication(tokenValue);

		System.out.println("----------------------------------------------");

		System.out.println("OAuth2Authentication: " + auth);
		assertTrue(auth.isAuthenticated());

		System.out.println("Details: " + auth.getDetails());

		Map<String, Object> details = (Map<String, Object>) auth.getDetails();
//        Assert.assertNotNull(details);
//        
//        assertTrue(details.containsKey("organization"));
//        System.out.println(details.get("organization"));
	}

	@Test
	public void whenTokenDontContainAndRetrieveTokenAndUseRefreshToken() {
		final Response response = obtainResponse(CLIENT_ID, ACCOUNT_ID, ACCOUNT_PASWD);
		System.out.println("----------------------------------------------");
		System.out.println(response.asString());
		final String accessToken = response.jsonPath().getString("access_token");
		final String refreshToken = response.jsonPath().getString("refresh_token");
		final Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "refresh_token");
		params.put("refresh_token", refreshToken);

		final Response response2 = RestAssured.given().auth().preemptive().basic(CLIENT_ID, CLIENT_SECRET).and().with()
				.params(params).when().post(AUTH_URL);
		System.out.println("----------------------------------------------");
		System.out.println(response2.getBody().asString());
	}

	@Test
	@Disabled
	public void givenInvalidRole_whenGetSecureRequest_thenForbidden() throws Exception {
		final String accessToken = obtainAccessTokenByRestTemplate(ACCOUNT_ID, ACCOUNT_PASWD);
		System.out.println("token:" + accessToken);
	}

	@Test
//  @Ignore
	public void contactResource() throws Exception {
		final String accessToken = obtainAccessToken(CLIENT_ID, ACCOUNT_ID, ACCOUNT_PASWD);
		System.out.println("token:" + accessToken);
		final Map<String, String> params = new HashMap<String, String>();
		String url = "http://localhost:" + randomServerPort + "/me";
//		params.put("grant_type", "password");
//		params.put("client_id", clientId); 
//		params.put("client_secret", CLIENT_SECRET); 
//		params.put("username", username);
//		params.put("password", password);
		final Response response = RestAssured.given().auth().preemptive().oauth2(accessToken).and().with()
				.params(params).when().get(url);
		System.out.println("----------------------------------------------");
		System.out.println(response.asString());
		System.out.println("----------------------------------------------");
	}

	protected String obtainAccessTokenByRestTemplate(String username, String password) throws Exception {
		// set up the basic authentication header

		String authorizationHeader = "Basic "
				+ Base64.getEncoder().encode((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());

		// setting up the request headers
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));
		requestHeaders.add("Authorization", authorizationHeader);

		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "password");
		params.add("client_id", CLIENT_ID);
		params.add("client_secret", CLIENT_SECRET);
		params.add("username", username);
		params.add("password", password);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params,
				requestHeaders);

		// @formatter:off
		ResponseEntity<String> response = restTemplate.exchange(AUTH_URL, HttpMethod.POST, request, String.class);
		System.out.println(response.getBody());

		JacksonJsonParser jsonParser = new JacksonJsonParser();
		return jsonParser.parseMap(response.getBody()).get("access_token").toString();
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
