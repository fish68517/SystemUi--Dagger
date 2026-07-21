# Dagger：`NetworkService` 与 `NetworkModule` 绑定逻辑分析

本文记录下面这段代码的编译结论，以及 `NetworkService` 为什么能拿到 `NetworkModule` 中提供的 `OkHttpClient`。

```java
class NetworkService {

    private final OkHttpClient client;

    @Inject
    NetworkService(OkHttpClient client) {
        this.client = client;
    }
}

@Module
class NetworkModule {

    @Provides
    static OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .build();
    }
}

@Component(modules = NetworkModule.class)
interface AppComponent {

    NetworkService getNetworkService();
}
```

## 会不会编译报错

不会因为 Dagger 依赖图报错。

前提是普通 Java / Android 工程条件都满足：

- 已经添加 OkHttp 依赖；
- 已经添加 Dagger 依赖；
- 已经配置 annotation processor，例如 `kapt` 或 `annotationProcessor`；
- import 正确，例如 `javax.inject.Inject`、`dagger.Module`、`dagger.Provides`、`dagger.Component`。

从 Dagger 依赖图角度看，这段代码是完整的：

```text
AppComponent.getNetworkService()
        |
        v
需要 NetworkService
        |
        v
NetworkService 有 @Inject 构造函数
        |
        v
构造函数需要 OkHttpClient
        |
        v
NetworkModule 有 @Provides OkHttpClient
        |
        v
调用 provideOkHttpClient()
        |
        v
new NetworkService(okHttpClient)
```

Dagger 生成代码可以理解成近似这样：

```java
final class DaggerAppComponent implements AppComponent {

    @Override
    public NetworkService getNetworkService() {
        OkHttpClient client = NetworkModule.provideOkHttpClient();
        return new NetworkService(client);
    }
}
```

实际生成代码通常会经过 `Factory` 和 `Provider` 包装，但核心逻辑就是：

```text
先得到 OkHttpClient，再创建 NetworkService
```

## 为什么 `OkHttpClient` 必须由 Module 提供

`OkHttpClient` 是第三方库对象。你不能修改它的构造函数，也不能给它的构造函数加 `@Inject`。

所以 Dagger 不能靠下面这种方式自动创建：

```java
new OkHttpClient()
```

更不能自动猜测你想用：

```java
new OkHttpClient.Builder().build()
```

因此需要：

```java
@Provides
static OkHttpClient provideOkHttpClient() {
    return new OkHttpClient.Builder().build();
}
```

`@Provides` 的含义是：当对象图里需要 `OkHttpClient` 时，可以调用这个方法获取。

## `NetworkService` 为什么知道去 `NetworkModule` 找

严格说，`NetworkService` 自己并不知道去哪里找。

真正负责查找和连接依赖的是 Dagger 为 `AppComponent` 生成的代码。

这行代码很关键：

```java
@Component(modules = NetworkModule.class)
```

它表示：`NetworkModule` 里的 `@Provides` 绑定会加入 `AppComponent` 的对象图。

然后 Dagger 分析：

```java
@Inject
NetworkService(OkHttpClient client)
```

这表示创建 `NetworkService` 时需要一个依赖 Key：

```text
OkHttpClient
```

再分析：

```java
@Provides
static OkHttpClient provideOkHttpClient()
```

这表示 `NetworkModule` 提供一个依赖 Key：

```text
OkHttpClient
```

两者匹配，所以 Dagger 连接成功。

```text
需要的 Key：OkHttpClient
提供的 Key：OkHttpClient
```

## 不是根据命名匹配

Dagger 不会根据这些名字匹配：

- `NetworkService`
- `NetworkModule`
- `provideOkHttpClient`
- `client`

真正的匹配规则是：

```text
依赖 Key = 类型 + 限定符
```

当前没有 `@Named` 或自定义 `@Qualifier`，所以 Key 就是：

```text
OkHttpClient
```

因此，即使把 Module 和方法改名，只要 Component 引入了它，返回类型仍然是 `OkHttpClient`，也能匹配：

```java
@Module
class AbcModule {

    @Provides
    static OkHttpClient createAnything() {
        return new OkHttpClient.Builder().build();
    }
}

@Component(modules = AbcModule.class)
interface AppComponent {
    NetworkService getNetworkService();
}
```

这里方法名叫 `createAnything()`，Module 名叫 `AbcModule`，仍然可以工作。

原因是：

```text
NetworkService 需要 OkHttpClient
AbcModule 提供 OkHttpClient
AppComponent 引入了 AbcModule
```

## 如果有多个 Module 会怎样

例如：

```java
@Component(modules = {
    NetworkModule.class,
    OtherModule.class
})
interface AppComponent {
    NetworkService getNetworkService();
}
```

Dagger 会在 `AppComponent` 注册的所有 Module 和可用的 `@Inject` 构造函数里查找 `OkHttpClient` 绑定。

结果有三种：

### 1. 只有一个 `OkHttpClient` 绑定

编译通过。

```text
需要 OkHttpClient
找到唯一 OkHttpClient
使用它
```

### 2. 没有 `OkHttpClient` 绑定

编译失败。

典型错误类似：

```text
OkHttpClient cannot be provided without an @Inject constructor or an @Provides-annotated method
```

原因是 Dagger 不知道如何创建 `OkHttpClient`。

### 3. 有多个无区分的 `OkHttpClient` 绑定

编译失败。

例如：

```java
@Module
class NetworkModule {
    @Provides
    static OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder().build();
    }
}

@Module
class OtherModule {
    @Provides
    static OkHttpClient provideAnotherOkHttpClient() {
        return new OkHttpClient.Builder().build();
    }
}
```

这两个方法虽然方法名不同，但都提供同一个 Key：

```text
OkHttpClient
```

Dagger 不会根据方法名判断哪个更适合 `NetworkService`，而是报重复绑定错误。

## 如何区分多个 `OkHttpClient`

如果确实需要两个不同的 `OkHttpClient`，要加限定符。

例如使用 `@Named`：

```java
@Module
class NetworkModule {

    @Provides
    @Named("api")
    static OkHttpClient provideApiClient() {
        return new OkHttpClient.Builder().build();
    }

    @Provides
    @Named("upload")
    static OkHttpClient provideUploadClient() {
        return new OkHttpClient.Builder().build();
    }
}
```

使用方也要写清楚需要哪个：

```java
class NetworkService {

    private final OkHttpClient client;

    @Inject
    NetworkService(@Named("api") OkHttpClient client) {
        this.client = client;
    }
}
```

此时依赖 Key 变成：

```text
@Named("api") OkHttpClient
```

另一个是：

```text
@Named("upload") OkHttpClient
```

Dagger 就能准确区分。

## 什么时候会报错

下面几种情况会报错。

### 删除 `@Provides OkHttpClient`

```java
@Component
interface AppComponent {
    NetworkService getNetworkService();
}
```

`NetworkService` 仍然需要 `OkHttpClient`，但对象图里没有提供方式，会报缺失绑定。

### `NetworkService` 没有 `@Inject` 构造函数

```java
class NetworkService {
    NetworkService(OkHttpClient client) {
        this.client = client;
    }
}
```

如果没有额外提供：

```java
@Provides
static NetworkService provideNetworkService(OkHttpClient client)
```

Dagger 就不知道怎么创建 `NetworkService`。

### 存在两个相同 Key 的 `OkHttpClient`

两个无 `@Qualifier` 的 `@Provides OkHttpClient` 会导致重复绑定。

### `NetworkModule` 没有加入 `AppComponent`

```java
@Component
interface AppComponent {
    NetworkService getNetworkService();
}
```

即使 `NetworkModule` 类存在，只要没有被当前 Component 引入，它就不属于当前对象图。

`Module` 不是全局自动生效的。它必须通过 Component 的 `modules = ...` 加入对应对象图。

## 最终结论

这段代码不会因为 Dagger 依赖图报错。

原因是：

```text
NetworkService 通过 @Inject 构造函数告诉 Dagger：
我需要 OkHttpClient。

AppComponent 通过 modules = NetworkModule.class 告诉 Dagger：
当前对象图包含 NetworkModule。

NetworkModule 通过 @Provides OkHttpClient 告诉 Dagger：
我能提供 OkHttpClient。
```

所以 Dagger 可以生成完整对象创建链。

最重要的理解点是：

```text
Dagger 按 类型 + 限定符 匹配依赖，
不是按类名、方法名、参数名匹配。
```
