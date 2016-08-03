# wifidog-java-portal

## 背景

在一些公共场所（比如公交车、地跌、机场等）连接当地的 WiFi 时会弹出一个验证表单，输入验证信息（比如短信验证码）后就能够通过该 WiFi 联网。

## WiFiDog

WiFiDog 是一套开源的无线热点认证管理工具，它主要提供如下功能：

1. 位置相关的内容递送（比如不同的接入点可以投递不同的广告）
2. 用户认证和授权（认证方式可以通过短信，或者是基础第三方开放平台，比如 QQ、微信等，授权可通过流控实现）
3. 集中式网络监控（在认证服务器上可以获得各个 WiFi 热点上用户的流量使用情况）

WiFiDog 在架构上分为两个部分：

* Gateway：即安装在路由器上的 WiFiDog 软件
* Captive Portal：即认证服务器（AuthServer），是 Web HTTP 服务，独立部署在公网上

为了启用认证功能，我们需要对 WiFiDog 进行一点配置，修改配置文件 /etc/wifidog.conf，找到 AuthServer，并取消其中的几行注释：

```
AuthServer {               
    Hostname 192.168.1.109 
# SSLAvailable             
# SSLPort                  
    HTTPPort 8910          
# Path                     
# LoginScriptPathFragment  
# PortalScriptPathFragment 
# MsgScriptPathFragment    
# PingScriptPathFragment   
# AuthScriptPathFragment   
}

```

为了简便，其他的配置项我们都用默认的。修改后重启 WiFiDog：/etc/init.d/wifidog restart

## 认证协议

WiFiDog v1 协议是目前（2016）使用最广泛的，协议流程如下（红色部分即认证服务需要实现的接口）：

![1470207764317](https://img.hacpai.com/d4b1f076eae04cb0a6565809dd87b53c.png) 

1. GW（Gateway） 会定时调用 AS（AuthServer） 的 ping 接口来检查 AS 是否在线
    1.1. AS 在响应 body 里写入 "Pong"
2. 用户连上 GW 后，通过浏览器发出对某站点的请求
    2.1. GW 收到请求后发现用户没有认证过，则重定向用户到 AS 的 /login 
3. 浏览器请求 AS 的 /login 
    3.1.  AS 返回验证表单 HTML
4. 用户填写验证表单后提交 AS
    4.1. AS 验证通过后生成 token 并重定向用户到 GW
5. 浏览器带 token 请求 GW 的验证接口
    5.1. GW 获取用户 token 后再请求 AS /auth 接口
	5.2. AS 验证 token 有效性，成功则返回 "Auth: 1"（注意空格），验证通过后 GW 会定时请求 AS 的 /auth 接口来检查 token 有效性，期间会带上用户的流量数据
    5.3. GW 重定向用户到 AS /portal
6. 浏览器请求 GW 的 /portal 接口
    6.1  AS 重定向用户到 2 中指定的某站点（也可以按需进行广告投放）

## 报文详解

基于 WiFiDog 1.2.1 整理，192.168.1.1 是路由 IP，192.168.1.109 同时是客户端和 AuthServer 的 IP。

### ping 

```
request [
  URI=/wifidog/ping/
  method=GET
  remoteAddr=192.168.1.1
  queryStr=gw_id=100D7F6F25F5&sys_uptime=7265&sys_memfree=95256&sys_load=0.03&wifidog_uptime=61
  headers=[
    user-agent=WiFiDog 1.2.1
    host=192.168.1.109
  ]
]
```

GW 将系路由标识、负载情况传给 AuthSer，可以基于这些数据做监控。

### login

```
request [
  URI=/wifidog/login/
  method=GET
  remoteAddr=192.168.1.109
  queryStr=gw_address=192.168.1.1&gw_port=2060&gw_id=100D7F6F25F5&ip=192.168.1.109&mac=64:00:6a:5d:0a:23&url=http%3A%2F%2Ffangstar.net%2F
  headers=[
    host=192.168.1.109:8910
    connection=keep-alive
    upgrade-insecure-requests=1
    user-agent=Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36
    accept=text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
    accept-encoding=gzip, deflate, sdch
    accept-language=zh-CN,zh;q=0.8,da;q=0.6,en;q=0.4,id;q=0.2,ja;q=0.2,sq;q=0.2,zh-TW;q=0.2,en-US;q=0.2
    cookie=JSESSIONID=3AE20A3B0636E2F7DB7247AD055491FE
  ]
]
```

查询参数中的 url 是用户要访问的目标站点。

### auth

```
request [
  URI=/wifidog/auth/
  method=GET
  remoteAddr=192.168.1.1
  queryStr=stage=login&ip=192.168.1.109&mac=64:00:6a:5d:0a:23&token=22&incoming=0&outgoing=0&gw_id=100D7F6F25F5
  headers=[
    user-agent=WiFiDog 1.2.1
    host=192.168.1.109
  ]
]
```
* stage：第一次认证是 login，后续定时轮询是 counters
* incoming/outgoing：用户流量

### portal

```
request [
  URI=/wifidog/portal/
  method=GET
  remoteAddr=192.168.1.109
  queryStr=gw_id=100D7F6F25F5
  headers=[
    host=192.168.1.109:8910
    connection=keep-alive
    cache-control=max-age=0
    upgrade-insecure-requests=1
    user-agent=Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36
    accept=text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
    referer=http://192.168.1.109:8910/wifidog/login/?gw_address=192.168.1.1&gw_port=2060&gw_id=100D7F6F25F5&ip=192.168.1.109&mac=64:00:6a:5d:0a:23&url=http%3A%2F%2Ffangstar.net%2F
    accept-encoding=gzip, deflate, sdch
    accept-language=zh-CN,zh;q=0.8,da;q=0.6,en;q=0.4,id;q=0.2,ja;q=0.2,sq;q=0.2,zh-TW;q=0.2,en-US;q=0.2
    cookie=JSESSIONID=3AE20A3B0636E2F7DB7247AD055491FE
  ]
]
```


