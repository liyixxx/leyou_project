   
## 1. 系统描述
   乐优商城是一个基于分布式技术部署的项目，整体采用spring boot和spring cloud来开发，由cloud的各个组件来管理微服务中的各个模块。项目前后端分离，使用restful请求风格。
   系统中会使用到分布式的一些主流技术，包括redis，rabbitmq，elastic search，fast DFS等，借用这些技术来完成特定的业务功能。
## 2. 系统执行流程
### 2.1	用户交互界面
用户交互界面处理流程是线性的，和主流电商网站一样，所以按照流程进行介绍。 
- 1. 用户注册：完成用户注册，前后端会对输入值进行校验判断。（调用 QQ邮箱进行验证码认证） 
- 2. 用户登录：数据库存储的用户密码进行了加密处理，和盐值一起存储 
- 3. 商城首页：系统展示门户信息，包括一些分类和图片的展示。 
- 4. 商品搜索：商品的数据存储在 elastic search 中，可以进行普通查询和聚合检索。 
- 5. 商品详情页面展示：查询对于商品的信息，展现给用户。 
- 6. 购物车：加入购物车时判断是否已经登录，如果没有则转发到登录页， 登录之后在跳转回来进行购物车逻辑处理。 
- 7. 订单确认以及下单操作：调用微信支付接口进行支付，页面每隔一段时间发送请求判断是否已经支付完成。 
### 2.2	后端界面
 	后端界面主要是管理者使用，没有一个特定的流程，因此按照模块功能进行介绍。 
- 1. 商品分类查看：按照主流商品的三级分类，进行展示。 
- 2. 规格参数管理：对应交互界面的商品详情参数，可以调整对应商品或者商品集的通用规格。 
- 3. 品牌/商家管理：添加新的品牌或者修改品牌的信息，将图片存储在linux 上。 
- 4. 商品信息：细分为 SPU 和 SKU 的处理操作，同时商品信息会同步到 ES
上，保证交互界面和管理界面的数据一致性。 
### 2.3	系统内部处理流程
- 1. 系统使用微服务搭建方式，在系统外侧设计 nginx 服务器来配置本地域名。当用户输入域名请求时，首先到达 nginx，由它把请求转发到系统内部。 
- 2. 经过 nginx 后，到达微服务的网关组件，由该组件对请求进行解析和处理，决定路由到哪一个具体的微服务子系统，同时网关也需要解决系统的跨域问题。 
- 3. 请求在经过 nginx 和网关后到达具体的微服务系统，经过 MVC 三层处理，最终操作数据库拿到数据，在将数据返回页面进行渲染。 
- 4. 当管理界面执行了修改或者删除等操作时，发送消息给微服务中的消息中间件 mq，由一个消息处理模块来监听这些消息，并且进行对应的处理来保持系统数据的一致性。 
## 3.核心业务处理
### 3.1	交互界面部分
- 1. 用户的登录和注册：在注册时调用 QQ 的邮箱，发送验证码给用户，同时存储一份到 redis 中(由于 redis 可以设置 key 值有效期，可以实现限时的验证码校验)，比对数据后完成注册。 
- 2. 认证：在用户登录后，会把信息加密存储到 cookie 进行携带，在需要进行认证的业务上会携带 cookie 信息，由认证服务进行解析和判断用户信息是否正确。 
- 3. 商品搜索：借助 elastic search 完成搜索功能，首先通过关键字完成模糊匹配，显示相关的结果并提供二级搜索条件，在点击搜索条件(比如价格区间)后使用 es 的聚合查询完成复杂搜索，将桶中具体的信息展现出来。 
- 4. 购物车：加入购物车和支付需要进行认证验证，用户的信息封装到一个对象中，并存入 ThreadLocal 给认证服务进行判断。认证通过后才可以加入购物车，购物车中存储的信息放入在 redis，通过设置 key 键的格式，完成不同用户拥有各自的购物车，并且可以对购物车进行处理。 
- 5. 支付：认证后进行支付，调用微信的二维码来完成支付的操作。 
### 3.2	管理界面部分
- 1. 规格参数管理：规格参数分为了规格参数组和详细参数，涉及到多表操作，需要在 mapper 中实现不同的 SQL 语句进行处理，而单表操作则可以借用 mybatis 逆向工程实现。 
- 2. 商品管理：设置 mysql 进行模糊查询，借助 pageHelper 完成分页处理。对商品进行修改增加时，需要先指定分类，在指定商品信息，随后是 SPU 和 SKU 的参数，需要每张表单独处理，中间需要进行事务处理保证操作一次全部成功。 
## 4.服务部署以及框架使用
### 4.1	服务部署
 	在系统中需要部署其他的应用来完成特定业务。 
- 1. nginx：负责本地域名配置和本地化处理，在 windows 下设置。 
- 2. docker：在linux 上配置，负责装载和配置其他的服务端软件，比如redis，rabbitmq 等。 
- 3. redis：负责缓存模块，存储一些热点信息，或者是一些临时信息(验证码)，在 docker 上安装部署。 
- 4. rebbitmq：负责消息中间件处理，保证前后端数据的一致，在 docker 上安装部署 
- 5. es：负责搜索功能，在 linux 上配置。 
- 6. fast DFS：负责存储一些文件，包括图片信息等等，在 linux 上配置。 
### 4.2	框架使用
- 1. 微服务搭建：spring boot + spring cloud。由 spring boot 完成一个微服务子系统，spring cloud 管理各个微服务，包括熔断和服务注册等。
- 2. 业务模块：ssm + redis + mysql。使用 MVC 模式，借助 ssm 框架完成业务逻辑，操作数据库或者缓存。 
- 3. 微服务组件：rabbitmq + es + fast DFS。通过这些组件实现特定业务下的某些功能，比如搜索或者中间件处理。
