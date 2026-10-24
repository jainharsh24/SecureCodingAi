package com.securecode.ai.evaluation.groq;
import static org.junit.jupiter.api.Assertions.*;
import com.securecode.ai.entity.*;
import com.securecode.ai.evaluation.SecurityEvaluationStatus;
import com.securecode.ai.evaluation.groq.config.GroqProperties;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
class GroqSecurityEvaluatorTests {
    private final JsonMapper json=JsonMapper.builder().build();
    @Test void distinguishesDisabledAndMissingConfigurationWithoutCallingProvider(){
        GroqClient client=(a,b)->fail("provider must not be called");
        assertEquals(SecurityEvaluationStatus.DISABLED, evaluator(false,"configured",client).evaluate(submission()).status());
        assertEquals(SecurityEvaluationStatus.NOT_CONFIGURED, evaluator(true," ",client).evaluate(submission()).status());
    }
    @Test void mapsSafeVulnerableAndUncertainMockedResponses(){assertTrue(evaluator((a,b)->response("SAFE","NO")).evaluate(submission()).findings().isEmpty());assertEquals("SQL Injection",evaluator((a,b)->response("VULNERABLE","YES")).evaluate(submission()).findings().getFirst().vulnerabilityType());assertEquals("UNCERTAIN",evaluator((a,b)->response("UNCERTAIN","UNCERTAIN")).evaluate(submission()).assessment().status());}
    @Test void rejectsMalformedMockedOutput(){assertEquals(SecurityEvaluationStatus.ERROR,evaluator((a,b)->"bad").evaluate(submission()).status());}
    @Test void mapsAuthenticationTimeoutRateLimitAndProviderErrors(){assertEquals(SecurityEvaluationStatus.ERROR,evaluator((a,b)->fail(GroqProviderException.Kind.AUTHENTICATION)).evaluate(submission()).status());assertEquals(SecurityEvaluationStatus.TIMEOUT,evaluator((a,b)->fail(GroqProviderException.Kind.TIMEOUT)).evaluate(submission()).status());assertEquals(SecurityEvaluationStatus.ERROR,evaluator((a,b)->fail(GroqProviderException.Kind.RATE_LIMITED)).evaluate(submission()).status());assertEquals(SecurityEvaluationStatus.ERROR,evaluator((a,b)->fail(GroqProviderException.Kind.PROVIDER)).evaluate(submission()).status());}
    private String fail(GroqProviderException.Kind kind){throw new GroqProviderException(kind,"mocked "+kind);}
    private String fail(String message){throw new AssertionError(message);}
    private GroqSecurityEvaluator evaluator(GroqClient c){return evaluator(true,"unused",c);}
    private GroqSecurityEvaluator evaluator(boolean enabled,String apiKey,GroqClient c){return new GroqSecurityEvaluator(new GroqProperties(enabled,"https://api.groq.com/openai/v1","openai/gpt-oss-20b",apiKey,1000,200,4096,0,true),c,json);}
    private Submission submission(){return new Submission(new User("student@example.com","unused",Role.STUDENT),new Challenge(1,"SQL","Use prepared statements","java","Injection","SQL Injection","CWE-89","",Difficulty.EASY),"class Main {}",1);}
    private String response(String status,String expected){return "{\"status\":\""+status+"\",\"expected_vulnerability\":\""+expected+"\",\"evidence\":\"Review evidence\",\"severity\":\"HIGH\",\"cwe\":\"CWE-89\",\"confidence\":0.9,\"remediation\":\"Use a prepared statement\"}";}
}
