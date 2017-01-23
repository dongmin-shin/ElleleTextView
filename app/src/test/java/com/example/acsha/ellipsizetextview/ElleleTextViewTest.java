package com.example.acsha.ellipsizetextview;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author dong.min.shin on 2017. 1. 20..
 */
public class ElleleTextViewTest {

    @Test
    public void test_sample() {

        String text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.";
        System.out.println("Result: " + text.substring(0, text.length() - 1));
    }

    @Test
    public void replaceBlock() {
        String originalText = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.";

        int startIndex = 11;
        int endIndex = 21;

        String frontBlock = originalText.substring(0, startIndex);
        String targetBlock = originalText.substring(startIndex, endIndex);
        String backBlock = originalText.substring(endIndex, originalText.length());

        String composeBlock = frontBlock + targetBlock + backBlock;

        System.out.println("frontBlock: " + frontBlock);
        System.out.println("targetBlock: " + targetBlock);
        System.out.println("backBlock: " + backBlock);

        assertEquals(composeBlock.length(), originalText.length());
    }

}