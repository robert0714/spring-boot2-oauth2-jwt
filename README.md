OAuth2 + JWT using Spring Boot 2 / Spring Security 5
---

Read more http://blog.marcosbarbero.com/centralized-authorization-jwt-spring-boot2/

Official Documents  https://docs.spring.io/spring-security-oauth2-boot/docs/current/reference/htmlsingle/

## TokenStore  Relationship
![TokenStore  Relationship](pic/data/token.png)

## TokenStore && TokenServices  Relationship
![TokenStore && TokenServices  Relationship](pic/data/token_service.png)

## TokenStore && TokenServices && Spring-Security Relationship
![TokenStore && TokenServices && Spring-Security Relationship](pic/data/tokenstore_spring_security_relation.png)

## AuthenticationServerConfiguration && Spring-Security Relationship
![AuthenticationServerConfiguration](pic/data/auth_config.png)


## ResourceConfiguration && Spring-Security Relationship
![ResourceConfiguration](pic/data/resource_config.png)

## Add ActiveDirectoryLdap feathers
If you wanna add **ActiveDirectory** / **Ldap** feathers, you can modify the code about com.marcosbarbero.lab.sec.oauth.jwt.config.security.**WebSecurityConfiguration** in **oauth2-jwt-server** .

```java
package com.marcosbarbero.lab.sec.oauth.jwt.config.security;
(ommit..)
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    (ommit..)
    
    //add this below
    private ActiveDirectoryLdapAuthenticationProvider adProvider;
    
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder());
                
        //for  ActiveDirectory or Ldap  server
       auth.authenticationProvider(adProvider);
    }
   (ommit...)
   
    /***
     * for test ad server 
     * */
    @Bean
	public ActiveDirectoryLdapAuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
    	adProvider = new ActiveDirectoryLdapAuthenticationProvider("iead.local",
				"ldap://192.168.2.12:389", "dc=iead,dc=local");
        
      //notice: why   sAMAccountName={1} ? ,beccause i see the original code
      // about ActiveDirectoryLdapAuthenticationProvider( spring-security-ldap 5.1.6)
      // line 326:  return SpringSecurityLdapTemplate.searchForSingleEntryInternal(context,
      // line 327:         searchControls, searchRoot, searchFilter,
      // line 328:         new Object[] { bindPrincipal, username });
      // username's position is {1} ,not {0}
    	adProvider.setSearchFilter("(&(objectClass=user)(sAMAccountName={1}))");
      
    	adProvider.setConvertSubErrorCodesToExceptions(true);
    	adProvider.setUseAuthenticationRequestCredentials(true);
		return adProvider;
	}
  (ommit...)
}
```
# Testing all together

##Generating the token

```bash
$ curl -u clientId:secret -X POST localhost:9000/oauth/token\?grant_type=password\&username=user\&password=pass

```
The response body is

```json
{
  "access_token" : "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2MjI5MDg3MTQsInVzZXJfbmFtZSI6InVzZXIiLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiZjkxYmYzODUtNmM1NC00ODFmLThkMjQtZDIyNzZmZDI4Y2I4IiwiY2xpZW50X2lkIjoiY2xpZW50SWQiLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXX0.czhuPctXBQ_t2iA9PYrLNd14ABgBzGCOnTtAh5TF0zEa-pea3tvwEIAEUeO3Du3RS0j_1CovT4whmPDeffN1dV4UMYgTvoOPhSvdpyhYJgTejpIGfBnH0mldGSLQFrA8zrp-dheFokdRZb7-6wyO9og0qIq_yOOTfz67tJLP-lc3_faWrEh5bGVreMYSS-dQ9C77U0w1EiHI_vcv7bAng6sz_EIkHErnN1sOir0mNnPDzCeSRO1BLj40bCP4NFfxoE0BMYUDG22QEiZ27XqjLUguEfNkNOcNDBtM8QF344419egpQ5556TN3GKVo623oXrdof4em97UdhHBApnv9jA",
  "token_type" : "bearer",
  "refresh_token" : "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJ1c2VyIiwic2NvcGUiOlsicmVhZCIsIndyaXRlIl0sImF0aSI6ImY5MWJmMzg1LTZjNTQtNDgxZi04ZDI0LWQyMjc2ZmQyOGNiOCIsImV4cCI6MTYyNTUwMDQxNCwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6Ijc1MDBkYWU2LTE0MDMtNGRmNS04YzFhLTAzZjE4N2U4MTRlYSIsImNsaWVudF9pZCI6ImNsaWVudElkIn0.VTdZHQf9XHfdB19oypg7x1jwv83Y4AvdUCpguEZ6WBjRxjTO7V_pUuHOaXX_Yr2ThMkjsu3xqxKgJBuDVAAYKQBCzZfPnqPY6bgv_uIyBiyC7jH-TgmfGMR7fW0IRKtf0E5-DXe0vNkon9lyep8jGKudJz9cWhYqNb_3n_BRyYjS31oIvOPQzpX2JCU6SkA_jVEsQ-ACWI5UtAvoYFv4p08LbKSHDWdVsj5ugCg3S4Vu30OcojGJd9yHNz-fx82YGykGQwND68ZsKTEBJIMVxmM40qpvPNjU7SptrWGik5dJFCaNJtYSAM1HyVYHwc8tEH7AIbZXr2kS-KcF08dpPQ",
  "expires_in" : 299,
  "scope" : "read write",
  "jti" : "f91bf385-6c54-481f-8d24-d2276fd28cb8"
}
```

## Accessing the resource
Now that you have generated the token copy the access_token and add it to the request on the Authorization HTTP Header, e.g:

```bash
$ curl localhost:9100/me -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2MjI5MDg3MTQsInVzZXJfbmFtZSI6InVzZXIiLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiZjkxYmYzODUtNmM1NC00ODFmLThkMjQtZDIyNzZmZDI4Y2I4IiwiY2xpZW50X2lkIjoiY2xpZW50SWQiLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXX0.czhuPctXBQ_t2iA9PYrLNd14ABgBzGCOnTtAh5TF0zEa-pea3tvwEIAEUeO3Du3RS0j_1CovT4whmPDeffN1dV4UMYgTvoOPhSvdpyhYJgTejpIGfBnH0mldGSLQFrA8zrp-dheFokdRZb7-6wyO9og0qIq_yOOTfz67tJLP-lc3_faWrEh5bGVreMYSS-dQ9C77U0w1EiHI_vcv7bAng6sz_EIkHErnN1sOir0mNnPDzCeSRO1BLj40bCP4NFfxoE0BMYUDG22QEiZ27XqjLUguEfNkNOcNDBtM8QF344419egpQ5556TN3GKVo623oXrdof4em97UdhHBApnv9jA"

```