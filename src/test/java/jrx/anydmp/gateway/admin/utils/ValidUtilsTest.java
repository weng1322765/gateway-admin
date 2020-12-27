package jrx.anydmp.gateway.admin.utils;

import jrx.anytxn.common.exception.TxnArgumentException;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ValidUtilsTest {

    @Test
    public void validUrl() {
        String[] urls = {
                "https://sina.com.cn",
                "http://www.baidu.com",
                "lb://abc",
                "://abc",
                "lblb://abc",
                "lb://abc-aasd",
                "lb://abc_adsdfssadf$$$$",
                "http://wasdf",
                "htp://wasdf",
        };

        for (String url:urls) {
            if (!ValidUtils.validUrl(url)){
                System.out.println(url);
            }
        }
    }
}