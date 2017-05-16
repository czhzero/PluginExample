package com.chen.easyplugin.stub;

import android.app.Activity;

/**
 * Created by chenzhaohua on 17/4/7.
 */
public class ActivityStub extends Activity {


    private static class SingleInstanceStub extends ActivityStub {
    }

    private static class SingleTaskStub extends ActivityStub {
    }

    private static class SingleTopStub extends ActivityStub {
    }

    private static class StandardStub extends ActivityStub {
    }


    //Process 1
    public static class P00 {

        public static class Standard00 extends StandardStub {
        }


        public static class SingleTop00 extends SingleTopStub {
        }

        public static class SingleTop01 extends SingleTopStub {
        }

        public static class SingleTop02 extends SingleTopStub {
        }

        public static class SingleTop03 extends SingleTopStub {
        }


        public static class SingleTask00 extends SingleTaskStub {
        }

        public static class SingleTask01 extends SingleTaskStub {
        }

        public static class SingleTask02 extends SingleTaskStub {
        }

        public static class SingleTask03 extends SingleTaskStub {
        }


        public static class SingleInstance00 extends SingleInstanceStub {
        }

        public static class SingleInstance01 extends SingleInstanceStub {
        }

        public static class SingleInstance02 extends SingleInstanceStub {
        }

        public static class SingleInstance03 extends SingleInstanceStub {
        }


    }

}
