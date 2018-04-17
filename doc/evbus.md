# EvBus Guide

* Previous activity

```java

public class PreviousActivity extends TActivity {

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        //register by annotations
        if (!EvBus.registerAnnotationsQuiet(this)) {
            //false if some EvTransmitPopDeclare annotated field inject failed (Missing messages from previous activity)
            return;
        }
        //build views
        initData();
        initView();
    }

    //register receiver (register/post mode)
    @EvReceiverDeclare(type = EvBus.Type.ON_START)
    private void refreshUi(Message2 message) {
        //refresh ui
    }

    //to next activity
    private void toNextActivity(){
        //create message
        Message1 message = new Message1();
        message.setContent("hello activity 2");
        //push message to next activity (transmit mode)
        EvBus.transmitPush(this, message);
        //start next activity
        startActivity(new Intent(this, NextActivity.class));
    }

}

```

* Next activity

```java

public class NextActivity extends TActivity {

    //inject Message1 from previous activity (transmit mode)
    @EvTransmitPopDeclare(required = true)
    private Message1 message;
    
    //The flag to avoid NullPointException
    private boolean invalidData = false;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        //register by annotations
        if (!EvBus.registerAnnotationsQuiet(this)) {
            //false if some EvTransmitPopDeclare annotated field inject failed (Missing messages from previous activity)
            invalidData = true;
            return;
        }
        //build views
        initData();
        initView();
    }

    //post message to previous activity
    private void postToPreviousActivity(){
        //create message
        Message2 message = new Message2();
        message.setContent("hello activity 1");
        //post message to receiver in previous activity (register/post mode)
        EvBus.post(message);
    }
    
}

```