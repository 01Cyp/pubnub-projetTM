##### YOU MUST HAVE A PUBNUB ACCOUNT TO USE THE API.
##### http://www.pubnub.com/account

## PubNub 3.5 Real-time Cloud Push API - JAVA

www.pubnub.com - PubNub Real-time Push Service in the Cloud. 

Please reference the demo app in 3.4/jars/PubnubDemoConsole.jar for an example of using PubNub
Asyncronously in your applications!

Additional Java docs are available at 3.4/doc/index.html
###To browse source
Checkout [srcPubnubApi](/java/srcPubnubApi)

###To setup with your IDE:

1. Add new project with java/ as the root
2. Set the new project source as java/srcPubnubApi
3. Add java/Pubnub-Standardedition-3.5.jar and 3.5/libs/*.jar as project libaries
4. You should be able to run PubnubDemoConsole and PubnubExample via your IDE

###To build from the CL, run:
```
$ ant clean build
$ ant
```

###To test from the CL, run:
```
$ ant test
```

###To be really cool and clean, build, and test at once!
```
$ ant clean build test
```

###To run the DemoConsole:
```
$ cd jars
$ java -jar PubnubDemoConsole.jar
```

###Heres an example of running PubnubDemoConsole:

```
$~/pubnub/java/java/jars$ java -jar PubnubDemoConsole.jar 
HINT:  To test Re-connect and catch-up
	Disconnect your machine from network/internet and
	re-connect your machine after sometime
Enable SSL ? Enter Y for Yes, else N
N

SSL not enabled
Enter cipher key for encryption feature
If you don't want to avail at this time, press ENTER

No Cipher key provided
ENTER 1  FOR Subscribe (Currently subscribed to no channels.)
ENTER 2  FOR Publish
ENTER 3  FOR Presence
ENTER 4  FOR Detailed History
ENTER 5  FOR Here_Now
ENTER 6  FOR Unsubscribe
ENTER 7  FOR Presence-Unsubscribe
ENTER 8  FOR Time
ENTER 9  FOR EXIT OR QUIT
ENTER 10 FOR Disconnect-And-Resubscribe
ENTER 11 FOR Toggle Resume On Reconnect

ENTER 0 to display this menu
1
Subscribe: Enter Channel name
hello_world
Subscribed to following channels: 
hello_world
ENTER 1  FOR Subscribe (Currently subscribed to hello_world)
ENTER 2  FOR Publish
ENTER 3  FOR Presence
ENTER 4  FOR Detailed History
ENTER 5  FOR Here_Now
ENTER 6  FOR Unsubscribe
ENTER 7  FOR Presence-Unsubscribe
ENTER 8  FOR Time
ENTER 9  FOR EXIT OR QUIT
ENTER 10 FOR Disconnect-And-Resubscribe
ENTER 11 FOR Toggle Resume On Reconnect

ENTER 0 to display this menu

SUBSCRIBE : CONNECT on channel:hello_world
1
Subscribe: Enter Channel name
my_channel
Subscribed to following channels: 
hello_world : my_channel
ENTER 1  FOR Subscribe (Currently subscribed to hello_world,my_channel)
ENTER 2  FOR Publish
ENTER 3  FOR Presence
ENTER 4  FOR Detailed History
ENTER 5  FOR Here_Now
ENTER 6  FOR Unsubscribe
ENTER 7  FOR Presence-Unsubscribe
ENTER 8  FOR Time
ENTER 9  FOR EXIT OR QUIT
ENTER 10 FOR Disconnect-And-Resubscribe
ENTER 11 FOR Toggle Resume On Reconnect

ENTER 0 to display this menu
SUBSCRIBE : CONNECT on channel:my_channel
2
Publish: Enter Channel name
hello_world
Enter the message for publish. To exit loop enter QUIT
"hello, world!"
PUBLISH : [1,"Sent","13597534956712683"]
SUBSCRIBE : hello_world : class java.lang.String : hello, world!
SUBSCRIBE : hello_world : class java.lang.String : 3ZoxkVMB97lf09jpFU9gtw==
SUBSCRIBE : hello_world : class java.lang.String : 3ZoxkVMB97lf09jpFU9gtw==
```
[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/pubnub/java/trend.png)](https://bitdeli.com/free "Bitdeli Badge")
