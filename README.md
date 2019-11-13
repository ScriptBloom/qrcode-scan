## 扫码登录功能Demo—Postman模拟扫码请求
-  扫码登录功能—轮询or长连接WebSocket—Zxing生成二维码

扫码登录其实就是一个登录请求，只不过信息存储在用户手机上，还需要通过二维码验证是否匹配的方式就可以登录，免去了用户多次输入密码的场景，现在越来越多登录方式，其中扫码登录算是比较人性化的了

**我们把一个全局唯一id保存在二维码中，使用手机扫码可以获取到二维码中的信息，此时就把该二维码和你的手机用户账号建立一种绑定的关系，这个二维码就只归你所有了，当你登录完后这个二维码就废弃了，二维码起的作用就是一种认证的机制**

### 流程

具体流程如下图：

![](http://dzou.wangminwei.top/static/images/pj/2.png)


Step 1、用户 A 访问网页客户端，服务器为这个会话生成一个全局唯一的 ID，此时系统并不知道访问者是谁。

Step 2、用户A打开自己的手机App并扫描这个二维码，并提示用户是否确认登录。

Step 3、手机上的是登录状态，用户点击确认登录后，手机上的客户端将账号和这个扫描得到的 ID 一起提交到服务器

Step 4、服务器将这个 ID 和用户 A 的账号绑定在一起，并通知网页版，这个 ID 对应的微信号为用户 A，网页版加载用户 A 的信息，至此，扫码登录全部流程完成



### 创建二维码

我们选取使用自己在服务器端根据创建的全局唯一id生成一个二维码，使用`google`的`zxing`二维码生成类库

- 依赖

```xml
<dependency>
            <groupId>com.google.zxing</groupId>
            <artifactId>javase</artifactId>
            <version>3.2.1</version>
        </dependency>
```

- 生成二维码

根据content内容和指定高度和宽度生成二维码的base64格式图片，可以直接在前端显示

```java
public String createQrCode(String content, int width, int height) throws IOException {
        String resultImage = "";
        if (!StringUtils.isEmpty(content)) {
            ServletOutputStream stream = null;
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            @SuppressWarnings("rawtypes")
            HashMap<EncodeHintType, Comparable> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8"); // 指定字符编码为“utf-8”
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // 指定二维码的纠错等级为中级
            hints.put(EncodeHintType.MARGIN, 2); // 设置图片的边距
            try {
                QRCodeWriter writer = new QRCodeWriter();
                BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

                BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
                ImageIO.write(bufferedImage, "png", os);
                /**
                 * 原生转码前面没有 data:image/png;base64 这些字段，返回给前端是无法被解析，可以让前端加，也可以在下面加上
                 */
                resultImage = new String("data:image/png;base64," + Base64.encode(os.toByteArray()));

                return resultImage;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (stream != null) {
                    stream.flush();
                    stream.close();
                }
            }
        }
        return null;
    }
```



### 二维码状态管理

**我们使用redis来存储每一张二维码的状态**

状态:

1. NOT_SCAN 未被扫描
2. SCANNED 被扫描
3. VERIFIED 确认完后
4. EXPIRED 过期
5. FINISH 完成

> 由于一张二维码只能被扫描一次，所以我们每一次扫描一张二维码后，把状态设置为`SCANNED`，`SCANNED`状态的二维码无法再次被扫描，抛出已被扫描的信息

状态转移：

> NOT_SCANNED->SCANNED->VERIFIED->FINISH
>
> 其中EXPIRED状态可以插在其中任意一个位置，过期了的二维码也自动过期



### 生成二维码接口

- 创建二维码

**使用UUID工具类生成全局唯一id，也可以使用snowflake生成自增的全局唯一id，然后保存到redis中，key为uuid，val为当前二维码状态，我们这里维护了一个map保存所有uuid对应的二维码base格式，用于建立对应关系，前端传递二维码base64过来我们来判断这张二维码对应的uuid是多少**

> 很多人问为什么不让前端传递扫描过后的uuid呢？第一，我们只能使用postman模拟请求，我们无法根据手机app扫码获取二维码信息，所以暂时采取传输图片，实际中肯定采用uuid去传输，因为base64本来就很大，尽量传输数据量小的数据

```java
@GetMapping("/createQr")
    @ResponseBody
    public Result<String> createQrCode() throws IOException {
        String uuid = UUIDUtil.uuid();
        log.info(uuid);
        String qrCode = qrCodeService.createQrCode(uuid,200,200);
        qrCodeMap.put(qrCode,uuid);
        redisService.set(QrCodeKey.UUID,uuid,QrCodeStatus.NOT_SCAN);
        return Result.success(qrCode);
    }
```



### 前端轮询法判断二维码是否被扫描

**目前阿里云登录控制台就是使用轮询的方法，具体为什么不使用长连接我也不清楚，但是说明这种方法也是比较常见的**

后端只需要处理app登录请求和确认请求以及网页端响应的请求就好了

#### 二维码是否被扫描接口—前端只需要轮询该接口

获取到redis保存对应uuid的状态，返回给前端，前端轮询判断做处理

```java
@GetMapping("/query")
    @ResponseBody
    public Result<String> queryIsScannedOrVerified(@RequestParam("img")String img){
        String uuid = qrCodeMap.get(img);
        QrCodeStatus s = redisService.get(QrCodeKey.UUID, uuid, QrCodeStatus.class);
        return Result.success(s.getStatus());
    }
```

#### app扫描接口

app扫描二维码后，拿到对应的二维码信息发送一个扫描请求给后端，携带app用户参数，这里demo演示就模拟一个绝对的用户信息

**之后就是判断redis中uuid的状态，*

- **如果为`NOT_SCAN`，就修改为`SCANNED`**
- **如果为`SCANNED`，就返回重复扫描的错误**
- **如果为`VERIFIED`，就完成本次二维码登录逻辑，用户登录成功**

```java
@GetMapping("/doScan")
    @ResponseBody
    public Result doAppScanQrCode(@RequestParam("username")String username,
                               @RequestParam("password")String password,
                               @RequestParam("uuid")String uuid){
        QrCodeStatus status = redisService.get(QrCodeKey.UUID,uuid,QrCodeStatus.class);
        log.info(status.getStatus());
        if(status.getStatus().isEmpty()) return Result.error(ErrorCodeEnum.UUID_EXPIRED);
        switch (status){
            case NOT_SCAN:
                //等待确认 todo
                if(username.equals("dzou")&&password.equals("1234")){
                    redisService.set(QrCodeKey.UUID,uuid, QrCodeStatus.SCANNED);
                    return Result.success("请手机确认");
                }else{
                    return Result.error(ErrorCodeEnum.LOGIN_FAIL);
                }
            case SCANNED:
                return Result.error(ErrorCodeEnum.QRCODE_SCANNED);
            case VERIFIED:
                return Result.success("你已经确认过了");
        }
        return Result.error(ErrorCodeEnum.SEVER_ERROR);
    }
```

#### app确认登录接口

**app扫描成功后，二维码状态变为`SCANNED`，需要发送一个请求给app前端请求用户确认，用户点击确认后请求这个接口，完成登录**

```java
@GetMapping("/verify")
    @ResponseBody
    public Result verifyQrCode(@RequestParam("uuid")String uuid){
        String status = redisService.get(QrCodeKey.UUID,uuid,String.class);
        if(status.isEmpty()) return Result.error(ErrorCodeEnum.UUID_EXPIRED);
        redisService.set(QrCodeKey.UUID,uuid,QrCodeStatus.VERIFIED);
        return Result.success("确认成功");
    }
```

#### 前端—JQuery

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>扫描二维码</title>
  <!-- jquery -->
  <script type="text/javascript" th:src="@{/js/jquery.min.js}"></script>
  <!-- bootstrap -->
  <link rel="stylesheet" type="text/css" th:href="@{/bootstrap/css/bootstrap.min.css}"/>
  <script type="text/javascript" th:src="@{/bootstrap/js/bootstrap.min.js}"></script>
</head>
<body>
  <h1>二维码</h1>
  <div>
    <table>
      <tr>
        <td><img id="qrCode" width="200" height="200"/></td>
      </tr>
    </table>
  </div>
</body>
<script>
  var img = "";
  $.ajax({
    url: "/api/createQr",
    type:"GET",
    success:function (data) {
      $("#qrCode").attr("src",data.data);
      img = data.data;
      callbackScan($("#qrCode").attr("src"))
    }
  });
    //使用setTimeOut来循环请求判断是否被扫描，被扫描以后调用下面一个函数循环判断是否被确认
  function callbackScan(img) {
    var tID = setTimeout(function() {
      $.ajax({
        url : '/api/query',
        dataType: "json",
        type: 'GET',
        data:{"img":img},
        success : function(res) {
          //process data here
          console.log("img:"+img);
          console.log(res.data);
          if(res.data=="scanned") {
            clearTimeout(tID);
            console.log("请求确认")
            callbackVerify(img)
          }else {
            callbackScan(img)
          }
        }
      }) }, 1500);
  }
//循环判断是否被确认
  function callbackVerify(img) {
    var tID = setTimeout(function() {
      $.ajax({
        url : '/api/query',
        dataType: "json",
        type: 'GET',
        data:{"img":img},
        success : function(res) {
          //process data here
          console.log(res.data);
          if(res.data=="verified") {
            clearTimeout(tID);
            console.log("确认成功")
            window.location.href = "success";
          }else {
            callbackVerify(img)
          }
        }
      }) }, 1500);
  }

</script>
</html>
```

**成功后跳转到成功页面**

#### 测试

- 打开主页创建二维码

![](http://dzou.wangminwei.top/static/images/pj/3.png)


- 拿到服务器端创建的uuid请求扫描接口

![](http://dzou.wangminwei.top/static/images/pj/4.png)


- 拿uuid请求确认接口

![](http://dzou.wangminwei.top/static/images/pj/5.png)


- 确认完成，跳转到登录界面

![](http://dzou.wangminwei.top/static/images/pj/6.png)




### 长连接WebSocket来传输二维码被扫描的信息

> 除了轮询还有一种相对来说更好的实现方式就是WebSocket长连接，但是有些浏览器不支持WebSocket，考虑到这点我们决定使用`SockJs`，他是一种优先Websocket的连接方式，不支持的话它会去使用其他类似轮询的方式

**我们服务器端需要编写对应的WebSocket处理逻辑，我们在加载页面时建立长连接，扫描时请求接口，把状态发送给前端WebSocket，如果为被扫描，发送请求确认的信息，请求确认接口完成确认后发送状态给前端WebSocket，跳转到success页面**

> 我们使用Springboot提供的WebSocket支持类库编写，如果有需要使用netty编写的同学，可以参考我的另外一篇netty的文章

maven依赖

```xml
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
            <version>2.0.4.RELEASE</version>
        </dependency>
```

#### WebSocket配置类

- 其中第一个方法`registerStompEndpoints`相当于指定代理服务器的WebSocket路由
- 第二个方法就是客户端订阅路由，客户端可以接收到这个路由发送的信息

```java
@Configuration
@EnableWebSocketMessageBroker
public class IWebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
//注册一个Stomp 协议的endpoint,并指定 SockJS协议
        registry.addEndpoint("/websocket").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        //registry.setApplicationDestinationPrefixes("/app");
    }
}
```

#### 注入WebSocket发送消息模板

```java
@Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
```

#### 扫描二维码接口

**我们只需要稍微改一下代码，在第一次扫描后使用WebSocket发送一个信息请求确认给前端WebSocket**

```java
@GetMapping("/doScan")
    @ResponseBody
    public Result doAppScanQrCode(@RequestParam("username")String username,
                                  @RequestParam("password")String password,
                                  @RequestParam("uuid")String uuid){
        QrCodeStatus status = redisService.get(QrCodeKey.UUID,uuid,QrCodeStatus.class);
        log.info(
                status.getStatus());
        if(status.getStatus().isEmpty()) return Result.error(ErrorCodeEnum.UUID_EXPIRED);
        switch (status){
            case NOT_SCAN:
                if(username.equals("dzou")&&password.equals("1234")){
                    redisService.set(QrCodeKey.UUID,uuid, QrCodeStatus.SCANNED);
                    simpMessagingTemplate.convertAndSend("/topic/ws","请确认");
                    return Result.success("请手机确认");
                }else{
                    return Result.error(ErrorCodeEnum.LOGIN_FAIL);
                }
            case SCANNED:
                return Result.error(ErrorCodeEnum.QRCODE_SCANNED);
            case VERIFIED:
                return Result.success("你已经确认过了");
        }
        return Result.error(ErrorCodeEnum.SEVER_ERROR);
    }
```

#### 确认登录接口

**我们需要稍改确认的代码，因为确认成功我们需要向客户端订阅的指定路由发送一条消息**

调用`convertAndSend`发送指定消息到指定路由下

```java
@GetMapping("/verify")
    @ResponseBody
    public Result verifyQrCode(@RequestParam("uuid")String uuid){
        String status = redisService.get(QrCodeKey.UUID,uuid,String.class);
        if(status.isEmpty()) return Result.error(ErrorCodeEnum.UUID_EXPIRED);
        redisService.set(QrCodeKey.UUID,uuid,QrCodeStatus.VERIFIED);
        simpMessagingTemplate.convertAndSend("/topic/ws","已经确认");
        return Result.success("确认成功");
    }
```

#### 前端

**前端就不需要轮询的那两个方法了，只需要连接SockJs就好了，根据WebSocket发送的信息进行处理，我们这里需要客户端连接上后进行订阅，指定接收服务器哪个路由发送的消息**

```js
function connect() {
    var socket = new SockJS('/websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
      console.log('Connected: ' + frame);
      stompClient.subscribe('/topic/ws', function (response) {//订阅路由消息
        console.log(response);
        if(response.body=="请确认"){
          layer.msg("请在你的app上确认登录")
        }else if(response.body=="已经确认"){
          window.location.href = "success"
        }
      });
    });
  }
```

#### 测试

- 打开主页创建二维码，连接WebSocket

![](http://dzou.wangminwei.top/static/images/pj/7.png)


- 拿到服务器端创建的uuid请求扫描接口

![](http://dzou.wangminwei.top/static/images/pj/8.png)


- 控制台打印请求确认信息

![](http://dzou.wangminwei.top/static/images/pj/9.png)


- 拿uuid请求确认接口

![](http://dzou.wangminwei.top/static/images/pj/10.png)


- 确认完成，跳转到登录界面，发送已经确认

![](http://dzou.wangminwei.top/static/images/pj/11.png)

