package com.huimin.baidulib.bean;

import java.util.List;

/**
 * Created by kermitye
 * Date: 2018/5/18 9:57
 * Desc:百度听写回调实体
 */
public class PartialBean {

    /**
     * results_recognition : ["12345"]
     * origin_result : {"corpus_no":6556471138845618967,"err_no":0,"result":{"word":["12345","12345","1234舞","衣2345","e2345","医2345",
     * "伊2345","12345","123455","2345"]},"sn":"d94d2661-3e17-4cd6-8a73-cc17e5fd544c_s-0","voice_energy":19459.765625}
     * error : 0
     * best_result : 12345
     * result_type : final_result
     */

    public OriginResultBean origin_result;
    public int error;
    public String best_result;
    public String result_type;
    public List<String> results_recognition;

    public static class OriginResultBean {
        /**
         * corpus_no : 6556471138845618967
         * err_no : 0
         * result : {"word":["12345","12345","1234舞","衣2345","e2345","医2345","伊2345","12345","123455","2345"]}
         * sn : d94d2661-3e17-4cd6-8a73-cc17e5fd544c_s-0
         * voice_energy : 19459.765625
         */

        public long corpus_no;
        public int err_no;
        public ResultBean result;
        public String sn;
        public double voice_energy;

        public static class ResultBean {
            public List<String> word;
        }
    }
}
