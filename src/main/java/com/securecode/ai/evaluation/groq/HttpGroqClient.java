package com.securecode.ai.evaluation.groq;
import com.securecode.ai.evaluation.groq.config.GroqProperties;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
@Component public class HttpGroqClient implements GroqClient {
    private final RestClient http; private final GroqProperties properties; private final JsonMapper json;
    public HttpGroqClient(GroqProperties properties, JsonMapper json) { SimpleClientHttpRequestFactory f=new SimpleClientHttpRequestFactory(); f.setConnectTimeout(Duration.ofMillis(properties.timeoutMs())); f.setReadTimeout(Duration.ofMillis(properties.timeoutMs())); this.http=RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(f).build(); this.properties=properties; this.json=json; }
    @Override public String complete(String system, String user) { return complete(system, user, "security_assessment", GroqResponseSchema.VALUE); }
    @Override public String complete(String system, String user, String schemaName, Map<String,Object> schema) {
        if (properties.apiKey()==null||properties.apiKey().isBlank()) throw new GroqProviderException(GroqProviderException.Kind.AUTHENTICATION,"GROQ_API_KEY is not configured");
        GroqProviderException last=null;
        for(int attempt=0;attempt<=properties.maxRetries();attempt++) try {
            String body=http.post().uri("/chat/completions").contentType(MediaType.APPLICATION_JSON).header("Authorization","Bearer "+properties.apiKey()).body(request(system,user,schemaName,schema)).retrieve().body(String.class);
            if(body==null||body.length()>properties.maxResponseChars()) throw new GroqProviderException(GroqProviderException.Kind.MALFORMED,"Groq returned an empty or oversized response");
            return content(body);
        } catch(GroqProviderException e) { last=e; if(!retryable(e)||attempt==properties.maxRetries()) throw e; pause(attempt); }
        catch(RestClientResponseException e) { last=httpError(e); if(!retryable(last)||attempt==properties.maxRetries()) throw last; pause(attempt); }
        catch(ResourceAccessException e) { last=new GroqProviderException(GroqProviderException.Kind.TIMEOUT,"Groq request timed out or could not connect",e); if(attempt==properties.maxRetries()) throw last; pause(attempt); }
        throw last==null?new GroqProviderException(GroqProviderException.Kind.PROVIDER,"Groq request failed"):last;
    }
    private Map<String,Object> request(String system,String user,String schemaName,Map<String,Object> schema) { Map<String,Object> format=properties.structuredOutput()?Map.of("type","json_schema","json_schema",Map.of("name",schemaName,"strict",true,"schema",schema)):Map.of("type","json_object"); return Map.of("model",properties.model(),"messages",List.of(Map.of("role","system","content",system),Map.of("role","user","content",user)),"max_tokens",properties.maxOutputTokens(),"response_format",format); }
    private String content(String body) { try { JsonNode n=json.readTree(body).path("choices"); if(!n.isArray()||n.isEmpty()) throw new GroqProviderException(GroqProviderException.Kind.MALFORMED,"Groq response contained no choices"); String value=n.get(0).path("message").path("content").asText(); if(value.isBlank()) throw new GroqProviderException(GroqProviderException.Kind.MALFORMED,"Groq response contained no message content"); return value; } catch(GroqProviderException e){throw e;} catch(Exception e){throw new GroqProviderException(GroqProviderException.Kind.MALFORMED,"Groq returned malformed JSON",e);} }
    private GroqProviderException httpError(RestClientResponseException e) { int s=e.getStatusCode().value(); if(s==401||s==403)return new GroqProviderException(GroqProviderException.Kind.AUTHENTICATION,"Groq authentication failed",e); if(s==429)return new GroqProviderException(GroqProviderException.Kind.RATE_LIMITED,"Groq rate limit reached",e); return new GroqProviderException(GroqProviderException.Kind.PROVIDER,"Groq provider returned HTTP "+s,e); }
    private boolean retryable(GroqProviderException e){return e.kind()==GroqProviderException.Kind.RATE_LIMITED||e.kind()==GroqProviderException.Kind.TIMEOUT||e.kind()==GroqProviderException.Kind.PROVIDER;}
    private void pause(int a){try{Thread.sleep(100L*(a+1));}catch(InterruptedException e){Thread.currentThread().interrupt();throw new GroqProviderException(GroqProviderException.Kind.TIMEOUT,"Groq retry interrupted",e);}}
}
