package fr.pierrickrouxel.jpaentitygenerator.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.pierrickrouxel.jpaentitygenerator.config.CodeGeneratorConfig.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class CodeGeneratorConfigTest {

    @Test
    public void testHasEnvVariables() {
        {
            assertFalse(hasEnvVariables(""));
            assertFalse(hasEnvVariables(" "));
            assertFalse(hasEnvVariables("foo"));
            assertFalse(hasEnvVariables("foo barr"));
            assertFalse(hasEnvVariables("foo $ test"));
        }
        {
            assertTrue(hasEnvVariables("${TEST}"));
            assertTrue(hasEnvVariables("Something is ${WRONG}"));
            assertTrue(hasEnvVariables("${WHAT} is happening?"));
        }
    }

    @Test
    public void testReplaceEnvVariables() {
        Map<String, String> env = System.getenv();
        List<String> keys = new ArrayList<>(env.keySet());
        String k1 = keys.get(0);
        String v1 = env.get(k1);
        String k2 = keys.get(1);
        String v2 = env.get(k2);
        String k3 = keys.get(2);
        String v3 = env.get(k3);

        HashMap<String, String> environment = new HashMap<>();
        String k4 = "test";
        String v4 = "test";
        environment.put(k4, v4);

        assertTrue(replaceEnvVariables("${" + k1 + "} is missing!", environment).equals(v1 + " is missing!"));
        assertTrue(replaceEnvVariables("You are the ${" + k2 + "}", environment).equals("You are the " + v2));
        assertTrue(replaceEnvVariables("${" + k3 + "}", environment).equals(v3));
        assertTrue(replaceEnvVariables("You can override ${" + k4 + "}", environment).equals("You can override " + v4));
        assertTrue(replaceEnvVariables("as is $", environment).equals("as is $"));
        assertTrue(replaceEnvVariables("${" + k1 + "}${" + k2 + "}", environment).equals(v1 + v2));
    }

}
