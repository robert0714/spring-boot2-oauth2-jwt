package com.marcosbarbero.lab.sec.oauth.jwt.ds;
 
import org.junit.jupiter.api.extension.ExtendWith; 
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension; 
import org.junit.jupiter.api.Test;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ResourceServerJwtApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT) 
public class ResourceServerIntegrationTest {

    @Test
    public void whenLoadApplication_thenSuccess() {

    }
}
