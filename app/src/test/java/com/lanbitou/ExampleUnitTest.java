package com.lanbitou;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testSplitStringByN(){
        String str = "我和很多人一同,漫游在无人知的原野上";
        StringBuilder sb = new StringBuilder(str);
        for(int i = 1; i < str.length(); i++){
            sb.insert(i,"\n");
        }
        System.out.print(sb.toString());
    }


}